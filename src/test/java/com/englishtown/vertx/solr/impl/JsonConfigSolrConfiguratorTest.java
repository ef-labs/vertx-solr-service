package com.englishtown.vertx.solr.impl;

import com.englishtown.vertx.solr.VertxSolrClient;
import io.vertx.core.Context;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
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
    public void testCreateSolrClient() throws Exception {

        config.put(JsonConfigSolrConfigurator.CONFIG_SERVER_URL, "http://test.englishtown.com/solr");

        VertxSolrClient server = configurator.createSolrClient();
        assertThat(server, instanceOf(DefaultVertxSolrClient.class));

    }

    @Test
    public void testCreateSolrClient_Fail() throws Exception {

        try {
            configurator.createSolrClient();
            fail();
        } catch (IllegalArgumentException e) {
            // expected
        }

    }

    @Test
    public void testCreateSolrClient_Invalid_SolrClient() throws Exception {

        config.put(JsonConfigSolrConfigurator.CONFIG_CLIENT_TYPE, this.getClass().getName());

        try {
            configurator.createSolrClient();
            fail();
        } catch (IllegalArgumentException e) {
            // expected
        }

    }

}