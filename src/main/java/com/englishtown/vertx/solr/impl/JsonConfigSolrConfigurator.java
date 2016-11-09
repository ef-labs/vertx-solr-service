package com.englishtown.vertx.solr.impl;

import com.englishtown.vertx.solr.SolrConfigurator;
import com.englishtown.vertx.solr.VertxSolrClient;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpClientOptions;
import io.vertx.core.json.JsonObject;

import javax.inject.Inject;

/**
 * Vert.x json configuration implementation
 */
public class JsonConfigSolrConfigurator implements SolrConfigurator {

    public static final String CONFIG_CLIENT_TYPE = "client_type";
    public static final String CONFIG_SERVER_URL = "server_url";
    public static final String CONFIG_HTTP_CLIENT_OPTIONS = "http_client_options";

    public static final String DEFAULT_CLIENT_TYPE = DefaultVertxSolrClient.class.getSimpleName();

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
    public VertxSolrClient createSolrClient() {

        String type = config.getString(CONFIG_CLIENT_TYPE, DEFAULT_CLIENT_TYPE);

        if (type.equals(DefaultVertxSolrClient.class.getSimpleName())
                || type.equals(DefaultVertxSolrClient.class.getName())
                || type.equals(VertxSolrClient.class.getSimpleName())
                || type.equals(VertxSolrClient.class.getName())) {
            return createVertxSolrClient();
        } else if (type.equals(CloudVertxSolrClient.class.getName()) 
        		|| type.equals(CloudVertxSolrClient.class.getSimpleName())) {
            return createCloudVertxSolrClient();
        } else {
            throw new IllegalArgumentException("Solr client type " + type + " is not supported.  Try the default: " + DEFAULT_CLIENT_TYPE);
        }

    }

    protected VertxSolrClient createCloudVertxSolrClient() {
    	String serverUrl = config.getString(CONFIG_SERVER_URL);
        if (serverUrl == null || serverUrl.isEmpty()) {
            throw new IllegalArgumentException("CloudVertxSolrClient requires a " + CONFIG_SERVER_URL + " field");
        }

        JsonObject clientOptions = config.getJsonObject(CONFIG_HTTP_CLIENT_OPTIONS, new JsonObject());

        return new CloudVertxSolrClient(vertx, serverUrl);
	}
    
    protected VertxSolrClient createVertxSolrClient() {

        String serverUrl = config.getString(CONFIG_SERVER_URL);
        if (serverUrl == null || serverUrl.isEmpty()) {
            throw new IllegalArgumentException("DefaultVertxSolrClient requires a " + CONFIG_SERVER_URL + " field");
        }

        JsonObject clientOptions = config.getJsonObject(CONFIG_HTTP_CLIENT_OPTIONS, new JsonObject());

        return new DefaultVertxSolrClient(vertx, serverUrl, new HttpClientOptions(clientOptions));
    }

}
