package com.englishtown.vertx.solr.impl;

import com.englishtown.vertx.solr.SolrConfigurator;
import com.englishtown.vertx.solr.VertxSolrQuery;
import com.englishtown.vertx.solr.VertxSolrServer;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class DefaultSolrServiceTest {

    DefaultSolrService service;

    @Mock
    Vertx vertx;

    @Mock
    SolrConfigurator configurator;
    @Mock
    VertxSolrServer solrServer;
    @Mock
    Handler<AsyncResult<JsonObject>> asyncResultHandler;
    @Captor
    ArgumentCaptor<Future<JsonObject>> futureCaptor;


    @Before
    public void setUp() throws Exception {

        when(configurator.createSolrServer()).thenReturn(solrServer);

        service = new DefaultSolrService(configurator);
        service.start();

    }

    @Test
    public void testStart() throws Exception {
        // start() is called from setUp
        // just verify the configurator createSolrServer() was called
        verify(configurator).createSolrServer();
    }

    @Test
    public void testStop() throws Exception {

        service.stop();
        verify(solrServer).stop();

    }

    @Test
    public void testQuery() throws Exception {
        VertxSolrQuery query = new VertxSolrQuery();

        service.query(query, asyncResultHandler);
        verify(solrServer).request(any(), eq(asyncResultHandler));
    }

}