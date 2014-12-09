package com.englishtown.vertx.solr.impl;

import com.englishtown.vertx.solr.VertxSolrQuery;
import io.vertx.core.json.JsonObject;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class VertxSolrQueryTest {

    @Test
    public void testSerialize() throws Exception {

        VertxSolrQuery query1 = new VertxSolrQuery();
        VertxSolrQuery query2;

        query1.setQuery("sony digital camera")
                .addFilterQuery("cat:electronics", "store:amazon.com")
                .setFields("id", "price", "merchant", "cat", "store")
                .setStart(0)
                .set("defType", "edismax");

        JsonObject json = query1.toJson();
        query2 = new VertxSolrQuery(json);

        assertEquals(query1.toString(), query2.toString());

    }


    @Test
    public void testCreateFromOther() throws Exception {

        VertxSolrQuery query1 = new VertxSolrQuery();


        query1.setQuery("sony digital camera")
                .addFilterQuery("cat:electronics", "store:amazon.com")
                .setFields("id", "price", "merchant", "cat", "store")
                .setStart(0)
                .set("defType", "edismax");

        VertxSolrQuery query2 = new VertxSolrQuery(query1);

        assertEquals(query1.toString(), query2.toString());

    }
}