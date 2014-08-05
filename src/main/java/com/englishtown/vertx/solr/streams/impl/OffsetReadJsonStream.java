package com.englishtown.vertx.solr.streams.impl;

import org.apache.solr.client.solrj.SolrQuery;
import org.vertx.java.core.json.JsonObject;

/**
 * Implementation of {@link ReadJsonStreamBase} using an integer offset
 * to paginate json Solr results
 */
public class OffsetReadJsonStream extends ReadJsonStreamBase<OffsetReadJsonStream> {

    private int nextOffset;

    public OffsetReadJsonStream() {
        super();
    }


    // this method needs to be aware of pausing so that it can keep track of where it is when it resumes
    @Override
    protected void setQueryStart(SolrQuery query) {

        query.setStart(nextOffset);

    }

    @Override
    protected void handleReply(JsonObject reply) {

        int count = reply.getArray("docs").size();
        nextOffset += count;

        // Recall super
        super.handleReply(reply);

    }

    @Override
    protected boolean isComplete(JsonObject reply) {

        boolean complete = false;
        int numberFound = reply.getInteger("number_found");
        int count = reply.getArray("docs").size();

        if (nextOffset >= numberFound || count == 0) {
            complete = true;
        }
        return complete;

    }

}
