package com.englishtown.vertx.solr.integration;

import com.englishtown.vertx.solr.SolrConfigurator;
import com.englishtown.vertx.solr.VertxSolrServer;

import static org.mockito.Mockito.mock;

/**
 * Mock implementation of {@link com.englishtown.vertx.solr.SolrConfigurator}
 */
public class MockSolrConfigurator implements SolrConfigurator {

    private VertxSolrServer solrServer = mock(VertxSolrServer.class);

    @Override
    public VertxSolrServer createSolrServer() {
        return solrServer;
    }
}
