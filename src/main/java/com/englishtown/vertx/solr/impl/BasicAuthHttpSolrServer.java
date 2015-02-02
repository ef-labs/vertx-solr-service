package com.englishtown.vertx.solr.impl;

import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.solr.client.solrj.ResponseParser;
import org.apache.solr.client.solrj.SolrRequest;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.HttpClientUtil;
import org.apache.solr.client.solrj.impl.HttpSolrServer;

import java.io.IOException;
import java.util.Base64;

/**
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
        HttpRequestBase base = super.createMethod(request);

        String authUser = request.getParams().get(HttpClientUtil.PROP_BASIC_AUTH_USER);
        String authPassword = request.getParams().get(HttpClientUtil.PROP_BASIC_AUTH_PASS);

        if ((authUser != null && !authUser.isEmpty()) && (authPassword != null && !authPassword.isEmpty())) {
            String authHeader = authUser + ":" + authPassword;
            String encodedHeader = new String(Base64.getEncoder().encode(authHeader.getBytes()));

            base.addHeader("Authorization", "Basic " + encodedHeader);
        }

        return base;
    }
}
