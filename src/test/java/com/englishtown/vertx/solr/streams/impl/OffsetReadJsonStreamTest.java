package com.englishtown.vertx.solr.streams.impl;

import com.englishtown.vertx.solr.SolrQuerySerializer;
import com.englishtown.vertx.solr.SolrVerticle;
import org.apache.solr.client.solrj.SolrQuery;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.vertx.java.core.Handler;
import org.vertx.java.core.eventbus.EventBus;
import org.vertx.java.core.eventbus.Message;
import org.vertx.java.core.json.JsonArray;
import org.vertx.java.core.json.JsonObject;

import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link com.englishtown.vertx.solr.streams.impl.SolrPump}
 */

@RunWith(MockitoJUnitRunner.class)
public class OffsetReadJsonStreamTest {

    OffsetReadJsonStream offsetReadJsonStream;
    String address = SolrVerticle.DEFAULT_ADDRESS;

    // contains the json query object we pass to Solr
    JsonObject queryMessage = new JsonObject();
    // put into Message<JsonObject> used to simulate various reply messages from Solr
    JsonObject messageBody = new JsonObject();

    @Mock
    EventBus eventBus;
    @Captor
    ArgumentCaptor<Handler<Message<JsonObject>>> replyHandlerCaptor;
    @Mock
    Handler<JsonObject> dataHandler;
    @Mock
    Message<JsonObject> jsonMessage; // message wrapper for the Solr json response
    @Mock
    SolrQuerySerializer serializer;
    @Mock
    Handler<Throwable> exceptionHandler;
    @Mock
    Handler<Void> endHandler;

    @Before
    public void setUp() throws Exception {

        SolrQuery query = new SolrQuery()
                .setQuery("start_date:*").setRows(25);

        offsetReadJsonStream = new OffsetReadJsonStream(query, serializer, eventBus, address);
        offsetReadJsonStream.exceptionHandler(exceptionHandler);
        offsetReadJsonStream.endHandler(endHandler);

        queryMessage = new JsonObject()
                .putString(SolrVerticle.FIELD_ACTION, SolrVerticle.FIELD_QUERY)
                .putObject(SolrVerticle.FIELD_QUERY, serializer.serialize(query));

        when(jsonMessage.body()).thenReturn(messageBody);
    }

    @Test
    public void testDataHandler_doQuery_okStatus() {

        offsetReadJsonStream.dataHandler(dataHandler);
        messageBody
                .putString("status", "ok")
                .putNumber("number_found", 50)
                .putArray("docs", new JsonArray()
                        .addObject(new JsonObject()
                                .putString("test_session_id", "60e8a540-1323-11e4-93d8-e3c7cf74f423")
                                .putString("status", "started")
                                .putString("start_date", "1406200362078"))
                        .addObject(new JsonObject()
                                .putString("test_session_id", "9023udj2-1312-1ld1-lvd3-989fu1dmnsfm")
                                .putString("status", "created")
                                .putString("start_date", "1407337289472")));
        verify(eventBus).send(eq(address), eq(queryMessage), replyHandlerCaptor.capture());
        replyHandlerCaptor.getValue().handle(jsonMessage);

        verifyZeroInteractions(exceptionHandler);
        // endHandler should only be called if there are no more results to paginate through
        verifyZeroInteractions(endHandler);

    }

    @Test
    public void testDataHandler_doQuery_badStatus() {

        offsetReadJsonStream.dataHandler(dataHandler);
        messageBody.putString("status", "bad")
                .putString("message", "defaultMessage");

        verify(eventBus).send(eq(address), eq(queryMessage), replyHandlerCaptor.capture());
        replyHandlerCaptor.getValue().handle(jsonMessage);

        verify(exceptionHandler).handle(any(Throwable.class));
        verifyZeroInteractions(dataHandler);
        verifyZeroInteractions(endHandler);

    }

    @Test
    public void testDataHandler_doQuery_nullExceptionHandler() {

        offsetReadJsonStream.dataHandler(dataHandler);
        offsetReadJsonStream.exceptionHandler(null);

        messageBody.putString("status", "bad")
                .putString("message", "default unit test message")
                .putNumber("number_found", 3)
                .putArray("docs", new JsonArray()
                        .addObject(new JsonObject()
                                .putString("test_session_id", "9023udj2-1312-1ld1-lvd3-989fu1dmnsfm")
                                .putString("status", "created")
                                .putString("start_date", "1407337289472")));

        verify(eventBus).send(eq(address), eq(queryMessage), replyHandlerCaptor.capture());
        replyHandlerCaptor.getValue().handle(jsonMessage);
        // with no exceptionHandler, the exception cannot be handled, but it will be logged
        // However, mocking static classes is a limitation of mockito, so we can't check for it.
        verifyZeroInteractions(exceptionHandler);
        verifyZeroInteractions(dataHandler);
        verifyZeroInteractions(endHandler);

    }

