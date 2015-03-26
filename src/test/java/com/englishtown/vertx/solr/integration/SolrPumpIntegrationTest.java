package com.englishtown.vertx.solr.integration;

import com.englishtown.vertx.solr.VertxSolrQuery;
import com.englishtown.vertx.solr.streams.impl.OffsetJsonReadStream;
import com.englishtown.vertx.solr.streams.impl.SolrPump;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.core.streams.WriteStream;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;


/**
 * Integration test for the SolrPump
 */
public class SolrPumpIntegrationTest extends SolrIntegrationTestBase {

    SolrPump solrPump;
    final List<JsonObject> results = new ArrayList<>();

    OffsetJsonReadStream offsetReadJsonStream;
    WriteStream<JsonObject> writeJsonStreamBase;

    VertxSolrQuery query;

    private void initJsonStreams(final Runnable onEnd, final Runnable onWrite) {

        this.query = new VertxSolrQuery();

        writeJsonStreamBase = new WriteStream<JsonObject>() {

            @Override
            public WriteStream<JsonObject> exceptionHandler(Handler<Throwable> handler) {
                return this;
            }

            @Override
            public WriteStream<JsonObject> write(JsonObject data) {
                results.add(data);
                if (onWrite != null) {
                    onWrite.run();
                }
                return this;
            }

            @Override
            public WriteStream<JsonObject> setWriteQueueMaxSize(int maxSize) {
                return this;
            }

            @Override
            public boolean writeQueueFull() {
                return false;
            }

            @Override
            public WriteStream<JsonObject> drainHandler(Handler<Void> handler) {
                return null;
            }
        };

        offsetReadJsonStream = new OffsetJsonReadStream(proxyService);
        offsetReadJsonStream
                .endHandler(aVoid -> {

                    assertTrue(solrPump.objectsPumped() > 0);
                    assertTrue(results.size() > 0);
                    JsonObject result = results.get(0);
                    assertNotNull(result);

                    onEnd.run();
                    testComplete();

                })
                .exceptionHandler(this::handleThrowable)
                .queryOptions(queryOptions)
                .solrQuery(query);

    }

    @Test
    public void testSolrPump_Start_Stop() {

        initJsonStreams(
                () -> {
                    for (JsonObject result : results) {
                        // check each result for a docs array and ensure number_found is greater than 0
                        JsonObject response = result.getJsonObject("response");
                        JsonArray docs = response.getJsonArray("docs");
                        assertNotNull(docs);
                        Integer numberFound = response.getInteger("numFound");
                        assertTrue(numberFound > 0);
                    }
                },
                null);

        query.setQuery("*:*").setRows(50);

        solrPump = SolrPump.createPump(offsetReadJsonStream, writeJsonStreamBase);
        // start the pump, which initializes the dataHandler
        solrPump.start();
        // stop and start it again
        solrPump.stop();
        solrPump.start();

        await();
    }

    @Test
    public void testSolrPump_Pause_Resume() {

        initJsonStreams(
                () -> {
                    for (JsonObject result : results) {
                        // check each result for a docs array and ensure number_found is greater than 0
                        JsonObject response = result.getJsonObject("response");
                        JsonArray docs = response.getJsonArray("docs");
                        assertNotNull(docs);
                        Integer numberFound = response.getInteger("numFound");
                        assertTrue(numberFound > 0);
                    }
                },
                () -> vertx.runOnContext(event -> offsetReadJsonStream.resume()));

        query.setQuery("*:*").setRows(50);

        solrPump = SolrPump.createPump(offsetReadJsonStream, writeJsonStreamBase);
        solrPump.start();
        // try multiple pauses/resumes
        offsetReadJsonStream.pause();
        offsetReadJsonStream.resume();
        offsetReadJsonStream.pause();
        offsetReadJsonStream.resume();
        offsetReadJsonStream.pause();

        await();
    }

    @Test
    public void testSolrPump_Integrated() {

        initJsonStreams(
                () -> {
                    for (JsonObject result : results) {
                        // check each result for a docs array and ensure number_found is greater than 0
                        JsonObject response = result.getJsonObject("response");
                        JsonArray docs = response.getJsonArray("docs");
                        assertNotNull(docs);
                        Integer numberFound = response.getInteger("numFound");
                        assertTrue(numberFound > 0);
                    }
                },
                null);

        query.setQuery("*:*").setRows(50);

        solrPump = SolrPump.createPump(offsetReadJsonStream, writeJsonStreamBase);
        // start the pump, which initializes the dataHandler
        solrPump.start();
        // stop and start it again
        solrPump.stop();
        solrPump.start();

        await();
    }

}
