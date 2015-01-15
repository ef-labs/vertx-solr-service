package com.englishtown.vertx.solr.streams.impl;

import com.englishtown.vertx.solr.QueryOptions;
import com.englishtown.vertx.solr.SolrService;
import com.englishtown.vertx.solr.VertxSolrQuery;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.impl.LoggerFactory;
import io.vertx.core.streams.ReadStream;
import org.apache.solr.client.solrj.SolrQuery;

import javax.inject.Inject;

/**
 * Base implementation of {@link io.vertx.core.json.JsonObject} {@link io.vertx.core.streams.ReadStream}
 */
public abstract class JsonReadStreamBase implements ReadStream<JsonObject> {

    private static final Logger logger = LoggerFactory.getLogger(JsonReadStreamBase.class);
    private final SolrService solrService;

    private VertxSolrQuery query;
    private QueryOptions options;

    private Handler<Void> endHandler;
    private Handler<JsonObject> dataHandler;
    private Handler<Throwable> exceptionHandler;

    private boolean queryRunning;
    private boolean paused;
    private boolean completed;

    @Inject
    protected JsonReadStreamBase(SolrService solrService) {
        this.solrService = solrService;
    }

    /**
     * This is a required parameter.
     *
     * @param query SolrQuery object which holds the query data
     * @return Returns this
     */
    public JsonReadStreamBase solrQuery(VertxSolrQuery query) {
        this.query = query;
        return this;
    }

    /**
     * Optional param
     *
     * @param options Optional query options
     * @return Returns this
     */
    public JsonReadStreamBase queryOptions(QueryOptions options) {
        this.options = options;
        return this;
    }

    /**
     * @param endHandler Handler called when the pump has finished
     * @return Returns this
     */
    @Override
    public JsonReadStreamBase endHandler(Handler<Void> endHandler) {
        this.endHandler = endHandler;
        return this;
    }

    /**
     * This method starts the SolrPump.
     * It takes a dataHandler, which is passed in from the Pump, sets this class's dataHandler equal to the one passed in
     * and starts the Solr query and begins running through the pagination loops
     *
     * @param handler Handler responsible for writing data to the stream
     * @return Returns this
     */
    @Override
    public JsonReadStreamBase handler(Handler<JsonObject> handler) {
        this.dataHandler = handler;
        if (!paused) {
            if (dataHandler != null) {
                doQuery();
            }
        }
        return this;
    }

    @Override
    public JsonReadStreamBase pause() {
        this.paused = true;
        return this;
    }

    @Override
    public JsonReadStreamBase resume() {
        if (paused) {
            paused = false;
            if (dataHandler != null) {
                doQuery();
            }
        }
        return this;
    }

    @Override
    public JsonReadStreamBase exceptionHandler(Handler<Throwable> handler) {
        this.exceptionHandler = handler;
        return this;
    }


    /**
     * Set the query start point for pagination.  Typically either the start or cursorMark param.
     *
     * @param query SolrQuery object
     * @return ReadJsonStreamBase object
     */
    protected abstract JsonReadStreamBase setQueryStart(SolrQuery query);

    private void doQuery() {

        if (query == null) {
            handleException(new IllegalStateException("Please ensure you have set the query object"));
        }
        // Don't send a second query if already running or completed
        if (queryRunning || completed) {
            return;
        }

        queryRunning = true;
        setQueryStart(query);

        // send this json message over the event bus with a reply handler.
        solrService.query(query, options, result -> {
            try {
                if (result.succeeded()) {
                    handleResults(result.result());
                } else {
                    handleException(result.cause());
                }
            } catch (Throwable t) {
                handleException(t);
            } finally {
                queryRunning = false;
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
     * @param results JsonObject query reply from Solr
     */
    protected void handleResults(JsonObject results) {

        if (dataHandler != null) {
            dataHandler.handle(results);
        }

        if (isComplete(results)) {
            completed = true;
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