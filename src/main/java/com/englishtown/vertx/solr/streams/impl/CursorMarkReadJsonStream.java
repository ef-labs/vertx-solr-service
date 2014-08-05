package com.englishtown.vertx.solr.streams.impl;

import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.common.params.CursorMarkParams;
import org.vertx.java.core.json.JsonObject;

/**
 * CursorMark implementation of {@link com.englishtown.vertx.solr.streams.impl.ReadJsonStreamBase}
 * using {@code CursorMark} to paginate json Solr results
 */
public class CursorMarkReadJsonStream extends ReadJsonStreamBase<CursorMarkReadJsonStream> {

    private String currCursorMark;
    private String nextCursorMark;

    public CursorMarkReadJsonStream() {
        super();
        this.nextCursorMark = CursorMarkParams.CURSOR_MARK_START;
    }

    @Override
    protected void setQueryStart(SolrQuery query) {

        query.set(CursorMarkParams.CURSOR_MARK_PARAM, this.nextCursorMark);

    }

    @Override
    protected void handleReply(JsonObject reply) {

        this.currCursorMark= this.nextCursorMark;
        this.nextCursorMark = reply.getString("next_cursor_mark");

        // Recall super
        super.handleReply(reply);

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
