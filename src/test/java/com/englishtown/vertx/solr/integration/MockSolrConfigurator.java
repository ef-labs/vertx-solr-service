package com.englishtown.vertx.solr.integration;

import com.englishtown.vertx.solr.SolrConfigurator;
import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocumentList;
import org.apache.solr.common.params.SolrParams;

import javax.inject.Inject;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Mock implementation of {@link com.englishtown.vertx.solr.SolrConfigurator}
 */
public class MockSolrConfigurator implements SolrConfigurator {

    private SolrServer solrServer = mock(SolrServer.class);
    private QueryResponse queryResponse = mock(QueryResponse.class);
    private SolrDocumentList results = new SolrDocumentList();

    @Inject
    public MockSolrConfigurator() throws SolrServerException {

        when(solrServer.query(any(SolrParams.class))).thenReturn(queryResponse);
        when(queryResponse.getResults()).thenReturn(results);

    }

    @Override
    public SolrServer createSolrServer() {
        return solrServer;
    }
}
