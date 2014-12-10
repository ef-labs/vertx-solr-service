package com.englishtown.vertx.solr.querybuilder.impl;

import com.englishtown.vertx.solr.VertxSolrQuery;
import com.englishtown.vertx.solr.querybuilder.Group;
import com.englishtown.vertx.solr.querybuilder.QueryBuilder;
import org.apache.solr.common.params.CommonParams;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Arrays;

import static com.englishtown.vertx.solr.querybuilder.Group.group;
import static com.englishtown.vertx.solr.querybuilder.Query.query;
import static com.englishtown.vertx.solr.querybuilder.Range.range;
import static com.englishtown.vertx.solr.querybuilder.Term.term;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

/**
 */
@RunWith(MockitoJUnitRunner.class)
public class DefaultQueryBuilderTest {

    @Test
    public void testSimpleListOfTerms() throws Exception {
        // When I instantiate DefaultQueryBuilder
        DefaultQueryBuilder dqb = new DefaultQueryBuilder();

        // And attempt to build a simple query with 3 terms
        dqb.query(query(term("testcolumn", "searchterm1").boost(10))
                .and(term("testcolumn", "searchterm2").boost(20))
                .and(term("testcolumn", "searchterm3").boost(30)));

        // Then our SolrQuery should have the right query string
        assertEquals("testcolumn:searchterm1^10 AND testcolumn:searchterm2^20 AND testcolumn:searchterm3^30", dqb.build().getQuery());
    }

    @Test
    public void testRange() throws Exception {
        // When I instantiate DefaultQueryBuilder
        DefaultQueryBuilder dqb = new DefaultQueryBuilder();

        // And attempt to build a simple query with 3 terms
        dqb.query(query(term("testcolumn", "searchterm1").boost(10))
                .and(term("testcolumn", "searchterm2").boost(20))
                .and(range("testcolumn2", "0", "100").boost(30)));

        // Then our SolrQuery should have the right query string
        assertEquals("testcolumn:searchterm1^10 AND testcolumn:searchterm2^20 AND testcolumn2:[0 TO 100]^30", dqb.build().getQuery());
    }

    @Test
    public void testUsingGroups() throws Exception {
        // When I instantiate DefaultQueryBuilder
        DefaultQueryBuilder dqb = new DefaultQueryBuilder();

        // and build a query with two groups
        Group group1 = group(term("testcol1", "st1g1").boost(100))
                .and(term("testcol1", "st2g1").boost(200));

        Group group2 = group(term("testcol2", "st1g2"))
                .or(term("testcol2", "st2g2"))
                .boost(500);

        dqb.query(query(group1).or(group2));

        // Then our SolrQuery should have the right query string
        assertEquals("(testcol1:st1g1^100 AND testcol1:st2g1^200) OR (testcol2:st1g2 OR testcol2:st2g2)^500", dqb.build().getQuery());
    }

    @Test
    public void testEnablingDebug() throws Exception {
        // When I instantiate DefaultQueryBuilder
        DefaultQueryBuilder dqb = new DefaultQueryBuilder();

        // And attempt to build a simple query
        dqb.query(query(term("testcolumn", "searchterm1").boost(10)));

        // And then enable debug
        dqb.showDebugInfo();

        // Then our SolrQuery should have debug enabled
        Assert.assertTrue(dqb.build().getBool(CommonParams.DEBUG_QUERY));
    }

    @Test
    public void testSettingNumberOfRows() throws Exception {
        // When I instantiate DefaultQueryBuilder
        DefaultQueryBuilder dqb = new DefaultQueryBuilder();

        // And attempt to build a simple query
        dqb.query(query(term("testcolumn", "searchterm1").boost(10)));

        // And then set the number of rows to 5
        dqb.rows(5);

        // Then our SolrQuery should have 5 rows set
        assertEquals(5, (int) dqb.build().getRows());
    }

    @Test
    public void testSettingDateBoostAscending() throws Exception {
        // When I instantiate DefaultQueryBuilder
        DefaultQueryBuilder dqb = new DefaultQueryBuilder();

        // And then enable date boost ascending
        dqb.dateBoost("datecolumn", QueryBuilder.DateOrder.ASCENDING);

        // Then our SolrQuery should have the right query string
        assertEquals("{!boost b=product(sub(1,recip(ms(NOW,datecolumn),3.16e-11,1,1)), 1)}*:*", dqb.build().getQuery());
    }

    @Test
    public void testSettingDateBoostDescendingWithMultiplier() throws Exception {
        // When I instantiate DefaultQueryBuilder
        DefaultQueryBuilder dqb = new DefaultQueryBuilder();

        // And then enable date boost descending
        dqb.dateBoost("datecolumn", QueryBuilder.DateOrder.DESCENDING, 25);

        // Then our SolrQuery should have the right query string
        assertEquals("{!boost b=product(recip(ms(NOW,datecolumn),3.16e-11,1,1), 25)}*:*", dqb.build().getQuery());
    }

    @Test
    public void testSettingFilterQueries() throws Exception {
        // When I instantiate DefaultQueryBuilder
        DefaultQueryBuilder dqb = new DefaultQueryBuilder();

        // And then set a couple of filter queries
        dqb.filterQuery(term("col1", "fq1"), term("col2", "fq2"));

        // Then our SolrQuery should have the right filter queries
        Assert.assertTrue(Arrays.equals(new String[]{"col1:fq1", "col2:fq2"}, dqb.build().getFilterQueries()));
    }

    @Test
    public void testQuery_full_example() {
        DefaultQueryBuilder dqb = new DefaultQueryBuilder();

        Group group1 = group(range("lang_level", "6", "8").boost(100))
                .or(range("lang_level", "4", "5").boost(150))
                .or(term("lang_level", "9").boost(10));

        Group group2 = group(term("utc_offset", "\"-9\"").boost(50))
                .or(term("utc_offset", "\"-10\"").boost(20))
                .or(term("utc_offset", "\"-11\"").boost(20));

        dqb.dateBoost("created", QueryBuilder.DateOrder.ASCENDING, 1)
                .query(query(group1).and(group2))
                .filterQuery(term("lang_code", "en"), term("gender", "f"));

        VertxSolrQuery solrQuery = dqb.build();

        String q = solrQuery.getQuery();
        assertEquals("{!boost b=product(sub(1,recip(ms(NOW,created),3.16e-11,1,1)), 1)}(lang_level:[6 TO 8]^100 OR lang_level:[4 TO 5]^150 OR lang_level:9^10) AND (utc_offset:\"-9\"^50 OR utc_offset:\"-10\"^20 OR utc_offset:\"-11\"^20)", q);

        String[] fq = solrQuery.getFilterQueries();
        assertArrayEquals(new String[]{"lang_code:en", "gender:f"}, fq);

    }
}
