package com.englishtown.vertx.solr.streams.impl;

import io.vertx.core.json.JsonObject;
import io.vertx.core.streams.ReadStream;
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
public class SolrPumpTest {

    private SolrPump solrPump;
    @Mock
    ReadStream<JsonObject> rs;
    @Mock
    WriteStream<JsonObject> ws;


    @Before
    public void setUp() throws Exception {
        solrPump = SolrPump.createPump(rs, ws);
    }

    @Test
    public void setWriteQueueMaxSizeTest() {
        solrPump.setWriteQueueMaxSize(anyInt());
        verify(ws).setWriteQueueMaxSize(anyInt());
    }

    @Test
    public void startTest() {
        solrPump.start();
        verify(rs).handler(anyObject());
    }

    @Test
    public void stopTest() {
        solrPump.stop();
        verify(ws).drainHandler(anyObject());
        verify(rs).handler(anyObject());
    }

    @Test
    public void createPumpTest() {
        SolrPump.createPump(rs, ws, anyInt());
        verify(ws).setWriteQueueMaxSize(anyInt());
    }

}