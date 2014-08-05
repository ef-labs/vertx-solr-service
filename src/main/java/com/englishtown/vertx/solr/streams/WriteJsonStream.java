package com.englishtown.vertx.solr.streams;

import org.vertx.java.core.json.JsonObject;
import org.vertx.java.core.streams.DrainSupport;
import org.vertx.java.core.streams.ExceptionSupport;

/**
 * Represents a stream of Json data that can be written to<p>
 * Any class that implements this interface can be used by a {@link com.englishtown.vertx.solr.streams.impl.SolrPump} to pump data to it.<p>
 * This interface exposes a fluent api and the type T represents the type of the object that implements
 * the interface to allow method chaining
 */
public interface WriteJsonStream<T> extends ExceptionSupport<T>, DrainSupport<T> {

    /**
     * Write some data to the stream. The data is put on an internal write queue, and the write actually happens
     * asynchronously. To avoid running out of memory by putting too much on the write queue,
     * check the {@link #writeQueueFull} method before writing. This is done automatically if using a
     * {@link com.englishtown.vertx.solr.streams.impl.SolrPump}.
     *
     * @param jsonObject JsonObject we write to
     * @return Returns this
     */
    T write(JsonObject jsonObject);
}
