package com.englishtown.vertx.solr.impl;

import com.englishtown.vertx.solr.SolrConfigurator;
import com.englishtown.vertx.solr.VertxSolrQuery;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import org.apache.solr.client.solrj.SolrRequest;
import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.apache.solr.common.util.NamedList;
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
    NamedList<Object> namedList;
    SolrDocumentList results;

    @Mock
    Vertx vertx;

    @Mock
    SolrConfigurator configurator;
    @Mock
    SolrServer solrServer;
    @Mock
    Handler<AsyncResult<JsonObject>> asyncResultHandler;
    @Captor
    ArgumentCaptor<Future<JsonObject>> futureCaptor;


    @Before
    public void setUp() throws Exception {

        results = new SolrDocumentList();
        namedList = new NamedList<>();
        namedList.add("response", results);

        when(configurator.createSolrServer()).thenReturn(solrServer);
        when(solrServer.request(any(SolrRequest.class))).thenReturn(namedList);

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
        verify(solrServer).shutdown();

    }

    @Test
    public void testQuerySolrServerException() throws Exception {
        //noinspection unchecked
        when(solrServer.query(any())).thenThrow(SolrServerException.class);
        service.query((VertxSolrQuery) null, asyncResultHandler);
        verify(asyncResultHandler).handle(any());
    }

    @Test
    public void testQuery() throws Exception {
        VertxSolrQuery options = new VertxSolrQuery();
        SolrDocumentList results = new SolrDocumentList();
        SolrDocument result = new SolrDocument();
        result.setField("prop1", "val");
        results.add(result);

        QueryResponse queryResponse = mock(QueryResponse.class);
        when(solrServer.query(any())).thenReturn(queryResponse);
        when(queryResponse.getResults()).thenReturn(results);
        service.query(options, asyncResultHandler);
        verify(asyncResultHandler).handle(futureCaptor.capture());
        assertTrue(futureCaptor.getValue().succeeded());
    }

}