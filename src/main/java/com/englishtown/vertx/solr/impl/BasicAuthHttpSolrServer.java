package com.englishtown.vertx.solr.impl;

import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.solr.client.solrj.ResponseParser;
import org.apache.solr.client.solrj.SolrRequest;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.HttpClientUtil;
import org.apache.solr.client.solrj.impl.HttpSolrServer;
import org.apache.solr.common.params.ModifiableSolrParams;
import org.apache.solr.common.params.SolrParams;

import java.io.IOException;
import java.util.Base64;

/**
 * Extension of {@link org.apache.solr.client.solrj.impl.HttpSolrServer} which can add the basic authorization http header
 */
public class BasicAuthHttpSolrServer extends HttpSolrServer {

    public BasicAuthHttpSolrServer(String baseURL) {
        super(baseURL);
    }

    public BasicAuthHttpSolrServer(String baseURL, HttpClient client) {
        super(baseURL, client);
    }

    public BasicAuthHttpSolrServer(String baseURL, HttpClient client, ResponseParser parser) {
        super(baseURL, client, parser);
    }

    @Override
    protected HttpRequestBase createMethod(SolrRequest request) throws IOException, SolrServerException {

        SolrParams params = request.getParams();
        String authUser = params.get(HttpClientUtil.PROP_BASIC_AUTH_USER);
        String authPassword = params.get(HttpClientUtil.PROP_BASIC_AUTH_PASS);

        // Remove credentials so they don't appear in the http request query string
        if (params instanceof ModifiableSolrParams) {
            ModifiableSolrParams modifiableParams = (ModifiableSolrParams) params;
            modifiableParams.remove(HttpClientUtil.PROP_BASIC_AUTH_USER);
            modifiableParams.remove(HttpClientUtil.PROP_BASIC_AUTH_PASS);
        }

        HttpRequestBase requestBase = super.createMethod(request);

        if ((authUser != null && !authUser.isEmpty()) && (authPassword != null && !authPassword.isEmpty())) {
            String authHeader = authUser + ":" + authPassword;
            String encodedHeader = new String(Base64.getEncoder().encode(authHeader.getBytes()));

            requestBase.addHeader("Authorization", "Basic " + encodedHeader);
        }

        return requestBase;
    }
}
