package com.englishtown.vertx.solr.streams.impl;

import com.englishtown.vertx.solr.SolrQuerySerializer;
import com.englishtown.vertx.solr.SolrVerticle;
import com.englishtown.vertx.solr.streams.ReadJsonStream;
import org.apache.solr.client.solrj.SolrQuery;
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

    private SolrQuery query;
    private SolrQuerySerializer serializer;
    private EventBus eventBus;
    private String address = SolrVerticle.DEFAULT_ADDRESS;

    private Handler<Void> endHandler;
    private Handler<JsonObject> dataHandler;
    private Handler<Throwable> exceptionHandler;

    private boolean queryRunning;
    private boolean paused;

    protected ReadJsonStreamBase() {}

    /**
     * @param query SolrQuery object which holds the query data
     * @return Returns this
     */
    @SuppressWarnings("unchecked")
    public T solrQuery(SolrQuery query) {
        this.query = query;
        return (T) this;
    }

    /**
     * @param serializer SolrQuerySerializer which helps to serialize/deserialize SolrQuery objects
     * @return Returns this
     */
    @SuppressWarnings("unchecked")
    public T serializer(SolrQuerySerializer serializer) {
        this.serializer = serializer;
        return (T) this;
    }

    /**
     * @param eventBus Vert.x eventBus
     * @return Returns this
     */
    @SuppressWarnings("unchecked")
    public T eventBus(EventBus eventBus) {
        this.eventBus = eventBus;
        return (T) this;
    }

    /**
     * @param address address the read and write streams will communicate with on over the Vert.x eventBus
     * @return Returns this
     */
    @SuppressWarnings("unchecked")
    public T address(String address) {
        this.address = address;
        return (T) this;
    }

    /**
     * @param endHandler Handler called when the pump has finished
     * @return Returns this
     */
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
     * @param handler Handler responsible for writing data to the stream
     * @return Returns this
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
     * @param query SolrQuery object
     */

    protected abstract void setQueryStart(SolrQuery query);

    private void doQuery() {

        // Don't send a second query if already running
        if (queryRunning) {
            return;
        }

        queryRunning = true;

        setQueryStart(query);

        // send Solr a "query" json object with the query and attributes to signify this is a Solr query.
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
     * @param reply JsonObject query reply from Solr
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