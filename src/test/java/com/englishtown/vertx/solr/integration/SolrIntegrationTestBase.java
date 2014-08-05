package com.englishtown.vertx.solr.integration;

import com.englishtown.vertx.solr.SolrQuerySerializer;
import com.englishtown.vertx.solr.SolrVerticle;
import com.englishtown.vertx.solr.impl.DefaultSolrQuerySerializer;
import org.apache.solr.client.solrj.SolrQuery;
import org.vertx.java.core.AsyncResult;
import org.vertx.java.core.Future;
import org.vertx.java.core.Handler;
import org.vertx.java.core.json.JsonObject;
import org.vertx.testtools.TestVerticle;

/**
 * Solr base integration test
 */
public class SolrIntegrationTestBase extends TestVerticle {

    protected SolrQuerySerializer serializer = new DefaultSolrQuerySerializer();
    protected String address = SolrVerticle.DEFAULT_ADDRESS;
    protected SolrQuery query = new SolrQuery();

    /**
     * {@inheritDoc}
     */
    @Override
    public void start(final Future<Void> startedResult) {

        JsonObject config = new JsonObject()
                .putString("hk2_binder", "com.englishtown.vertx.solr.hk2.SolrBinder")
                .putString("server_url", "http://localhost:8983/solr/collection1");

        container.deployVerticle(SolrVerticle.class.getName(), config, new Handler<AsyncResult<String>>() {
            @Override
            public void handle(AsyncResult<String> result) {
                if (result.succeeded()) {
                    start();
                    startedResult.setResult(null);
                } else {
                    startedResult.setFailure(result.cause());
                }
            }
        });

    }
}
