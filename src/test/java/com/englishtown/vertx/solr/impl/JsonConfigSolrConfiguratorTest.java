package com.englishtown.vertx.solr.impl;

import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.impl.HttpSolrServer;
import org.apache.solr.client.solrj.impl.LBHttpSolrServer;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.vertx.java.core.json.JsonArray;
import org.vertx.java.core.json.JsonObject;
import org.vertx.java.platform.Container;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class JsonConfigSolrConfiguratorTest {

    JsonConfigSolrConfigurator configurator;
    JsonObject config = new JsonObject();

    @Mock
    Container container;

    @Before
    public void setUp() throws Exception {

        when(container.config()).thenReturn(config);

        configurator = new JsonConfigSolrConfigurator(container);

    }

    @Test
    public void testCreateSolrServer_HttpSolrServer() throws Exception {

        config.putString(JsonConfigSolrConfigurator.CONFIG_SERVER_URL, "http://test.englishtown.com/solr");

        SolrServer server = configurator.createSolrServer();
        assertThat(server, instanceOf(HttpSolrServer.class));

    }

    @Test
    public void testCreateSolrServer_HttpSolrServer_Fail() throws Exception {

        try {
            configurator.createSolrServer();
            fail();
        } catch (IllegalArgumentException e) {
            // expected
        }

    }

    @Test
    public void testCreateSolrServer_LBHttpSolrServer() throws Exception {

        config.putString(JsonConfigSolrConfigurator.CONFIG_SERVER_TYPE, LBHttpSolrServer.class.getSimpleName());
        config.putArray(JsonConfigSolrConfigurator.CONFIG_SERVER_URLS, new JsonArray().addString("http://test.englishtown.com/solr"));

        SolrServer server = configurator.createSolrServer();
        assertThat(server, instanceOf(LBHttpSolrServer.class));

    }

    @Test
    public void testCreateSolrServer_LBHttpSolrServer_Fail() throws Exception {

        config.putString(JsonConfigSolrConfigurator.CONFIG_SERVER_TYPE, LBHttpSolrServer.class.getSimpleName());

        try {
            configurator.createSolrServer();
            fail();
        } catch (IllegalArgumentException e) {
            // expected
        }

    }

    @Test
    public void testCreateSolrServer_Invalid_SolrServer() throws Exception {

        config.putString(JsonConfigSolrConfigurator.CONFIG_SERVER_TYPE, this.getClass().getName());

        try {
            configurator.createSolrServer();
            fail();
        } catch (IllegalArgumentException e) {
            // expected
        }

    }

}