package com.englishtown.vertx.solr.streams.impl;

import io.vertx.core.Handler;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.JsonObject;
import io.vertx.core.streams.WriteStream;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class BufferJsonWriteStreamTest {

    private BufferJsonWriteStream bufferJsonWriteStream;
    @Mock
    WriteStream<Buffer> stream;
    @Mock
    Handler<Throwable> exceptionHandler;

    @Before
    public void setUp() {
        bufferJsonWriteStream = new BufferJsonWriteStream(stream);
    }

    @Test
    public void testExceptionHandler() throws Exception {
        bufferJsonWriteStream.exceptionHandler(exceptionHandler);
        verify(stream).exceptionHandler(exceptionHandler);
    }

    @Test
    public void testWrite() throws Exception {
        JsonObject jsonObject = new JsonObject().put("key", "value");
        bufferJsonWriteStream.write(jsonObject);
    }

    @Test
    public void testSetWriteQueueMaxSize() throws Exception {
        bufferJsonWriteStream.setWriteQueueMaxSize(anyInt());
        verify(stream).setWriteQueueMaxSize(anyInt());
    }

    @Test
    public void testWriteQueueFull() throws Exception {
        bufferJsonWriteStream.writeQueueFull();
        verify(stream).writeQueueFull();
    }

    @Test
    public void testDrainHandler() throws Exception {
        bufferJsonWriteStream.drainHandler(anyObject());
        verify(stream).drainHandler(anyObject());
    }
}