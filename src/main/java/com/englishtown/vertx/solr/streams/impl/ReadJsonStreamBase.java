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
public abstract class ReadJsonStreamBase<T extends ReadJsonStreamBase<T>> implements ReadJsonStream<T> {

    private static final Logger logger = LoggerFactory.getLogger(ReadJsonStreamBase.class);

    private final SolrQuery query;
    private final SolrQuerySerializer serializer;
    private final EventBus eventBus;
    private final String address;

    private Handler<Void> endHandler;
    private Handler<JsonObject> dataHandler;
    private Handler<Throwable> exceptionHandler;

    private boolean queryRunning;
    private boolean paused;

    protected ReadJsonStreamBase(SolrQuery query, SolrQuerySerializer serializer, EventBus eventBus, String address) {
        this.query = query;
        this.serializer = serializer;
        this.eventBus = eventBus;
        this.address = address;
    }

    @SuppressWarnings("unchecked")
    @Override
    public T endHandler(Handler<Void> endHandler) {
        this.endHandler = endHandler;
        return (T) this;
    }

    /**
     * This method starts the SolrPump.
     * It takes a dataHandler, which is passed in from the Pump, sets this class's dataHandler equal to the one passed in
     * and starts the Solr query and begins running through the pagination loops
     *
     * @param handler
     * @return
     */
    @SuppressWarnings("unchecked")
    @Override
    public T dataHandler(Handler<JsonObject> handler) {
        this.dataHandler = handler;
        if (!paused) {
            if (dataHandler != null) {
                doQuery();
            }
        }
        return (T) this;
    }

    @SuppressWarnings("unchecked")
    @Override
    public T pause() {
        this.paused = true;
        return (T) this;
    }

    @SuppressWarnings("unchecked")
    @Override
    public T resume() {
        if (paused) {
            paused = false;
            if (dataHandler != null) {
                doQuery();
            }
        }
        return (T) this;
    }

    @SuppressWarnings("unchecked")
    @Override
    public T exceptionHandler(Handler<Throwable> handler) {
        this.exceptionHandler = handler;
        return (T) this;
    }

    /**
     * Set the query start point for pagination.  Typically either the start or cursorMark param.
     *
     * @param query the SolrQuery object
     * @return
     */
    protected abstract ModifiableSolrParams setQueryStart(SolrQuery query);

    // doQuery never knows if something is paused, because it will never come in here if it is paused, yet the counter
    // will keep incrementing inside the override of handleReply. we need to make handleReply aware of the value of pause
    // or move the pause check somewhere else.
    private void doQuery() {

        // Don't send a second query if already running
        if (queryRunning) {
            return;
        }

        queryRunning = true;

        setQueryStart(query);

        // send Solr a "query" json object with the query and attributes to signify this is a Solr query, which basically says:
        // "Here is the action. It is a query. Also, here is the query"
        JsonObject message = new JsonObject()
                .putString(SolrVerticle.FIELD_ACTION, SolrVerticle.FIELD_QUERY)
                .putObject(SolrVerticle.FIELD_QUERY, serializer.serialize(query));

        // send this json message over the event bus with a reply handler.
        eventBus.send(address, message, new Handler<Message<JsonObject>>() {
            @Override
            public void handle(Message<JsonObject> reply) {

                try {
                    queryRunning = false;
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

    protected abstract boolean isComplete(JsonObject reply);

    /**
     * Pagination looping logic - continuously runs through a doQuery loop until all results have been streamed
     *
     * @param reply the JsonObject query reply from Solr
     */
    protected void handleReply(JsonObject reply) {

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