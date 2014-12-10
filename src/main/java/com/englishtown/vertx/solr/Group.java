package com.englishtown.vertx.solr;

/**
 */
public class Group {
    private StringBuilder groupBuilder;
    private int boost;

    Group() {
        groupBuilder = new StringBuilder("(");
    }

    public static Group group(Term term) {
        return new Group().term(term);
    }

    public Group term(Term term) {
        groupBuilder.append(term.toString());

        return this;
    }

    public Group and(Term term) {
        groupBuilder.append(" AND ");

        return term(term);
    }

    public Group or(Term term) {
        groupBuilder.append(" OR ");

        return term(term);
    }

    /**
     * Allows for the entire group to be boosted.
     *
     * @param boost
     * @return
     */
    public Group boost(int boost) {
        this.boost = boost;

        return this;
    }

    @Override
    public String toString() {
        groupBuilder.append(")");

        if (boost > 0) {
            groupBuilder.append("^");
            groupBuilder.append(Integer.toString(boost));
        }

        return groupBuilder.toString();
    }
}

