package com.englishtown.vertx.solr;

/**
 */
public class Query {

    private StringBuilder queryBuilder;

    Query() {
        queryBuilder = new StringBuilder();
    }

    public static Query query(Term term) {
        return new Query().term(term);
    }

    public static Query query(Group group) {
        return new Query().group(group);
    }

    public Query group(Group group) {
        queryBuilder.append(group.toString());

        return this;
    }

    public Query term(Term term) {
        queryBuilder.append(term.toString());

        return this;
    }

    public Query and(Group group) {
        queryBuilder.append(" AND ");

        return group(group);
    }

    public Query and(Term term) {
        queryBuilder.append(" AND ");

        return term(term);
    }

    public Query or(Group group) {
        queryBuilder.append(" OR ");

        return group(group);
    }

    public Query or(Term term) {
        queryBuilder.append(" OR ");

        return term(term);
    }

    @Override
    public String toString() {
        if (queryBuilder.length() == 0) queryBuilder.append("*:*");

        return queryBuilder.toString();
    }
}
