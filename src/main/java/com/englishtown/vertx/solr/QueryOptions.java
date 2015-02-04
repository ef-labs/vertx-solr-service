package com.englishtown.vertx.solr;

import io.vertx.codegen.annotations.Options;
import io.vertx.core.json.JsonObject;

/**
 * Vert.x service query options
 */
@Options
public class QueryOptions {

    private String core;
    private String basicAuthUser;
    private String basicAuthPass;

    public static final String JSON_FIELD_CORE = "core";
    public static final String JSON_FIELD_BASIC_AUTH_USER = "basic_auth_user";
    public static final String JSON_FIELD_BASIC_AUTH_PASS = "basic_auth_pass";

    public QueryOptions() {
    }

    public QueryOptions(QueryOptions other) {
        this.core = other.getCore();
        this.basicAuthUser = other.getBasicAuthUser();
        this.basicAuthPass = other.getBasicAuthPass();
    }

    public QueryOptions(JsonObject json) {
        this.core = json.getString(JSON_FIELD_CORE);
        this.basicAuthUser = json.getString(JSON_FIELD_BASIC_AUTH_USER);
        this.basicAuthPass = json.getString(JSON_FIELD_BASIC_AUTH_PASS);
    }

    public String getCore() {
        return core;
    }

    public QueryOptions setCore(String core) {
        this.core = core;
        return this;
    }

    public String getBasicAuthUser() {
        return basicAuthUser;
    }

    public QueryOptions setBasicAuthUser(String basicAuthUser) {
        this.basicAuthUser = basicAuthUser;
        return this;
    }

    public String getBasicAuthPass() {
        return basicAuthPass;
    }

    public QueryOptions setBasicAuthPass(String basicAuthPass) {
        this.basicAuthPass = basicAuthPass;
        return this;
    }

    public JsonObject toJson() {

        return new JsonObject()
                .put(JSON_FIELD_CORE, getCore())
                .put(JSON_FIELD_BASIC_AUTH_USER, getBasicAuthUser())
                .put(JSON_FIELD_BASIC_AUTH_PASS, getBasicAuthPass());

    }

}
