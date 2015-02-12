package com.englishtown.vertx.solr.streams.impl;

import com.englishtown.vertx.solr.SolrService;
import io.vertx.core.json.JsonObject;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.common.params.CursorMarkParams;

/**
 * CursorMark implementation of {@link JsonReadStreamBase}
 * using {@code CursorMark} to paginate json Solr results
 */
public class CursorMarkJsonReadStream extends JsonReadStreamBase {

    private String currCursorMark;
    private String nextCursorMark;

    public CursorMarkJsonReadStream(SolrService solrService) {
        super(solrService);
        this.nextCursorMark = CursorMarkParams.CURSOR_MARK_START;
    }

    @Override
    protected CursorMarkJsonReadStream setQueryStart(SolrQuery query) {
        query.set(CursorMarkParams.CURSOR_MARK_PARAM, this.nextCursorMark);
        return this;
    }

    @Override
    protected void handleResults(JsonObject reply) {

        this.currCursorMark = this.nextCursorMark;
        this.nextCursorMark = reply.getJsonObject("response").getString("next_cursor_mark");

        // Recall super
        super.handleResults(reply);

    }

    @Override
    protected boolean isComplete(JsonObject reply) {

        boolean complete = false;

        if (nextCursorMark.equalsIgnoreCase(currCursorMark)) {
            complete = true;
        }
        return complete;

    }
}
