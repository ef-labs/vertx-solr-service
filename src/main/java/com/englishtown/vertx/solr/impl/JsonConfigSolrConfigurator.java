package com.englishtown.vertx.solr.impl;

import com.englishtown.vertx.solr.SolrConfigurator;
import org.apache.http.client.HttpClient;
import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.impl.HttpClientUtil;
import org.apache.solr.client.solrj.impl.HttpSolrServer;
import org.apache.solr.client.solrj.impl.LBHttpSolrServer;
import org.apache.solr.common.params.ModifiableSolrParams;
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
    private static String CONFIG_BASIC_USER_NAME = "username";
    private static String CONFIG_BASIC_USER_PASS = "password";

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

        String basicUserName = config.getString(CONFIG_BASIC_USER_NAME);
        String basicUserPass = config.getString(CONFIG_BASIC_USER_PASS);

        if (basicUserPass != null && !basicUserPass.isEmpty()
                && basicUserName != null && !basicUserName.isEmpty()) {
            ModifiableSolrParams params = new ModifiableSolrParams();
            params.set(HttpClientUtil.PROP_MAX_CONNECTIONS, 128);
            params.set(HttpClientUtil.PROP_MAX_CONNECTIONS_PER_HOST, 32);
            params.set(HttpClientUtil.PROP_FOLLOW_REDIRECTS, false);
            params.set(HttpClientUtil.PROP_BASIC_AUTH_USER, basicUserName);
            params.set(HttpClientUtil.PROP_BASIC_AUTH_PASS, basicUserPass);
            HttpClient httpClient = HttpClientUtil.createClient(params);
            return new HttpSolrServer(serverUrl, httpClient);
        } else {
            return new HttpSolrServer(serverUrl);
        }
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
            LBHttpSolrServer server = new LBHttpSolrServer(serverUrls);
            return server;

        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }

    }
}
