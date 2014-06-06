package com.englishtown.vertx.solr.impl;

import org.apache.solr.client.solrj.SolrQuery;
import org.junit.Test;
import org.vertx.java.core.json.JsonObject;

import static org.junit.Assert.assertEquals;

public class DefaultSolrQuerySerializerTest {

    @Test
    public void testSerialize() throws Exception {

        DefaultSolrQuerySerializer serializer = new DefaultSolrQuerySerializer();
        SolrQuery query1 = new SolrQuery();
        SolrQuery query2;

        query1.setQuery("sony digital camera")
                .addFilterQuery("cat:electronics", "store:amazon.com")
                .setFields("id", "price", "merchant", "cat", "store")
                .setStart(0)
                .set("defType", "edismax");

        JsonObject json = serializer.serialize(query1);
        query2 = serializer.deserialize(json);

        assertEquals(query1.toString(), query2.toString());

    }
}