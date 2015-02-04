package com.englishtown.vertx.solr.impl;

import com.englishtown.vertx.solr.SolrConfigurator;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.impl.HttpSolrServer;
import org.apache.solr.client.solrj.impl.LBHttpSolrServer;

import javax.inject.Inject;
import java.net.MalformedURLException;

/**
 * Vert.x json configuration implementation
 */
public class JsonConfigSolrConfigurator implements SolrConfigurator {

    public static final String CONFIG_SERVER_TYPE = "server_type";
    public static final String CONFIG_SERVER_URL = "server_url";
    public static final String CONFIG_SERVER_URLS = "server_urls";

    public static final String DEFAULT_SERVER_TYPE = HttpSolrServer.class.getSimpleName();

    private JsonObject config;

    @Inject
    public JsonConfigSolrConfigurator(Vertx vertx) {
        this(vertx.getOrCreateContext().config());
    }

    public JsonConfigSolrConfigurator(JsonObject config) {
        if (config == null) {
            throw new RuntimeException("JSON config was null");
        }
        this.config = config;
    }


    @Override
    public SolrServer createSolrServer() {

        String type = config.getString(CONFIG_SERVER_TYPE, DEFAULT_SERVER_TYPE);

        if (type.equals(HttpSolrServer.class.getSimpleName()) || type.equals(HttpSolrServer.class.getName())) {
            return createHttpSolrServer();
        } else if (type.equals(LBHttpSolrServer.class.getSimpleName()) || type.equals(LBHttpSolrServer.class.getName())) {
            return createLBHttpSolrServer();
        } else {
            throw new IllegalArgumentException("Solr server type " + type + " is not supported");
        }

    }

    private HttpSolrServer createHttpSolrServer() {

        String serverUrl = config.getString(CONFIG_SERVER_URL);
        if (serverUrl == null || serverUrl.isEmpty()) {
            throw new IllegalArgumentException("HttpSolrServer requires a " + CONFIG_SERVER_URL + " field");
        }

        HttpSolrServer server = new BasicAuthHttpSolrServer(serverUrl);
        return server;
    }

    private LBHttpSolrServer createLBHttpSolrServer() {

        JsonArray array = config.getJsonArray(CONFIG_SERVER_URLS);
        if (array == null || array.size() == 0) {
            throw new IllegalArgumentException("LBHttpSolrServer requires a " + CONFIG_SERVER_URLS + " array field");
        }

        String[] serverUrls = new String[array.size()];

        for (int i = 0; i < array.size(); i++) {
            serverUrls[i] = array.getString(i);
        }

        try {
            LBHttpSolrServer server = new BasicAuthLBHttpSolrServer(serverUrls);
            return server;

        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }

    }
}
