package com.englishtown.vertx.solr.streams.impl;

import com.englishtown.vertx.solr.SolrQuerySerializer;
import com.englishtown.vertx.solr.streams.ReadJsonStream;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.common.params.ModifiableSolrParams;
import org.vertx.java.core.eventbus.EventBus;
import org.vertx.java.core.json.JsonObject;

/**
 * Implementation of {@link com.englishtown.vertx.solr.streams.impl.BaseReadJsonStream} using an integer offset
 * to paginate json Solr results
 */
public class NewOffsetReadJsonStream extends BaseReadJsonStream implements ReadJsonStream<BaseReadJsonStream> {

    private int nextOffset;

    public NewOffsetReadJsonStream(SolrQuery query, SolrQuerySerializer serializer, EventBus eventBus, String address) {
        super(query, serializer, eventBus, address);
    }

    @Override
    protected ModifiableSolrParams setQueryStart(SolrQuery query) {

        return query.setStart(nextOffset);

    }

    @Override
    protected boolean isComplete(JsonObject reply) {

        boolean complete = false;
        int count = reply.getArray("docs").size();
        int numberFound = reply.getInteger("number_found");

        nextOffset += count;

        if (numberFound == nextOffset || count == 0) {
            complete = true;
        }
        return complete;

    }

}
