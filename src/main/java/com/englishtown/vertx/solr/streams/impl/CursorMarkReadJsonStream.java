package com.englishtown.vertx.solr.streams.impl;

import com.englishtown.vertx.solr.SolrQuerySerializer;
import com.englishtown.vertx.solr.SolrVerticle;
import com.englishtown.vertx.solr.streams.ReadJsonStream;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.common.params.CursorMarkParams;
import org.vertx.java.core.Handler;
import org.vertx.java.core.eventbus.EventBus;
import org.vertx.java.core.eventbus.Message;
import org.vertx.java.core.json.JsonObject;

/**
 * Default implementation of {@link com.englishtown.vertx.solr.streams.ReadJsonStream}
 */
public class CursorMarkReadJsonStream implements ReadJsonStream<CursorMarkReadJsonStream> {

    private final SolrQuery query;
    private final SolrQuerySerializer serializer;
    private final EventBus eventBus;
    private final String address;

    private Handler<Void> endHandler;
    private Handler<JsonObject> dataHandler;
    private Handler<Throwable> exceptionHandler;
    private String nextCursorMark;
    private boolean paused;

    public CursorMarkReadJsonStream(SolrQuery query, SolrQuerySerializer serializer, EventBus eventBus, String address) {
        this.query = query;
        this.serializer = serializer;
        this.eventBus = eventBus;
        this.address = address;
        this.nextCursorMark = CursorMarkParams.CURSOR_MARK_START;
    }

    @Override
    public CursorMarkReadJsonStream endHandler(Handler<Void> endHandler) {
        this.endHandler = endHandler;
        return this;
    }

    // this is the method which starts the SolrPump
    @Override
    public CursorMarkReadJsonStream dataHandler(Handler<JsonObject> handler) {
        this.dataHandler = handler;
        if (dataHandler != null && !paused) {
            doQuery();
        }
        return this;
    }

    @Override
    public CursorMarkReadJsonStream pause() {
        this.paused = true;
        return this;
    }

    @Override
    public CursorMarkReadJsonStream resume() {
        if (paused) {
            paused = false;
            if (dataHandler != null) {
                doQuery();
            }
        }
        return this;
    }

    @Override
    public CursorMarkReadJsonStream exceptionHandler(Handler<Throwable> handler) {
        this.exceptionHandler = handler;
        return this;
    }

    private void doQuery() {

        query.set(CursorMarkParams.CURSOR_MARK_PARAM, this.nextCursorMark);

        // send Solr a query json object with the query and attributes to signify this is a Solr query, which basically says:
        // "Here is the action. It is a query. Also, here is the query"
        JsonObject message = new JsonObject()
                .putString(SolrVerticle.FIELD_ACTION, SolrVerticle.FIELD_QUERY)
                .putObject(SolrVerticle.FIELD_QUERY, serializer.serialize(query));

        // we send this message along the event bus and also tell it how to handle the reply.
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
        }
        // TODO: Call endHandler on exception?
    }

    private void handleReply(JsonObject reply) {

        String currCursorMark = nextCursorMark;
        nextCursorMark = reply.getString("next_cursor_mark");

        if (dataHandler != null) {
            dataHandler.handle(reply);
        }

        if (nextCursorMark == null || currCursorMark.equalsIgnoreCase(nextCursorMark)) {
            if (endHandler != null) {
                endHandler.handle(null);
            }
        } else {
            if (!paused && dataHandler != null) {
                doQuery();
            }
        }

    }

}
