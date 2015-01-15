package com.englishtown.vertx.solr.integration;

import com.englishtown.vertx.solr.QueryOptions;
import com.englishtown.vertx.solr.SolrService;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.json.JsonObject;
import io.vertx.test.core.VertxTestBase;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * Solr base integration test
 */
public abstract class SolrIntegrationTestBase extends VertxTestBase {

    public static final String EB_ADDRESS = "et.solr";

    protected SolrService proxyService;
    protected QueryOptions queryOptions;

    /**
     * {@inheritDoc}
     */
    @Override
    public void setUp() throws Exception {
        super.setUp();

        JsonObject config = new JsonObject()
                .put("address", EB_ADDRESS)
                .put("hk2_binder", "com.englishtown.vertx.solr.hk2.SolrBinder")
                .put("server_url", "http://localhost:8983/solr");

        this.queryOptions = new QueryOptions().setCore("collection1");

        DeploymentOptions options = new DeploymentOptions()
                .setConfig(config);

        CountDownLatch latch = new CountDownLatch(1);

        vertx.deployVerticle("service:com.englishtown.vertx:vertx-solr-service", options, result -> {
            if (result.failed()) {
                result.cause().printStackTrace();
                fail();
            }
            latch.countDown();
        });

        latch.await(2, TimeUnit.SECONDS);

        proxyService = SolrService.createEventBusProxy(vertx, EB_ADDRESS);

    }

    protected void handleThrowable(Throwable t) {
        t.printStackTrace();
        fail();
    }

}
