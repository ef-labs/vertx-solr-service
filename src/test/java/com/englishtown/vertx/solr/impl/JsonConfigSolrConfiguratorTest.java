package com.englishtown.vertx.solr.impl;

import com.englishtown.vertx.solr.VertxSolrServer;
import io.vertx.core.Context;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.impl.HttpSolrServer;
import org.apache.solr.client.solrj.impl.LBHttpSolrServer;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class JsonConfigSolrConfiguratorTest {

    JsonConfigSolrConfigurator configurator;
    JsonObject config = new JsonObject();

    @Mock
    Vertx vertx;
    @Mock
    Context container;

    @Before
    public void setUp() throws Exception {

        when(container.config()).thenReturn(config);

        configurator = new JsonConfigSolrConfigurator(vertx, container.config());

    }

    @Test
    public void testCreateSolrServer_HttpSolrServer() throws Exception {

        config.put(JsonConfigSolrConfigurator.CONFIG_SERVER_URL, "http://test.englishtown.com/solr");

        VertxSolrServer server = configurator.createSolrServer();
        assertThat(server, instanceOf(DefaultVertxSolrServer.class));

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
    public void testCreateSolrServer_Invalid_SolrServer() throws Exception {

        config.put(JsonConfigSolrConfigurator.CONFIG_SERVER_TYPE, this.getClass().getName());

        try {
            configurator.createSolrServer();
            fail();
        } catch (IllegalArgumentException e) {
            // expected
        }

    }

}