package com.englishtown.vertx.solr.impl;

import com.englishtown.vertx.solr.*;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;
import org.apache.solr.client.solrj.impl.HttpClientUtil;
import org.apache.solr.client.solrj.request.QueryRequest;

import javax.inject.Inject;

/**
 * Default implementation of {@link com.englishtown.vertx.solr.SolrService}
 */
public class DefaultSolrService implements SolrService {

    private final SolrConfigurator configurator;
    private VertxSolrClient solrClient;

    @Inject
    public DefaultSolrService(SolrConfigurator configurator) {
        this.configurator = configurator;
    }

    @Override
    public void start() {
        solrClient = configurator.createSolrClient();
    }

    @Override
    public void stop() {
        if (solrClient != null) {
            solrClient.stop();
            solrClient = null;
        }
    }

    @Override
    public void query(JsonObject query, QueryOptions options, Handler<AsyncResult<JsonObject>> resultHandler) {
        query(new VertxSolrQuery(query), options, resultHandler);
    }

    @Override
    public void query(VertxSolrQuery query, QueryOptions options, Handler<AsyncResult<JsonObject>> resultHandler) {

        if (options == null) {
            options = new QueryOptions();
        } else {
            if (options.getBasicAuthUser() != null && options.getBasicAuthPass() != null) {
                query.set(HttpClientUtil.PROP_BASIC_AUTH_USER, options.getBasicAuthUser());
                query.set(HttpClientUtil.PROP_BASIC_AUTH_PASS, options.getBasicAuthPass());
            }
        }

        try {
            QueryRequest request = new QueryRequest(query);
            if (options.getCore() != null) {
                request.setPath("/" + options.getCore() + "/select");
            }

            solrClient.request(request, resultHandler);

        } catch (Throwable t) {
            resultHandler.handle(Future.failedFuture(t));

        }

    }

}
