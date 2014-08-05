package com.englishtown.vertx.solr.integration;

import com.englishtown.vertx.solr.SolrVerticle;
import org.apache.solr.client.solrj.SolrQuery;
import org.junit.Test;
import org.vertx.java.core.Handler;
import org.vertx.java.core.eventbus.Message;
import org.vertx.java.core.json.JsonArray;
import org.vertx.java.core.json.JsonObject;

import static org.vertx.testtools.VertxAssert.*;

/**
 * Integration tests for {@link com.englishtown.vertx.solr.SolrVerticle}
 */
public class SolrIntegrationTest extends SolrIntegrationTestBase {

    @Test
    public void testSolrVerticle() {

        SolrQuery query = new SolrQuery()
                .setQuery("*:*");

        JsonObject message = new JsonObject()
                .putString(SolrVerticle.FIELD_ACTION, SolrVerticle.FIELD_QUERY)
                .putObject(SolrVerticle.FIELD_QUERY, serializer.serialize(query));

        vertx.eventBus().send(SolrVerticle.DEFAULT_ADDRESS, message, new Handler<Message<JsonObject>>() {
            /**
             * Something has happened, so handle it.
             *
             * @param reply
             * The json reply from Solr
             */
            @Override
            public void handle(Message<JsonObject> reply) {

                JsonObject body = reply.body();
                String status = body.getString("status");
                assertEquals("ok", status);

                Integer numberFound = body.getInteger("number_found");
                Integer start = body.getInteger("start");
                JsonArray docs = body.getArray("docs");

                assertNotNull(numberFound);
                assertNotNull(start);
                assertNotNull(docs);

                assertEquals(0, start.intValue());
                assertTrue(numberFound.intValue() > 0);
                assertTrue(docs.size() > 0);

                testComplete();

            }
        });

    }

}
