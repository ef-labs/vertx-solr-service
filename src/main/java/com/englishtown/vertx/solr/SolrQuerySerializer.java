package com.englishtown.vertx.solr;

import org.apache.solr.client.solrj.SolrQuery;
import org.vertx.java.core.json.JsonObject;

/**
 * Serialize/deserialize solr queries to json
 */
public interface SolrQuerySerializer {

    JsonObject serialize(SolrQuery query);

    SolrQuery deserialize(JsonObject json);

}
