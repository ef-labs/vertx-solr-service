package com.englishtown.vertx.solr;

/**
 */
public class Term {

    private final String name;
    private final String value;

    private int boost;

    Term(String name, String value, int boost) {
        this.name = name;
        this.value = value;

        this.boost = boost;
    }

    public static Term term(String name, String value) {
        return term(name, value, 0);
    }

    public static Term term(String name, String value, int boost) {
        return new Term(name, value, boost);
    }

    public Term boost(int boost) {
        this.boost = boost;

        return this;
    }

    @Override
    public String toString() {

        String boostString = (boost > 0) ? "^" + Integer.toString(boost) : "";

        return name + ":" + value + boostString;
    }
}
