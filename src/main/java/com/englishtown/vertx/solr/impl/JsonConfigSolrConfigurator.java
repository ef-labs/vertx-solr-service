package com.englishtown.vertx.solr.impl;

import com.englishtown.vertx.solr.SolrConfigurator;
import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.impl.HttpSolrServer;
import org.apache.solr.client.solrj.impl.LBHttpSolrServer;
import org.vertx.java.core.json.JsonArray;
import org.vertx.java.core.json.JsonObject;
import org.vertx.java.platform.Container;

import javax.inject.Inject;
import java.net.MalformedURLException;

/**
 * Vert.x json configuration implementation
 */
public class JsonConfigSolrConfigurator implements SolrConfigurator {

    public static String CONFIG_SERVER_TYPE = "server_type";
    public static String CONFIG_SERVER_URL = "server_url";
    public static String CONFIG_SERVER_URLS = "server_urls";

    public static String DEFAULT_SERVER_TYPE = HttpSolrServer.class.getSimpleName();

    private JsonObject config;

    @Inject
    public JsonConfigSolrConfigurator(Container container) {
        config = init(container);
    }

    protected JsonObject init(Container container) {
        return container.config();
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

        JsonArray array = config.getArray(CONFIG_SERVER_URLS);
        if (array == null || array.size() == 0) {
            throw new IllegalArgumentException("LBHttpSolrServer requires a " + CONFIG_SERVER_URLS + " array field");
        }

        String[] serverUrls = new String[array.size()];

        for (int i = 0; i < array.size(); i++) {
            serverUrls[i] = array.get(i);
        }

        try {
            LBHttpSolrServer server = new BasicAuthLBHttpSolrServer(serverUrls);
            return server;

        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }

    }
}
