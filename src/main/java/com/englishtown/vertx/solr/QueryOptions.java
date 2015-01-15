package com.englishtown.vertx.solr;

import io.vertx.codegen.annotations.Options;
import io.vertx.core.json.JsonObject;

/**
 * Vert.x service query options
 */
@Options
public class QueryOptions {

    private String core;

    public static final String JSON_FIELD_CORE = "core";

    public QueryOptions() {}

    public QueryOptions(QueryOptions other) {
        this.core = other.getCore();
    }

    public QueryOptions(JsonObject json) {
        this.core = json.getString(JSON_FIELD_CORE);
    }

    public String getCore() {
        return core;
    }

    public QueryOptions setCore(String core) {
        this.core = core;
        return this;
    }

    public JsonObject toJson() {

        return new JsonObject()
                .put(JSON_FIELD_CORE, getCore());

    }

}
