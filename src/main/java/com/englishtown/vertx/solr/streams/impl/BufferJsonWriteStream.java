package com.englishtown.vertx.solr.streams.impl;

import io.vertx.core.Handler;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.JsonObject;
import io.vertx.core.streams.WriteStream;

/**
 * Write the JSON to a buffer stream
 */
public class BufferJsonWriteStream implements WriteStream<JsonObject> {

    private final WriteStream<Buffer> stream;

    public BufferJsonWriteStream(WriteStream<Buffer> stream) {
        this.stream = stream;
    }

    @Override
    public WriteStream<JsonObject> exceptionHandler(Handler<Throwable> handler) {
        stream.exceptionHandler(handler);
        return this;
    }

    @Override
    public WriteStream<JsonObject> write(JsonObject data) {
        stream.write(Buffer.buffer(data.encode()));
        return this;
    }

    @Override
    public WriteStream<JsonObject> setWriteQueueMaxSize(int maxSize) {
        stream.setWriteQueueMaxSize(maxSize);
        return this;
    }

    @Override
    public boolean writeQueueFull() {
        return stream.writeQueueFull();
    }

    @Override
    public WriteStream<JsonObject> drainHandler(Handler<Void> handler) {
        stream.drainHandler(handler);
        return this;
    }
}
