package com.englishtown.vertx.solr.impl;

import com.englishtown.vertx.solr.SolrConfigurator;
import com.englishtown.vertx.solr.VertxSolrServer;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpClientOptions;
import io.vertx.core.json.JsonObject;
import org.apache.solr.client.solrj.impl.HttpSolrServer;

import javax.inject.Inject;

/**
 * Vert.x json configuration implementation
 */
public class JsonConfigSolrConfigurator implements SolrConfigurator {

    public static final String CONFIG_SERVER_TYPE = "server_type";
    public static final String CONFIG_SERVER_URL = "server_url";
    public static final String CONFIG_HTTP_CLIENT_OPTIONS = "http_client_options";

    public static final String DEFAULT_SERVER_TYPE = HttpSolrServer.class.getSimpleName();

    private Vertx vertx;
    private JsonObject config;

    @Inject
    public JsonConfigSolrConfigurator(Vertx vertx) {
        this(vertx, vertx.getOrCreateContext().config());
    }

    public JsonConfigSolrConfigurator(Vertx vertx, JsonObject config) {
        if (config == null) {
            throw new RuntimeException("JSON config was null");
        }
        this.config = config;
        this.vertx = vertx;
    }


    @Override
    public VertxSolrServer createSolrServer() {

        String type = config.getString(CONFIG_SERVER_TYPE, DEFAULT_SERVER_TYPE);

        if (type.equals(DefaultVertxSolrServer.class.getSimpleName())
                || type.equals(DefaultVertxSolrServer.class.getName())
                || type.equals(VertxSolrServer.class.getSimpleName())
                || type.equals(VertxSolrServer.class.getName())
                || type.equals(HttpSolrServer.class.getSimpleName())
                || type.equals(HttpSolrServer.class.getName())) {
            return createHttpSolrServer();
        } else {
            throw new IllegalArgumentException("Solr server type " + type + " is not supported");
        }

    }

    protected VertxSolrServer createHttpSolrServer() {

        String serverUrl = config.getString(CONFIG_SERVER_URL);
        if (serverUrl == null || serverUrl.isEmpty()) {
            throw new IllegalArgumentException("DefaultVertxSolrServer requires a " + CONFIG_SERVER_URL + " field");
        }

        JsonObject clientOptions = config.getJsonObject(CONFIG_HTTP_CLIENT_OPTIONS, new JsonObject());

        return new DefaultVertxSolrServer(vertx, serverUrl, new HttpClientOptions(clientOptions));
    }

}
