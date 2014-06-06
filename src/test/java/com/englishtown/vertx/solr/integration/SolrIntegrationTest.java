package com.englishtown.vertx.solr.integration;

import com.englishtown.vertx.solr.SolrQuerySerializer;
import com.englishtown.vertx.solr.SolrVerticle;
import com.englishtown.vertx.solr.impl.DefaultSolrQuerySerializer;
import org.apache.solr.client.solrj.SolrQuery;
import org.vertx.java.core.AsyncResult;
import org.vertx.java.core.Future;
import org.vertx.java.core.Handler;
import org.vertx.java.core.eventbus.Message;
import org.vertx.java.core.json.JsonObject;
import org.vertx.testtools.TestVerticle;

import org.junit.Test;

import static org.vertx.testtools.VertxAssert.assertEquals;
import static org.vertx.testtools.VertxAssert.testComplete;

/**
 * Integration tests for {@link com.englishtown.vertx.solr.SolrVerticle}
 */
public class SolrIntegrationTest extends TestVerticle {

    private SolrQuerySerializer serializer = new DefaultSolrQuerySerializer();

    /**
     * {@inheritDoc}
     */
    @Override
    public void start(final Future<Void> startedResult) {

        JsonObject config = new JsonObject()
                .putString("hk2_binder", "com.englishtown.vertx.solr.integration.TestBinder");

        container.deployVerticle(SolrVerticle.class.getName(), config, new Handler<AsyncResult<String>>() {
            @Override
            public void handle(AsyncResult<String> result) {
                if (result.succeeded()) {
                    start();
                    startedResult.setResult(null);
                } else {
                    startedResult.setFailure(result.cause());
                }
            }
        });

    }

    @Test
    public void testSolrVerticle() {

        SolrQuery query = new SolrQuery()
                .setQuery("*:*");

        JsonObject message = new JsonObject()
                .putString("action", "query")
                .putObject(SolrVerticle.FIELD_QUERY, serializer.serialize(query));

        vertx.eventBus().send(SolrVerticle.DEFAULT_ADDRESS, message, new Handler<Message<JsonObject>>() {
            /**
             * Something has happened, so handle it.
             *
             * @param reply
             */
            @Override
            public void handle(Message<JsonObject> reply) {

                String status = reply.body().getString("status");
                assertEquals("ok", status);
                testComplete();

            }
        });

    }

}
