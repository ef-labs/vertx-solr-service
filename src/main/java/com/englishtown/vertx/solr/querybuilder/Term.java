package com.englishtown.vertx.solr.querybuilder;

/**
 */
public class Term {

    protected String name;
    protected int boost;
    protected String value;
    protected boolean phrase = true;

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

    public Term phrase(boolean phrase) {
        this.phrase = phrase;
        return this;
    }

    @Override
    public String toString() {

        StringBuilder sb = new StringBuilder(name)
                .append(":");

        if (phrase) {
            sb.append("\"");
        }
        sb.append(value);
        if (phrase) {
            sb.append("\"");
        }
        if (boost > 0) {
            sb.append("^").append(boost);
        }
        return sb.toString();
    }
}
