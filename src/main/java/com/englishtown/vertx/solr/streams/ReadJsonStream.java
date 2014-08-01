package com.englishtown.vertx.solr.streams;

import org.vertx.java.core.Handler;
import org.vertx.java.core.json.JsonObject;
import org.vertx.java.core.streams.ReadSupport;

/**
 * Represents a stream of Json data that can be read from.<p>
 * <p>
 * This interface exposes a fluent api and the type T represents the type of the object that implements
 * the interface to allow method chaining
 */
public interface ReadJsonStream<T> extends ReadSupport<T, JsonObject> {

    /**
     * Set an end handler. Once the stream has ended, and there is no more data to be read, this handler will be called.
     */
    T endHandler(Handler<Void> endHandler);

}
