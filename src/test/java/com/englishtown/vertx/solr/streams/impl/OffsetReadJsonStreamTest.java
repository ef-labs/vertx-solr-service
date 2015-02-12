package com.englishtown.vertx.solr.streams.impl;

import com.englishtown.vertx.solr.QueryOptions;
import com.englishtown.vertx.solr.SolrService;
import com.englishtown.vertx.solr.VertxSolrQuery;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class OffsetReadJsonStreamTest {

    OffsetJsonReadStream readJsonStream;

    // put into Message<JsonObject> used to simulate various reply messages from Solr
    JsonObject result;
    JsonObject response;

    @Captor
    ArgumentCaptor<Handler<AsyncResult<JsonObject>>> resultHandlerCaptor;
    @Mock
    Handler<JsonObject> dataHandler;
    @Mock
    AsyncResult<JsonObject> jsonResult; // message wrapper for the Solr json response
    @Mock
    Handler<Throwable> exceptionHandler;
    @Mock
    Handler<Void> endHandler;
    @Mock
    SolrService solrService;

    @Before
    public void setUp() throws Exception {

        VertxSolrQuery vertxSolrQuery = new VertxSolrQuery();
        vertxSolrQuery.setQuery("start_date:*").setRows(25);

        readJsonStream = new OffsetJsonReadStream(solrService);
        readJsonStream.solrQuery(vertxSolrQuery)
                .exceptionHandler(exceptionHandler)
                .endHandler(endHandler);

        response = new JsonObject();
        result = new JsonObject().put("response", response);

        when(jsonResult.succeeded()).thenReturn(true);
        when(jsonResult.result()).thenReturn(result);
    }

    @Test
    public void testDataHandler_doQuery_okStatus() {

        readJsonStream.handler(dataHandler);
        response
                .put("numFound", 50)
                .put("next_cursor_mark", "test_cursor")
                .put("docs", new JsonArray()
                        .add(new JsonObject()
                                .put("test_session_id", "60e8a540-1323-11e4-93d8-e3c7cf74f423")
                                .put("status", "started")
                                .put("start_date", "1406200362078"))
                        .add(new JsonObject()
                                .put("test_session_id", "9023udj2-1312-1ld1-lvd3-989fu1dmnsfm")
                                .put("status", "created")
                                .put("start_date", "1407337289472")));
        verify(solrService).query(any(VertxSolrQuery.class), any(QueryOptions.class), resultHandlerCaptor.capture());
        verifyZeroInteractions(exceptionHandler);
        // endHandler should only be called if there are no more results to paginate through
        verifyZeroInteractions(endHandler);

    }

    @Test
    public void testDataHandler_doQuery_badStatus() {

        readJsonStream.handler(dataHandler);
        when(jsonResult.succeeded()).thenReturn(false);

        response.put("message", "defaultMessage");

        verify(solrService).query(any(VertxSolrQuery.class), any(QueryOptions.class), resultHandlerCaptor.capture());
        resultHandlerCaptor.getValue().handle(jsonResult);

        verify(exceptionHandler).handle(any(Throwable.class));
        verifyZeroInteractions(dataHandler);
        verifyZeroInteractions(endHandler);

    }

    @Test
    public void testDataHandler_doQuery_nullExceptionHandler() {

        readJsonStream.handler(dataHandler);
        readJsonStream.exceptionHandler(null);
        when(jsonResult.succeeded()).thenReturn(false);

        response.put("message", "defaultMessage");

        verify(solrService).query(any(VertxSolrQuery.class), any(QueryOptions.class), resultHandlerCaptor.capture());
        resultHandlerCaptor.getValue().handle(jsonResult);

        // with no exceptionHandler, the exception cannot be handled, but it will be logged
        // However, mocking static classes is a limitation of mockito, so we can't check for it.
        verifyZeroInteractions(exceptionHandler);
        verifyZeroInteractions(dataHandler);
        verifyZeroInteractions(endHandler);

    }

    @Test
    public void testDataHandler_doQuery_nullDataHandler() {

        readJsonStream.handler(null);

        response
                .put("numFound", 3)
                .put("next_cursor_mark", "test_cursor2")
                .put("docs", new JsonArray()
                        .add(new JsonObject()
                                .put("test_session_id", "9023udj2-1312-1ld1-lvd3-989fu1dmnsfm")
                                .put("status", "created")
                                .put("start_date", "1407337289472")));

        verifyZeroInteractions(dataHandler);
        verifyZeroInteractions(endHandler);

    }

    @Test
    public void testDataHandler_DoQuery_nullEndHandler() {

        readJsonStream.handler(dataHandler);
        readJsonStream.endHandler(null);

        response
                .put("numFound", 3)
                .put("next_cursor_mark", "test_cursor")
                .put("docs", new JsonArray()
                        .add(new JsonObject()
                                .put("test_session_id", "9023udj2-1312-1ld1-lvd3-989fu1dmnsfm")
                                .put("status", "created")
                                .put("start_date", "1407337289472")));

        verify(solrService).query(any(VertxSolrQuery.class), any(QueryOptions.class), resultHandlerCaptor.capture());
        resultHandlerCaptor.getValue().handle(jsonResult);

        // we are not throwing any exceptions if the endHandler is null - passing in an endHandler is optional
        verifyZeroInteractions(exceptionHandler);
        verify(dataHandler).handle(result);
        verifyZeroInteractions(endHandler);

    }

    @Test
    public void testEndHandler_emptyDocs() {

        // this test should test for the last page of results
        readJsonStream.handler(dataHandler);
        response
                // pretending we have more results than we do in order to trigger the 2nd loop of doQuery
                .put("numFound", 4)
                .put("next_cursor_mark", "test_cursor")
                        // empty array, which should trigger the endHandler
                .put("docs", new JsonArray()
                        .add(new JsonObject()
                                .put("test_session_id", "60e8a540-1323-11e4-93d8-e3c7cf74f423")
                                .put("status", "started")
                                .put("start_date", "1406200362078"))
                        .add(new JsonObject()
                                .put("test_session_id", "9023udj2-1312-1ld1-lvd3-989fu1dmnsfm")
                                .put("status", "created")
                                .put("start_date", "1407337289472")));

        // test with one result set that returns results
        verify(solrService).query(any(VertxSolrQuery.class), any(QueryOptions.class), resultHandlerCaptor.capture());
        resultHandlerCaptor.getValue().handle(jsonResult);
        verify(dataHandler).handle(result);
        verifyZeroInteractions(exceptionHandler);
        verifyZeroInteractions(endHandler);

        // update test_cursor
        response
                .put("next_cursor_mark", "test_cursor2")
                .put("docs", new JsonArray()
                        .add(new JsonObject()
                                .put("test_session_id", "9023udj2-1312-1ld1-lvd3-989fu1dmnsfm")
                                .put("status", "created")
                                .put("start_date", "1407337289472")));
        resultHandlerCaptor.getValue().handle(jsonResult);

        verifyZeroInteractions(exceptionHandler);
        verifyZeroInteractions(endHandler);

        // empty "docs" array, which should trigger the endHandler
        response
                .put("next_cursor_mark", "test_cursor2")
                .put("docs", new JsonArray());
        resultHandlerCaptor.getValue().handle(jsonResult);

        verifyZeroInteractions(exceptionHandler);
        verify(endHandler).handle(null);

    }

    @Test
    public void testEndHandler_noResults() {

        // this test should test for the last page of results
        readJsonStream.handler(dataHandler);
        // 0 results found, which should trigger the endHandler
        response
                .put("numFound", 0)
                .put("next_cursor_mark", "*")
                .put("docs", new JsonArray());

        // test with one result set that returns results
        verify(solrService).query(any(VertxSolrQuery.class), any(QueryOptions.class), resultHandlerCaptor.capture());
        resultHandlerCaptor.getValue().handle(jsonResult);
        // should still handle writing a response with nothing found
        verify(dataHandler).handle(result);
        verifyZeroInteractions(exceptionHandler);

        verify(endHandler).handle(null);

    }

    @Test
    public void testPause() {

        // first query will go through unless we pause it first
        readJsonStream.pause();
        readJsonStream.handler(dataHandler);
        verifyZeroInteractions(solrService);
        verifyZeroInteractions(endHandler);
        verifyZeroInteractions(exceptionHandler);
        verifyZeroInteractions(dataHandler);

    }

    @Test
    public void testResume() {

        response
                .put("numFound", 3)
                .put("next_cursor_mark", "test_cursor2")
                .put("docs", new JsonArray()
                        .add(new JsonObject()
                                .put("test_session_id", "9023udj2-1312-1ld1-lvd3-989fu1dmnsfm")
                                .put("status", "created")
                                .put("start_date", "1407337289472")));

        readJsonStream.pause();
        readJsonStream.handler(dataHandler);
        verifyZeroInteractions(endHandler);
        verifyZeroInteractions(dataHandler);
        verifyZeroInteractions(exceptionHandler);

        // on resume, it should go through another doQuery loop
        readJsonStream.resume();
        verify(solrService).query(any(VertxSolrQuery.class), any(QueryOptions.class), resultHandlerCaptor.capture());
        resultHandlerCaptor.getValue().handle(jsonResult);

        verifyZeroInteractions(exceptionHandler);
        verify(dataHandler).handle(result);

    }
}