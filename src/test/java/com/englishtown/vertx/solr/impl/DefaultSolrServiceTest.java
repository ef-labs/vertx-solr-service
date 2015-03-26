package com.englishtown.vertx.solr.impl;

import com.englishtown.vertx.solr.SolrConfigurator;
import com.englishtown.vertx.solr.VertxSolrQuery;
import com.englishtown.vertx.solr.VertxSolrClient;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
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
    VertxSolrClient solrClient;
    @Mock
    Handler<AsyncResult<JsonObject>> asyncResultHandler;
    @Captor
    ArgumentCaptor<Future<JsonObject>> futureCaptor;


    @Before
    public void setUp() throws Exception {

        when(configurator.createSolrClient()).thenReturn(solrClient);

        service = new DefaultSolrService(configurator);
        service.start();

    }

    @Test
    public void testStart() throws Exception {
        // start() is called from setUp
        // just verify the configurator createSolrClient() was called
        verify(configurator).createSolrClient();
    }

    @Test
    public void testStop() throws Exception {

        service.stop();
        verify(solrClient).stop();

    }

    @Test
    public void testQuery() throws Exception {
        VertxSolrQuery query = new VertxSolrQuery();

        service.query(query, asyncResultHandler);
        verify(solrClient).request(any(), eq(asyncResultHandler));
    }

}