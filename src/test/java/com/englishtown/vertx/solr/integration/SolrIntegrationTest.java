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

            JsonObject response = body.getJsonObject("response");
            assertNotNull(response);

            Integer numFound = response.getInteger("numFound");
            Integer start = response.getInteger("start");
            JsonArray docs = response.getJsonArray("docs");

            assertNotNull(numFound);
            assertNotNull(start);
            assertNotNull(docs);

            assertEquals(0, start.intValue());
            assertTrue(numFound > 0);
            assertTrue(docs.size() > 0);

            testComplete();
        });

        await();
    }

}
