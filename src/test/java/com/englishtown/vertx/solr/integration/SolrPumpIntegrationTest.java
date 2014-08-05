package com.englishtown.vertx.solr.integration;

import com.englishtown.vertx.solr.SolrQuerySerializer;
import com.englishtown.vertx.solr.impl.DefaultSolrQuerySerializer;
import com.englishtown.vertx.solr.streams.impl.OffsetReadJsonStream;
import com.englishtown.vertx.solr.streams.impl.SolrPump;
import com.englishtown.vertx.solr.streams.impl.WriteJsonStreamBase;
import org.apache.solr.client.solrj.SolrQuery;
import org.junit.Test;
import org.vertx.java.core.Handler;
import org.vertx.java.core.eventbus.EventBus;
import org.vertx.java.core.json.JsonArray;
import org.vertx.java.core.json.JsonObject;

import java.util.ArrayList;
import java.util.List;

import static org.vertx.testtools.VertxAssert.*;

/**
 * Integration test for the SolrPump
 */
public class SolrPumpIntegrationTest extends SolrIntegrationTestBase {

    SolrPump solrPump;
    final List<JsonObject> results = new ArrayList<>();

    OffsetReadJsonStream offsetReadJsonStream;
    WriteJsonStreamBase writeJsonStreamBase;

    EventBus eventBus;
    SolrQuery query;
    SolrQuerySerializer serializer;

    private void initJsonStreams(final Runnable onEnd, final Runnable onWrite) {

        this.eventBus = vertx.eventBus();
        this.query = new SolrQuery();
        this.serializer = new DefaultSolrQuerySerializer();

        writeJsonStreamBase = new WriteJsonStreamBase() {
            @Override
            public WriteJsonStreamBase write(JsonObject jsonObject) {
                results.add(jsonObject);
                if (onWrite != null) {
                    onWrite.run();
                }
                return this;
            }

            @Override
            public WriteJsonStreamBase setWriteQueueMaxSize(int maxSize) {
                return this;
            }

            @Override
            public boolean writeQueueFull() {
                return false;
            }

            @Override
            public WriteJsonStreamBase handleEnd() {

                assertTrue(solrPump.objectsPumped() > 0);
                assertTrue(results.size() > 0);
                JsonObject result = results.get(0);
                assertNotNull(result);

                onEnd.run();
                testComplete();
                return this;
            }
        }.exceptionHandler(new Handler<Throwable>() {
            @Override
            public void handle(Throwable t) {
                // it won't come in here unless it actually needs to throw an exception, but it does set the exception handler in case
                // it does need to through an exception.
                handleThrowable(t);
                fail();
            }
        });

        offsetReadJsonStream = new OffsetReadJsonStream()
                .endHandler(new Handler<Void>() {
                    @Override
                    public void handle(Void aVoid) {
                        writeJsonStreamBase.handleEnd();
                    }
                })
                .exceptionHandler(new Handler<Throwable>() {
                    @Override
                    public void handle(Throwable t) {
                        handleThrowable(t);
                        fail();
                    }
                }).eventBus(eventBus).serializer(serializer).solrQuery(query);

    }

    @Test
    public void testSolrPump_Start_Stop() {

        initJsonStreams(new Runnable() {
                            @Override
                            public void run() {
                                for (JsonObject result : results) {
                                    // check each result for a docs array and ensure number_found is greater than 0
                                    JsonArray docs = result.getArray("docs");
                                    assertNotNull(docs);
                                    Integer numberFound = result.getNumber("number_found").intValue();
                                    assertTrue(numberFound > 0);
                                }
                            }
                        },
                null);

        query.setQuery("name:*").setRows(5);

        solrPump = SolrPump.createPump(offsetReadJsonStream, writeJsonStreamBase);
        // start the pump, which initializes the dataHandler
        solrPump.start();
        // stop and start it again
        solrPump.stop();
        solrPump.start();

    }

    @Test
    public void testSolrPump_Pause_Resume() {

        initJsonStreams(new Runnable() {
            @Override
            public void run() {
                for (JsonObject result : results) {
                    // check each result for a docs array and ensure number_found is greater than 0
                    JsonArray docs = result.getArray("docs");
                    assertNotNull(docs);
                    Integer numberFound = result.getNumber("number_found").intValue();
                    assertTrue(numberFound > 0);
                }
            }
        }, new Runnable() {
            @Override
            public void run() {
                vertx.runOnContext(new Handler<Void>() {
                    @Override
                    public void handle(Void event) {
                        offsetReadJsonStream.resume();
                    }
                });
            }
        });

        query.setQuery("name:*").setRows(5);

        solrPump = SolrPump.createPump(offsetReadJsonStream, writeJsonStreamBase);
        solrPump.start();
        // try multiple pauses/resumes
        offsetReadJsonStream.pause();
        offsetReadJsonStream.resume();
        offsetReadJsonStream.pause();
        offsetReadJsonStream.resume();
        offsetReadJsonStream.pause();

    }

}
