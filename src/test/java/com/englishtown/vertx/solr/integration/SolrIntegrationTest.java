package com.englishtown.vertx.solr.integration;

import com.englishtown.vertx.solr.VertxSolrQuery;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import org.junit.Test;


/**
 * Integration tests for {@link com.englishtown.vertx.solr.SolrServiceVerticle}
 */
public class SolrIntegrationTest extends SolrIntegrationTestBase {

    @Test
    public void testQuery() {

       /* SolrQueryOptions  query = new SolrQueryOptions()
                .setQuery("*:*");*/

        VertxSolrQuery query = new VertxSolrQuery();
        query.setQuery("*:*");

        this.proxyService.query(query, queryOptions, result -> {

            if (result.failed()) {
                result.cause().printStackTrace();
                fail();
                return;
            }

            JsonObject body = result.result();
            assertNotNull(body);

            Integer numberFound = body.getInteger("number_found");
            Integer start = body.getInteger("start");
            JsonArray docs = body.getJsonArray("docs");

            assertNotNull(numberFound);
            assertNotNull(start);
            assertNotNull(docs);

            assertEquals(0, start.intValue());
            assertTrue(numberFound > 0);
            assertTrue(docs.size() > 0);

            testComplete();
        });

        await();
    }

}
