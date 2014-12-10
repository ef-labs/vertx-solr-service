package com.englishtown.vertx.solr.querybuilder.impl;

import com.englishtown.vertx.solr.querybuilder.Query;
import com.englishtown.vertx.solr.querybuilder.QueryBuilder;
import com.englishtown.vertx.solr.querybuilder.Term;
import com.englishtown.vertx.solr.VertxSolrQuery;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 */
public class DefaultQueryBuilder implements QueryBuilder {

    private final List<Term> filterQueryTerms;

    private boolean showDebugInfo;
    private String queryPrefix;

    private static final String DESCENDING_DATE_BOOST = "{!boost b=product(recip(ms(NOW,$FIELDNAME),3.16e-11,1,1), $MULTIPLIER)}";
    private static final String ASCENDING_DATE_BOOST = "{!boost b=product(sub(1,recip(ms(NOW,$FIELDNAME),3.16e-11,1,1)), $MULTIPLIER)}";
    private Query query;
    private int numRows;

    public DefaultQueryBuilder() {
        filterQueryTerms = new ArrayList<>();

        showDebugInfo = false;
        queryPrefix = "";
        numRows = 10;
    }

    @Override
    public QueryBuilder query(Query query) {
        this.query = query;

        return this;
    }

    @Override
    public QueryBuilder filterQuery(Term... terms) {
        // Go through all of the terms and ensure boost is set to 0, as boost has no meaning in the filter query
        for (Term term : terms) {
            term.boost(0);
        }

        filterQueryTerms.addAll(Arrays.asList(terms));

        return this;
    }

    @Override
    public QueryBuilder showDebugInfo() {
        showDebugInfo = true;

        return this;
    }

    @Override
    public QueryBuilder dateBoost(String fieldName, DateOrder dateOrder) {
        return dateBoost(fieldName, dateOrder, 1);
    }

    @Override
    public QueryBuilder dateBoost(String fieldName, DateOrder dateOrder, int multiplier) {
        if (fieldName == null || fieldName.equals("")) {
            throw new IllegalArgumentException("fieldname cannot be null or empty");
        }

        queryPrefix = (dateOrder.equals(DateOrder.ASCENDING)) ? ASCENDING_DATE_BOOST : DESCENDING_DATE_BOOST;
        queryPrefix = queryPrefix.replace("$FIELDNAME", fieldName).replace("$MULTIPLIER", Integer.toString(multiplier));

        return this;
    }

    @Override
    public QueryBuilder rows(int numRows) {
        this.numRows = numRows;

        return this;
    }

    @Override
    public VertxSolrQuery build() {

        String[] stringFilterQueries = new String[filterQueryTerms.size()];
        for (int i = 0; i < filterQueryTerms.size(); i++) {
            stringFilterQueries[i] = filterQueryTerms.get(i).toString();
        }

        String queryString = (query == null) ? "*:*" : query.toString();

        VertxSolrQuery vertxSolrQuery = new VertxSolrQuery();
        vertxSolrQuery
                .setQuery(queryPrefix + queryString)
                .setRows(numRows)
                .setFilterQueries(stringFilterQueries)
                .setShowDebugInfo(showDebugInfo);

        return vertxSolrQuery;
    }
}
