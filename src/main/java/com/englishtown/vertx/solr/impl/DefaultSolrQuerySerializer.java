package com.englishtown.vertx.solr.impl;

import com.englishtown.vertx.solr.SolrQuerySerializer;
import org.apache.solr.client.solrj.SolrQuery;
import org.vertx.java.core.json.JsonArray;
import org.vertx.java.core.json.JsonObject;

import java.util.Iterator;

/**
 * Default implementation of {@link com.englishtown.vertx.solr.SolrQuerySerializer}
 */
public class DefaultSolrQuerySerializer implements SolrQuerySerializer {

    @Override
    public JsonObject serialize(SolrQuery query) {

        JsonObject json = new JsonObject();

        Iterator<String> iterator = query.getParameterNamesIterator();

        while (iterator.hasNext()) {
            String param = iterator.next();
            String[] vals = query.getParams(param);

            if (vals != null) {
                JsonArray array = new JsonArray();
                for (String val : vals) {
                    array.addString(val);
                }
                json.putArray(param, array);
            }
        }

        return json;
    }

    @Override
    public SolrQuery deserialize(JsonObject json) {

        SolrQuery query = new SolrQuery();

        for (String param : json.getFieldNames()) {
            JsonArray array = json.getArray(param);

            if (array != null) {
                String[] vals = new String[array.size()];
                for (int i = 0; i < array.size(); i++) {
                    vals[i] = array.get(i);
                }
                query.set(param, vals);
            }
        }

        return query;
    }

}
