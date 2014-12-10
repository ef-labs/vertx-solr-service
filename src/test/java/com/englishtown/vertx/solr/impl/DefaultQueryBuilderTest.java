package com.englishtown.vertx.solr.impl;

import com.englishtown.vertx.solr.Group;
import com.englishtown.vertx.solr.QueryBuilder;
import org.apache.solr.common.params.CommonParams;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Arrays;

import static com.englishtown.vertx.solr.Group.group;
import static com.englishtown.vertx.solr.Query.query;
import static com.englishtown.vertx.solr.Term.term;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 */
@RunWith(MockitoJUnitRunner.class)
public class DefaultQueryBuilderTest {

    @Test
    public void testSimpleListOfTerms() throws Exception {
        // When I instantiate DefaultQueryBuilder
        DefaultQueryBuilder dqb = new DefaultQueryBuilder();

        // And attempt to build a simple query with 3 terms
        dqb.query(query(term("testcolumn", "searchterm1", 10))
                .and(term("testcolumn", "searchterm2", 20))
                .and(term("testcolumn", "searchterm3", 30)));

        // Then our SolrQuery should have the right query string
        assertEquals("testcolumn:searchterm1^10 AND testcolumn:searchterm2^20 AND testcolumn:searchterm3^30", dqb.build().getQuery());
    }

    @Test
    public void testUsingGroups() throws Exception {
        // When I instantiate DefaultQueryBuilder
        DefaultQueryBuilder dqb = new DefaultQueryBuilder();

        // and build a query with two groups
        Group group1 = group(term("testcol1", "st1g1", 100))
                .and(term("testcol1", "st2g1", 200));

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
        dqb.query(query(term("testcolumn", "searchterm1", 10)));

        // And then enable debug
        dqb.showDebugInfo();

        // Then our SolrQuery should have debug enabled
        assertTrue(dqb.build().getBool(CommonParams.DEBUG_QUERY));
    }

    @Test
    public void testSettingNumberOfRows() throws Exception {
        // When I instantiate DefaultQueryBuilder
        DefaultQueryBuilder dqb = new DefaultQueryBuilder();

        // And attempt to build a simple query
        dqb.query(query(term("testcolumn", "searchterm1", 10)));

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
        assertTrue(Arrays.equals(new String[] {"col1:fq1", "col2:fq2"}, dqb.build().getFilterQueries()));
    }
}
