package com.englishtown.vertx.solr.streams.impl;

import com.englishtown.vertx.solr.SolrQuerySerializer;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.common.params.CursorMarkParams;
import org.apache.solr.common.params.ModifiableSolrParams;
import org.vertx.java.core.eventbus.EventBus;
import org.vertx.java.core.json.JsonObject;

/**
 * CursorMark implementation of {@link com.englishtown.vertx.solr.streams.impl.ReadJsonStreamBase}
 * using {@code CursorMark} to paginate json Solr results
 */
public class CursorMarkReadJsonStream extends ReadJsonStreamBase<CursorMarkReadJsonStream> {

    private String currCursorMark;
    private String nextCursorMark;

    public CursorMarkReadJsonStream(SolrQuery query, SolrQuerySerializer serializer, EventBus eventBus, String address) {
        super(query, serializer, eventBus, address);
        this.nextCursorMark = CursorMarkParams.CURSOR_MARK_START;
    }

    @Override
    protected ModifiableSolrParams setQueryStart(SolrQuery query) {

        return query.set(CursorMarkParams.CURSOR_MARK_PARAM, this.nextCursorMark);

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

        if (nextCursorMark == null || currCursorMark.equalsIgnoreCase(nextCursorMark)) {
            complete = true;
        }
        return complete;

    }
}
