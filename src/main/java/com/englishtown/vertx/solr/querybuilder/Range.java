package com.englishtown.vertx.solr.querybuilder;

/**
 * Range for filter query
 */
public class Range extends Term {

    private final String from;
    private final String to;


    Range(String name, String from, String to) {
        super(name, null);

        this.from = from;
        this.to = to;
    }

    public static Range range(String name, String from, String to) {
        return new Range(name, from, to);
    }

    @Override
    public Range boost(int boost) {
        super.boost(boost);
        return this;
    }

    @Override
    public String toString() {
        // eq. total:[1 TO 100]
        StringBuilder sb = new StringBuilder()
                .append(name)
                .append(":[")
                .append(from)
                .append(" TO ")
                .append(to)
                .append("]");
        if (boost > 0) {
            sb.append("^").append(boost);
        }
        return sb.toString();
    }
}
