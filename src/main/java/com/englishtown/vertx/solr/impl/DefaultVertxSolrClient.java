package com.englishtown.vertx.solr.impl;

import com.englishtown.vertx.solr.VertxSolrClient;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.http.*;
import io.vertx.core.json.JsonObject;
import org.apache.solr.client.solrj.SolrRequest;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.HttpClientUtil;
import org.apache.solr.common.params.CommonParams;
import org.apache.solr.common.params.ModifiableSolrParams;
import org.apache.solr.common.params.SolrParams;

import javax.inject.Inject;
import java.net.URI;
import java.util.Base64;

/**
 * Default implementation of {@link VertxSolrClient}
 */
public class DefaultVertxSolrClient implements VertxSolrClient {

    private static final String DEFAULT_PATH = "/select";
    protected String baseUrl;
    protected HttpClient httpClient;
    protected ModifiableSolrParams invariantParams = new ModifiableSolrParams();

    @Inject
    public DefaultVertxSolrClient(Vertx vertx, String baseUrl, HttpClientOptions options) {

        if (baseUrl == null) {
            throw new IllegalArgumentException("The base URL cannot be null");
        }
        if (baseUrl.indexOf('?') >= 0) {
            throw new IllegalArgumentException("The base URL must not contain parameters: "
                    + baseUrl);
        }
        if (baseUrl.endsWith("/")) {
            baseUrl = baseUrl.substring(0, baseUrl.length() - 1);
        }

        this.baseUrl = baseUrl;
        this.httpClient = vertx.createHttpClient(options);

    }

    @Override
    public void setInvariantParams(SolrParams params) {
        if (params != null) {
            invariantParams.add(params);
        }
    }

    @Override
    public void request(SolrRequest request, Handler<AsyncResult<JsonObject>> resultHandler) {

        if (request.getMethod() != SolrRequest.METHOD.GET) {
            throw new IllegalStateException("Only GET operations are currently supported");
        }

        String path = request.getPath();

        if (null != request.getParams().get("collection")){
        	path = "/" + request.getParams().get("collection") + "/select";
        }
        
        if (path == null || !path.startsWith("/")) {
            path = DEFAULT_PATH;
        }

        ModifiableSolrParams params = new ModifiableSolrParams(request.getParams());
        params.set(CommonParams.WT, "json");

        if (invariantParams != null) {
            params.add(invariantParams);
        }

        // Remove credentials so they don't appear in the http request query string
        String authUser = params.get(HttpClientUtil.PROP_BASIC_AUTH_USER);
        String authPassword = params.get(HttpClientUtil.PROP_BASIC_AUTH_PASS);
        params.remove(HttpClientUtil.PROP_BASIC_AUTH_USER);
        params.remove(HttpClientUtil.PROP_BASIC_AUTH_PASS);

        URI uri = URI.create(baseUrl + path + params.toQueryString());
        int port = uri.getPort() > 0 ? uri.getPort() : ("https".equals(uri.getScheme()) ? 443 : 80);

        HttpClientRequest httpRequest = httpClient.request(HttpMethod.GET, port, uri.getHost(), uri.getPath() + "?" + uri.getQuery());

        if ((authUser != null && !authUser.isEmpty()) && (authPassword != null && !authPassword.isEmpty())) {
            String authHeader = authUser + ":" + authPassword;
            String encodedHeader = new String(Base64.getEncoder().encode(authHeader.getBytes()));
            httpRequest.putHeader("Authorization", "Basic " + encodedHeader);
        }

        httpRequest
                .exceptionHandler(t -> resultHandler.handle(Future.failedFuture(t)))
                .handler(response -> {
                    if (response.statusCode() != 200) {
                        resultHandler.handle(Future.failedFuture(new SolrServerException("Received http response status " + response.statusCode())));
                        return;
                    }

                    String contentType = response.headers().get(HttpHeaders.CONTENT_TYPE);
                    if (contentType == null || !(contentType.startsWith("application/json") || contentType.startsWith("text/plain"))) {
                        resultHandler.handle(Future.failedFuture(new SolrServerException("Received content type " + contentType)));
                        return;
                    }

                    response.bodyHandler(body -> {
                        JsonObject json = new JsonObject(body.toString());
                        resultHandler.handle(Future.succeededFuture(json));
                    });

                })
                .setTimeout(30000)
                .end();

    }

    @Override
    public void stop() {
        if (httpClient != null) {
            httpClient.close();
        }
    }

}
