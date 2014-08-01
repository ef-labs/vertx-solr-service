package com.englishtown.vertx.solr.streams.impl;

import com.englishtown.vertx.solr.SolrQuerySerializer;
import com.englishtown.vertx.solr.SolrVerticle;
import com.englishtown.vertx.solr.logger.SolrLogger;
import com.englishtown.vertx.solr.logger.SolrLoggerFactory;
import com.englishtown.vertx.solr.streams.ReadJsonStream;
import org.apache.solr.client.solrj.SolrQuery;
import org.vertx.java.core.Handler;
import org.vertx.java.core.eventbus.EventBus;
import org.vertx.java.core.eventbus.Message;
import org.vertx.java.core.json.JsonObject;

/**
 * Default implementation of {@link com.englishtown.vertx.solr.streams.ReadJsonStream}
 */
public class OffsetReadJsonStream implements ReadJsonStream<OffsetReadJsonStream> {

    private static final SolrLogger logger = SolrLoggerFactory.getSolrLogger(OffsetReadJsonStream.class);

    private final SolrQuery query;
    private final SolrQuerySerializer serializer;
    private final EventBus eventBus;
    private final String address;

    private Handler<Void> endHandler;
    private Handler<JsonObject> dataHandler;
    private Handler<Throwable> exceptionHandler;
    private int nextOffset;
    private boolean paused;

    public OffsetReadJsonStream(SolrQuery query, SolrQuerySerializer serializer, EventBus eventBus, String address) {
        this.query = query;
        this.serializer = serializer;
        this.eventBus = eventBus;
        this.address = address;
        this.nextOffset = 0;
    }

    @Override
    public OffsetReadJsonStream endHandler(Handler<Void> endHandler) {
        this.endHandler = endHandler;
        return this;
    }

    // this method starts the SolrPump.
    // It takes a dataHandler, which is passed in from the Pump, sets this class's dataHandler equal to the one passed in
    // and starts the Solr query and begins running through the pagination loops
    @Override
    public OffsetReadJsonStream dataHandler(Handler<JsonObject> handler) {
        this.dataHandler = handler;
        if (!paused) {
            if (dataHandler != null) {
                doQuery();
            } else {
                // a dataHandler being null will most likely never happen
                handleException(new RuntimeException("No dataHandler defined!"));
            }
        }
        return this;
    }

    @Override
    public OffsetReadJsonStream pause() {
        this.paused = true;
        return this;
    }

    @Override
    public OffsetReadJsonStream resume() {
        if (paused) {
            paused = false;
            if (dataHandler != null) {
                doQuery();
            }
        }
        return this;
    }

    @Override
    public OffsetReadJsonStream exceptionHandler(Handler<Throwable> handler) {
        this.exceptionHandler = handler;
        return this;
    }

    private void doQuery() {

        query.setStart(nextOffset);

        // send Solr a "query" json object with the query and attributes to signify this is a Solr query, which basically says:
        // "Here is the action. It is a query. Also, here is the query"
        JsonObject message = new JsonObject()
                .putString(SolrVerticle.FIELD_ACTION, SolrVerticle.FIELD_QUERY)
                .putObject(SolrVerticle.FIELD_QUERY, serializer.serialize(query));

        // we send this functional json message along the event bus which tells it how to handle the reply.
        eventBus.send(address, message, new Handler<Message<JsonObject>>() {
            @Override
            public void handle(Message<JsonObject> reply) {

                try {
                    JsonObject body = reply.body();

                    if ("ok".equalsIgnoreCase(body.getString("status"))) {
                        // if the status is ok from Solr, handle the reply.
                        handleReply(body);
                    } else {
                        handleException(new RuntimeException("Solr event bus query failed: " + body.getString("message")));
                    }

                } catch (Throwable t) {
                    handleException(t);
                }
            }
        });

    }

    private void handleException(Throwable t) {
        if (exceptionHandler != null) {
            exceptionHandler.handle(t);
        } else {
            logger.error("Unhandled Exception", t);
        }
    }

    // pagination looping logic - continuously runs through a doQuery loop until all results have been streamed
    private void handleReply(JsonObject reply) {

        int count;
        int numberFound;
        if (reply.containsField("docs") && reply.containsField("number_found")) {
            count = reply.getArray("docs").size();
            numberFound = reply.getInteger("number_found");

            nextOffset += count;

            if (dataHandler != null) {
                dataHandler.handle(reply);
            }

            if (numberFound == nextOffset || count == 0) {
                if (endHandler != null) {
                    endHandler.handle(null);
                }
            } else {
                if (!paused && dataHandler != null) {
                    doQuery();
                }
            }

        }
        else {
            handleException(new RuntimeException("Invalid Json: " + reply.toString()));
        }

    }

}
