package com.englishtown.vertx.solr.streams.impl;

import com.englishtown.vertx.solr.streams.WriteJsonStream;
import org.vertx.java.core.Handler;

/**
 * Abstract default implementation of {@link com.englishtown.vertx.solr.streams.WriteJsonStream}
 */
public abstract class WriteJsonStreamBase<T extends WriteJsonStreamBase> implements WriteJsonStream<T> {

    protected Handler<Throwable> exceptionHandler;
    protected Handler<Void> drainHandler;

    protected WriteJsonStreamBase() {
    }

    @SuppressWarnings("unchecked")
    @Override
    public T drainHandler(Handler<Void> drainHandler) {
        this.drainHandler = drainHandler;
        return (T) this;
    }

    @SuppressWarnings("unchecked")
    @Override
    public T exceptionHandler(Handler<Throwable> exceptionHandler) {
        this.exceptionHandler = exceptionHandler;
        return (T) this;
    }

    public abstract T handleEnd();

}
