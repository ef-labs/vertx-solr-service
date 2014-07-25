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

/**
 * Solr client worker verticle
 */
public class SolrVerticle extends BusModBase implements Handler<Message<JsonObject>> {

    public static String DEFAULT_ADDRESS = "et.solr.address";

    public static String FIELD_ACTION = "action";
    public static String FIELD_QUERY = "query";
    public static String FIELD_PAGE = "page";
    public static long PAGE_SIZE = 1000;

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
     * @param message
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

        long page = message.body().getNumber(FIELD_PAGE, 0).longValue();
        SolrQuery query = serializer.deserialize(json);

        try {
            QueryResponse response = solrServer.query(query);

            long offset = PAGE_SIZE * page;
            final SolrDocumentList results = response.getResults();
            final long totalItems = results.getNumFound();

            query.setStart((int) offset);
            query.setRows((int) PAGE_SIZE);

            JsonArray docs = new JsonArray();
            for (SolrDocument result : results) {
                JsonObject doc = new JsonObject();

                for (String key : result.keySet()) {
                    doc.putValue(key, result.getFieldValue(key));
                }

                docs.addObject(doc);
            }

            JsonObject reply = new JsonObject()
                    .putNumber("max_score", results.getMaxScore())
                    .putNumber("number_found", totalItems)
                    .putNumber("page", page)
                    .putNumber("start", offset)
                    .putArray("docs", docs);

            sendOK(message, reply);

        } catch (Exception e) {
            sendError(message, "Error querying solr server: " + e.getMessage(), e);
        }

    }

}
