package com.englishtown.vertx.solr.integration;

import com.englishtown.vertx.solr.SolrConfigurator;
import com.englishtown.vertx.solr.VertxSolrClient;

import static org.mockito.Mockito.mock;

/**
 * Mock implementation of {@link com.englishtown.vertx.solr.SolrConfigurator}
 */
public class MockSolrConfigurator implements SolrConfigurator {

    private VertxSolrClient solrClient = mock(VertxSolrClient.class);

    @Override
    public VertxSolrClient createSolrClient() {
        return solrClient;
    }
}
