package com.englishtown.vertx.solr.streams.impl;

import com.englishtown.vertx.solr.SolrService;
import io.vertx.core.json.JsonObject;
import org.apache.solr.client.solrj.SolrQuery;

/**
 * Implementation of {@link JsonReadStreamBase} using an integer offset
 * to paginate json Solr results
 */
public class OffsetJsonReadStream extends JsonReadStreamBase {

    private int nextOffset;

    public OffsetJsonReadStream(SolrService solrService) {
        super(solrService);
    }


    // this method needs to be aware of pausing so that it can keep track of where it is when it resumes
    @Override
    protected OffsetJsonReadStream setQueryStart(SolrQuery query) {
        query.setStart(nextOffset);
        return this;
    }

    @Override
    protected void handleResults(JsonObject reply) {

        int count = reply.getJsonArray("docs").size();
        nextOffset += count;

        // Recall super
        super.handleResults(reply);

    }

    @Override
    protected boolean isComplete(JsonObject reply) {

        boolean complete = false;
        int numberFound = reply.getInteger("number_found");
        int count = reply.getJsonArray("docs").size();

        if (nextOffset >= numberFound || count == 0) {
            complete = true;
        }
        return complete;

    }

}
