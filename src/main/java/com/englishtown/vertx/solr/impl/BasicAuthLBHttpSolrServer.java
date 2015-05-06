package com.englishtown.vertx.solr.impl;

import org.apache.http.client.HttpClient;
import org.apache.solr.client.solrj.ResponseParser;
import org.apache.solr.client.solrj.impl.HttpSolrServer;
import org.apache.solr.client.solrj.impl.LBHttpSolrServer;

import java.net.MalformedURLException;

/**
 * Extension of {@link org.apache.solr.client.solrj.impl.LBHttpSolrServer} with wrapped {@link com.englishtown.vertx.solr.impl.BasicAuthHttpSolrServer}
 */
public class BasicAuthLBHttpSolrServer extends LBHttpSolrServer {

    public BasicAuthLBHttpSolrServer(String... solrServerUrls) throws MalformedURLException {
        super(solrServerUrls);
    }

    /**
     * The provided httpClient should use a multi-threaded connection manager
     *
     * @param httpClient
     * @param solrServerUrl
     */
    public BasicAuthLBHttpSolrServer(HttpClient httpClient, String... solrServerUrl) {
        super(httpClient, solrServerUrl);
    }

    /**
     * The provided httpClient should use a multi-threaded connection manager
     *
     * @param httpClient
     * @param parser
     * @param solrServerUrl
     */
    public BasicAuthLBHttpSolrServer(HttpClient httpClient, ResponseParser parser, String... solrServerUrl) {
        super(httpClient, parser, solrServerUrl);
    }

    @Override
    protected HttpSolrServer makeServer(String server) {
        HttpSolrServer s = new BasicAuthHttpSolrServer(server, getHttpClient(), getParser());
        if (getRequestWriter() != null) {
            s.setRequestWriter(getRequestWriter());
        }
        if (getQueryParams() != null) {
            s.setQueryParams(getQueryParams());
        }
        return s;
    }
}
