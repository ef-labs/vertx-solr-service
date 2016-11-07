package com.englishtown.vertx.solr.integration;

import com.englishtown.vertx.solr.QueryOptions;
import com.englishtown.vertx.solr.SolrService;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.json.JsonObject;
import io.vertx.test.core.VertxTestBase;

import java.util.Scanner;
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

        JsonObject config = readJson("config.json");

        String core = config.getString("solr_core", "collection1");
        this.queryOptions = new QueryOptions().setCore(core);

        DeploymentOptions options = new DeploymentOptions()
                .setConfig(config);

        CountDownLatch latch = new CountDownLatch(1);

        vertx.deployVerticle("service:com.englishtown.vertx.vertx-solr-service", options, result -> {
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

    protected JsonObject readJson(String path) {

        ClassLoader cl = Thread.currentThread().getContextClassLoader();

        try (Scanner scanner = new Scanner(cl.getResourceAsStream(path)).useDelimiter("\\A")) {
            String s = scanner.next();
            return new JsonObject(s);
        }

    }

}
