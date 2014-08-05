package com.englishtown.vertx.solr;

import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.vertx.java.core.Vertx;
import org.vertx.java.core.eventbus.EventBus;
import org.vertx.java.core.eventbus.Message;
import org.vertx.java.core.json.JsonObject;
import org.vertx.java.core.logging.Logger;
import org.vertx.java.platform.Container;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class SolrVerticleTest {

    SolrVerticle verticle;
    JsonObject config = new JsonObject();
    JsonObject body = new JsonObject();

    @Mock
    Vertx vertx;
    @Mock
    Container container;
    @Mock
    EventBus eventBus;
    @Mock
    Logger logger;
    @Mock
    SolrConfigurator configurator;
    @Mock
    SolrQuerySerializer serializer;
    @Mock
    SolrServer solrServer;
    @Mock
    Message<JsonObject> message;
    @Captor
    ArgumentCaptor<JsonObject> jsonCaptor;

    @Before
    public void setUp() throws Exception {

        when(vertx.eventBus()).thenReturn(eventBus);
        when(container.config()).thenReturn(config);
        when(container.logger()).thenReturn(logger);
        when(configurator.createSolrServer()).thenReturn(solrServer);
        when(message.body()).thenReturn(body);

        verticle = new SolrVerticle(configurator, serializer);

        verticle.setVertx(vertx);
        verticle.setContainer(container);

        verticle.start();

    }

    @Test
    public void testStart() throws Exception {
        // start() is called from setUp
        // just verify the event bus handler was registered
        verify(eventBus).registerHandler(eq(SolrVerticle.DEFAULT_ADDRESS), eq(verticle));
    }

    @Test
    public void testStop() throws Exception {

        verticle.stop();
        verify(eventBus).unregisterHandler(eq(SolrVerticle.DEFAULT_ADDRESS), eq(verticle));

    }

    @Test
    public void testHandle_Action_Missing() throws Exception {

        verticle.handle(message);

        verify(message).reply(jsonCaptor.capture());
        JsonObject reply = jsonCaptor.getValue();

        assertNotNull(reply);
        assertEquals("error", reply.getString("status"));
        assertEquals("action must be specified", reply.getString("message"));

    }

    @Test
    public void testHandle_Action_Invalid() throws Exception {

        body.putString(SolrVerticle.FIELD_ACTION, "invalid");

        verticle.handle(message);

        verify(message).reply(jsonCaptor.capture());
        JsonObject reply = jsonCaptor.getValue();

        assertNotNull(reply);
        assertEquals("error", reply.getString("status"));
        assertEquals("Action 'invalid' is not supported", reply.getString("message"));

    }

    @Test
    public void testHandle_Action_Query() throws Exception {

        body.putString(SolrVerticle.FIELD_ACTION, "query");

        verticle.handle(message);

        verify(message).reply(jsonCaptor.capture());
        JsonObject reply = jsonCaptor.getValue();

        assertNotNull(reply);
        assertEquals("error", reply.getString("status"));
        assertEquals("query must be specified", reply.getString("message"));

    }

    @Test
    public void testDoQuery() throws Exception {

        SolrQuery solrQuery = mock(SolrQuery.class);
        QueryResponse queryResponse = mock(QueryResponse.class);
        SolrDocumentList results = new SolrDocumentList();
        SolrDocument result = new SolrDocument();
        result.setField("prop1", "val");

        results.add(result);
        results.add(result);

        when(serializer.deserialize(any(JsonObject.class))).thenReturn(solrQuery);
        when(solrServer.query(any(SolrQuery.class))).thenReturn(queryResponse);
        when(queryResponse.getResults()).thenReturn(results);

        JsonObject query = new JsonObject();
        body.putObject(SolrVerticle.FIELD_QUERY, query);


        verticle.doQuery(message);

        verify(serializer).deserialize(eq(query));
        verify(solrServer).query(eq(solrQuery));
        verify(queryResponse).getResults();

        verify(message).reply(jsonCaptor.capture());
        JsonObject reply = jsonCaptor.getValue();

        assertEquals("ok", reply.getString("status"));

    }
}