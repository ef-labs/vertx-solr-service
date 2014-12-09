package com.englishtown.vertx.solr;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import org.apache.solr.client.solrj.SolrQuery;

import java.util.Iterator;

/**
 * Vert.x {@link org.apache.solr.client.solrj.SolrQuery}
 */
public class VertxSolrQuery extends SolrQuery {

    public VertxSolrQuery() {
    }

    public VertxSolrQuery(VertxSolrQuery other) {
        if (other != null) {
            for (String name : other.getParameterNames()) {
                this.setParam(name, other.getParams(name));
            }
        }
    }

    public VertxSolrQuery(JsonObject json) {

        for (String param : json.fieldNames()) {
            JsonArray array = json.getJsonArray(param);

            if (array != null) {
                String[] vals = new String[array.size()];
                for (int i = 0; i < array.size(); i++) {
                    vals[i] = array.getString(i);
                }
                this.set(param, vals);
            }
        }

    }

    public JsonObject toJson() {

        JsonObject json = new JsonObject();

        Iterator<String> iterator = this.getParameterNamesIterator();

        while (iterator.hasNext()) {
            String param = iterator.next();
            String[] vals = this.getParams(param);

            if (vals != null) {
                JsonArray array = new JsonArray();
                for (String val : vals) {
                    array.add(val);
                }
                json.put(param, array);
            }
        }

        return json;
    }

}
