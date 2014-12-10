package com.englishtown.vertx.solr.querybuilder;

/**
 */
public class Term {

    protected String name;
    protected int boost;
    protected String value;

    Term(String name, String value) {
        this.name = name;
        this.value = value;
    }

    public static Term term(String name, String value) {
        return new Term(name, value);
    }

    public Term boost(int boost) {
        this.boost = boost;
        return this;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder()
                .append(name)
                .append(":")
                .append(value);
        if (boost > 0) {
            sb.append("^").append(boost);
        }
        return sb.toString();
    }
}
