package com.englishtown.vertx.solr;

import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.http.HttpClientOptions;
import io.vertx.core.json.JsonObject;
import org.apache.solr.client.solrj.SolrRequest;
import org.apache.solr.common.params.SolrParams;

/**
 * Vert.x solr http server
 */
public interface VertxSolrServer {

    void setInvariantParams(SolrParams params);

    void request(SolrRequest request, Handler<AsyncResult<JsonObject>> resultHandler);

    void stop();

}
