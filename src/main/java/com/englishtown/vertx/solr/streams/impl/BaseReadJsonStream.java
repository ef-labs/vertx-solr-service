package com.englishtown.vertx.solr.streams.impl;

import com.englishtown.vertx.solr.SolrQuerySerializer;
import com.englishtown.vertx.solr.SolrVerticle;
import com.englishtown.vertx.solr.streams.ReadJsonStream;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.common.params.ModifiableSolrParams;
import org.vertx.java.core.Handler;
import org.vertx.java.core.eventbus.EventBus;
import org.vertx.java.core.eventbus.Message;
import org.vertx.java.core.json.JsonObject;
import org.vertx.java.core.logging.Logger;
import org.vertx.java.core.logging.impl.LoggerFactory;

/**
 * Base implementation of {@link com.englishtown.vertx.solr.streams.ReadJsonStream}
 */
public abstract class BaseReadJsonStream implements ReadJsonStream<BaseReadJsonStream> {

    private static final Logger log = LoggerFactory.getLogger(BaseReadJsonStream.class);

    private final SolrQuery query;
    private final SolrQuerySerializer serializer;
    private final EventBus eventBus;
    private final String address;

    private Handler<Void> endHandler;
    private Handler<JsonObject> dataHandler;
    private Handler<Throwable> exceptionHandler;
    private boolean paused;

    public BaseReadJsonStream(SolrQuery query, SolrQuerySerializer serializer, EventBus eventBus, String address) {
        this.query = query;
        this.serializer = serializer;
        this.eventBus = eventBus;
        this.address = address;
    }

    @Override
    public BaseReadJsonStream endHandler(Handler<Void> endHandler) {
        this.endHandler = endHandler;
        return this;
    }

    // this method starts the SolrPump.
    // It takes a dataHandler, which is passed in from the Pump, sets this class's dataHandler equal to the one passed in
    // and starts the Solr query and begins running through the pagination loops
    @Override
    public BaseReadJsonStream dataHandler(Handler<JsonObject> handler) {
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
    public BaseReadJsonStream pause() {
        this.paused = true;
        return this;
    }

    @Override
    public BaseReadJsonStream resume() {
        if (paused) {
            paused = false;
            if (dataHandler != null) {
                doQuery();
            }
        }
        return this;
    }

    @Override
    public BaseReadJsonStream exceptionHandler(Handler<Throwable> handler) {
        this.exceptionHandler = handler;
        return this;
    }

    protected abstract ModifiableSolrParams setQueryStart(SolrQuery query);

    private void doQuery() {

        setQueryStart(query);

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
            log.error("Unhandled Exception", t);
        }
    }

    protected abstract boolean isComplete(JsonObject reply);

    // pagination looping logic - continuously runs through a doQuery loop until all results have been streamed
    private void handleReply(JsonObject reply) {

        if (dataHandler != null) {
            dataHandler.handle(reply);
        }

        if (isComplete(reply)) {
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