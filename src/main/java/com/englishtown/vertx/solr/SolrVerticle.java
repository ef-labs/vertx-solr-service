package com.englishtown.vertx.solr;

import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.vertx.java.busmods.BusModBase;
import org.vertx.java.core.Handler;
import org.vertx.java.core.eventbus.Message;
import org.vertx.java.core.json.JsonArray;
import org.vertx.java.core.json.JsonObject;

import javax.inject.Inject;
import java.util.Date;
import java.util.List;

/**
 * Solr client worker verticle
 */
public class SolrVerticle extends BusModBase implements Handler<Message<JsonObject>> {

    public static final String DEFAULT_ADDRESS = "et.solr.address";

    public static final String FIELD_ACTION = "action";
    public static final String FIELD_QUERY = "query";

    private String address;
    private SolrServer solrServer;
    private SolrConfigurator configurator;
    private final SolrQuerySerializer serializer;

    @Inject
    public SolrVerticle(SolrConfigurator configurator, SolrQuerySerializer serializer) {
        this.configurator = configurator;
        this.serializer = serializer;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void start() {
        super.start();
        solrServer = configurator.createSolrServer();
        address = container.config().getString("address", DEFAULT_ADDRESS);
        vertx.eventBus().registerHandler(address, this);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void stop() {
        vertx.eventBus().unregisterHandler(address, this);
        solrServer.shutdown();
        solrServer = null;
        super.stop();
    }

    /**
     * Event bus json message handler
     *
     * @param message The query message sent to Solr
     */
    @Override
    public void handle(Message<JsonObject> message) {

        String action = getMandatoryString(FIELD_ACTION, message);
        if (action == null) {
            return;
        }

        try {
            switch (action.toLowerCase()) {
                case "query":
                    doQuery(message);
                    break;

                default:
                    sendError(message, "Action '" + action + "' is not supported");
                    break;
            }
        } catch (Exception e) {
            sendError(message, "Unhandled exception", e);
        }

    }

    public void doQuery(Message<JsonObject> message) {

        JsonObject json = getMandatoryObject(FIELD_QUERY, message);
        if (json == null) {
            return;
        }

        SolrQuery query = serializer.deserialize(json);

        try {

            QueryResponse response = solrServer.query(query);
            SolrDocumentList results = response.getResults();

            JsonArray docs = new JsonArray();
            for (SolrDocument result : results) {
                JsonObject doc = new JsonObject();

                for (String key : result.keySet()) {
                    doc.putValue(key, getJsonValue(result.getFieldValue(key)));
                }

                docs.addObject(doc);
            }

            JsonObject reply = new JsonObject()
                    .putNumber("max_score", results.getMaxScore())
                    .putNumber("number_found", results.getNumFound())
                    .putNumber("start", results.getStart())
                    .putArray("docs", docs);

            // Solr CursorsMarks are supported as of version 4.7.0
            if (response.getNextCursorMark() != null && !response.getNextCursorMark().isEmpty()) {
                reply.putString("next_cursor_mark", response.getNextCursorMark());
            }

            sendOK(message, reply);

        } catch (Exception e) {
            sendError(message, "Error querying solr server: " + e.getMessage(), e);
        }

    }

    private Object getJsonValue(Object val) {

        if (val instanceof Date) {
            return ((Date) val).getTime();
        }

        if (val instanceof List) {
            JsonArray arr = new JsonArray();
            for (Object v : (Iterable) val) {
                arr.add(getJsonValue(v));
            }
            return arr;
        }

        return val;

    }

}
