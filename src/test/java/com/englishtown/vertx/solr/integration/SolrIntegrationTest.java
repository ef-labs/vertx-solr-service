package com.englishtown.vertx.solr.integration;

import com.englishtown.vertx.solr.SolrQuerySerializer;
import com.englishtown.vertx.solr.SolrVerticle;
import com.englishtown.vertx.solr.impl.DefaultSolrQuerySerializer;
import com.englishtown.vertx.solr.streams.impl.AbstractWriteJsonStream;
import com.englishtown.vertx.solr.streams.impl.OffsetReadJsonStream;
import com.englishtown.vertx.solr.streams.impl.SolrPump;
import org.apache.solr.client.solrj.SolrQuery;
import org.junit.Test;
import org.vertx.java.core.AsyncResult;
import org.vertx.java.core.Future;
import org.vertx.java.core.Handler;
import org.vertx.java.core.eventbus.EventBus;
import org.vertx.java.core.eventbus.Message;
import org.vertx.java.core.json.JsonObject;
import org.vertx.testtools.TestVerticle;

import java.util.ArrayList;
import java.util.List;

import static org.vertx.testtools.VertxAssert.*;

/**
 * Integration tests for {@link com.englishtown.vertx.solr.SolrVerticle}
 */
public class SolrIntegrationTest extends TestVerticle {

    private final SolrQuerySerializer serializer = new DefaultSolrQuerySerializer();

    OffsetReadJsonStream defaultReadJsonStream;
    AbstractWriteJsonStream abstractWriteJsonStream;
    SolrPump solrPump;

    final String address = SolrVerticle.DEFAULT_ADDRESS;
    SolrQuery query = new SolrQuery();


    /**
     * {@inheritDoc}
     */
    @Override
    public void start(final Future<Void> startedResult) {

//        JsonObject config = new JsonObject()
//                .putString("hk2_binder", "com.englishtown.vertx.solr.integration.TestBinder");

        JsonObject config = new JsonObject()
                .putString("hk2_binder", "com.englishtown.vertx.solr.hk2.SolrBinder")
                .putString("server_url", "http://10.43.41.252:8983/solr/athena.test_session");

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

                String status = reply.body().getString("status");
                assertEquals("ok", status);
                testComplete();

            }
        });

    }

    @Test
    public void testSolrPump() {

        EventBus eventBus = vertx.eventBus();

        query.setQuery("start_date:*").setRows(5);

        final List<JsonObject> results = new ArrayList<>();

        abstractWriteJsonStream = new AbstractWriteJsonStream() {
            @Override
            public AbstractWriteJsonStream write(JsonObject jsonObject) {
                results.add(jsonObject);
                return this;
            }

            @Override
            public AbstractWriteJsonStream setWriteQueueMaxSize(int maxSize) {
                return this;
            }

            @Override
            public boolean writeQueueFull() {
                return false;
            }

            @Override
            public AbstractWriteJsonStream handleEnd() {

                assertTrue(solrPump.objectsPumped() > 0);

                assertTrue(results.size() > 0);
                JsonObject result = results.get(0);
                assertNotNull(result);

                testComplete();
                return this;
            }
        }.exceptionHandler(new Handler<Throwable>() {
            @Override
            public void handle(Throwable t) {
                handleThrowable(t);
                fail();
            }
        });

        defaultReadJsonStream = new OffsetReadJsonStream(query, serializer, eventBus, address)
                .endHandler(new Handler<Void>() {
                    @Override
                    public void handle(Void aVoid) {
                        abstractWriteJsonStream.handleEnd();
                    }
                })
                .exceptionHandler(new Handler<Throwable>() {
                    @Override
                    public void handle(Throwable t) {
                        handleThrowable(t);
                        fail();
                    }
                });

        solrPump = SolrPump.createPump(defaultReadJsonStream, abstractWriteJsonStream);
        solrPump.start();
        // TODO check the eventBus message and returned query are what we expect
        // there is nothing returned from this query.

    }

}