    @Test
    public void testDataHandler_doQuery_nullDataHandler() {

        offsetReadJsonStream.dataHandler(null);

        messageBody.putString("status", "ok")
                .putString("message", "default unit test message")
                .putNumber("number_found", 3)
                .putArray("docs", new JsonArray()
                        .addObject(new JsonObject()
                                .putString("test_session_id", "9023udj2-1312-1ld1-lvd3-989fu1dmnsfm")
                                .putString("status", "created")
                                .putString("start_date", "1407337289472")));

        // should be no calls to the event bus if we don't have a dataHandler
        verifyZeroInteractions(eventBus);
        verify(exceptionHandler).handle(any(Throwable.class));
        verifyZeroInteractions(dataHandler);
        verifyZeroInteractions(endHandler);

    }

    @Test
    public void testDataHandler_DoQuery_nullEndHandler() {

        offsetReadJsonStream.dataHandler(dataHandler);
        offsetReadJsonStream.endHandler(null);

        messageBody.putString("status", "ok")
                .putString("message", "default unit test message")
                .putNumber("number_found", 3)
                .putArray("docs", new JsonArray()
                        .addObject(new JsonObject()
                                .putString("test_session_id", "9023udj2-1312-1ld1-lvd3-989fu1dmnsfm")
                                .putString("status", "created")
                                .putString("start_date", "1407337289472")));

        verify(eventBus).send(eq(address), eq(queryMessage), replyHandlerCaptor.capture());
        replyHandlerCaptor.getValue().handle(jsonMessage);

        // we are not throwing any exceptions if the endHandler is null - passing in an endHandler is optional
        verifyZeroInteractions(exceptionHandler);
        verify(dataHandler).handle(messageBody);
        verifyZeroInteractions(endHandler);

    }

    @Test
    public void testEndHandler_emptyDocs() {

        // this test should test for the last page of results
        offsetReadJsonStream.dataHandler(dataHandler);
        messageBody
                .putString("status", "ok")
                        // pretending we have more results than we do in order to trigger the 2nd loop of doQuery
                .putNumber("number_found", 3)
                        // empty array, which should trigger the endHandler
                .putArray("docs", new JsonArray()
                        .addObject(new JsonObject()
                                .putString("test_session_id", "60e8a540-1323-11e4-93d8-e3c7cf74f423")
                                .putString("status", "started")
                                .putString("start_date", "1406200362078"))
                        .addObject(new JsonObject()
                                .putString("test_session_id", "9023udj2-1312-1ld1-lvd3-989fu1dmnsfm")
                                .putString("status", "created")
                                .putString("start_date", "1407337289472")));

        // test with one result set that returns results
        verify(eventBus).send(eq(address), eq(queryMessage), replyHandlerCaptor.capture());
        replyHandlerCaptor.getValue().handle(jsonMessage);
        verify(dataHandler).handle(messageBody);
        verifyZeroInteractions(exceptionHandler);
        verifyZeroInteractions(endHandler);

        // empty "docs" array, which should trigger the endHandler
        messageBody
                .putArray("docs", new JsonArray());

        replyHandlerCaptor.getValue().handle(jsonMessage);

        verifyZeroInteractions(exceptionHandler);
        verify(endHandler).handle(null);

    }

    @Test
    public void testEndHandler_noResults() {

        // this test should test for the last page of results
        offsetReadJsonStream.dataHandler(dataHandler);
        // 0 results found, which should trigger the endHandler
        messageBody
                .putString("status", "ok")
                .putNumber("number_found", 0)
                .putArray("docs", new JsonArray());

        // test with one result set that returns results
        verify(eventBus).send(eq(address), eq(queryMessage), replyHandlerCaptor.capture());
        replyHandlerCaptor.getValue().handle(jsonMessage);
        // should still handle writing a response with nothing found
        verify(dataHandler).handle(messageBody);
        verifyZeroInteractions(exceptionHandler);

        verify(endHandler).handle(null);

    }

    @Test
    public void testPause() {

        // first query will go through unless we pause it first
        offsetReadJsonStream.pause();
        offsetReadJsonStream.dataHandler(dataHandler);
        // verify we sent no messages across the event bus
        verifyZeroInteractions(eventBus);

        verifyZeroInteractions(endHandler);
        verifyZeroInteractions(exceptionHandler);
        verifyZeroInteractions(dataHandler);

    }

    @Test
    public void testResume() {

        messageBody.putString("status", "ok")
                .putString("message", "default unit test message")
                .putNumber("number_found", 3)
                .putArray("docs", new JsonArray()
                        .addObject(new JsonObject()
                                .putString("test_session_id", "9023udj2-1312-1ld1-lvd3-989fu1dmnsfm")
                                .putString("status", "created")
                                .putString("start_date", "1407337289472")));

        offsetReadJsonStream.pause();
        offsetReadJsonStream.dataHandler(dataHandler);
        verifyZeroInteractions(eventBus);
        verifyZeroInteractions(endHandler);
        verifyZeroInteractions(dataHandler);
        verifyZeroInteractions(exceptionHandler);

        // on resume, it should go through another doQuery loop
        offsetReadJsonStream.resume();
        verify(eventBus).send(eq(address), eq(queryMessage), replyHandlerCaptor.capture());
        replyHandlerCaptor.getValue().handle(jsonMessage);

        verifyZeroInteractions(exceptionHandler);
        verify(dataHandler).handle(messageBody);

    }

}
