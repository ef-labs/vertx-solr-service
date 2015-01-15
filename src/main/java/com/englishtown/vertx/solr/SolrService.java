package com.englishtown.vertx.solr;

import io.vertx.codegen.annotations.ProxyGen;
import io.vertx.codegen.annotations.ProxyIgnore;
import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.serviceproxy.ProxyHelper;

/**
 * Vert.x solr service
 */
@VertxGen
@ProxyGen
public interface SolrService {

    static SolrService createEventBusProxy(Vertx vertx, String address) {
        return ProxyHelper.createProxy(SolrService.class, vertx, address);
    }

    @ProxyIgnore
    void start();

    @ProxyIgnore
    void stop();

    default void query(VertxSolrQuery query, Handler<AsyncResult<JsonObject>> resultHandler) {
        query(query, null, resultHandler);
    }

    default void query(VertxSolrQuery query, QueryOptions options, Handler<AsyncResult<JsonObject>> resultHandler) {
        query(query.toJson(), options, resultHandler);
    }

    default void query(JsonObject query, Handler<AsyncResult<JsonObject>> resultHandler) {
        query(query, null, resultHandler);
    }

    void query(JsonObject query, QueryOptions options, Handler<AsyncResult<JsonObject>> resultHandler);

}
