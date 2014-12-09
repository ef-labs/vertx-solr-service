package com.englishtown.vertx.solr.streams.impl;

import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;
import io.vertx.core.streams.ReadStream;
import io.vertx.core.streams.WriteStream;

/**
 * A Solr implementation of the vertx Pump using JsonObjects instead of Buffers
 */
public class SolrPump {

    private WriteStream<JsonObject> writeJsonStream;
    private ReadStream<JsonObject> readJsonStream;
    private int pumped;

    /**
     * Pumps data from a {@link io.vertx.core.streams.ReadStream} to a {@link io.vertx.core.streams.WriteStream}
     * and performs flow control where necessary to prevent the write stream buffer from getting overfull.<p>
     * Instances of this class read bytes from a {@link io.vertx.core.streams.ReadStream} and write them to
     * a {@link io.vertx.core.streams.WriteStream}.
     * This class can be used to pump from any {@link io.vertx.core.streams.ReadStream} to any {@link io.vertx.core.streams.WriteStream},
     * e.g. from an {@link OffsetJsonReadStream} to a {@link io.vertx.core.json.JsonObject} {@link io.vertx.core.streams.WriteStream},
     * <p>
     * Instances of this class are not thread-safe.
     * <p>
     * <code>
     * private final WriteStream&lt;JsonObject&gt; writeStream;
     * private final ReadStream&lt;JsonObject&gt; readJsonStream;
     * private int pumped;
     * </code>
     * <p>
     * /**
     * Create a new {@code SolrDataPump} with the given {@code ReadJsonStream} and {@code WriteJsonStream}
     *
     * @param rs ReadStream
     * @param ws WriteStream
     * @return Returns this
     */
    public static SolrPump createPump(ReadStream<JsonObject> rs, WriteStream<JsonObject> ws) {
        return new SolrPump(rs, ws);
    }

    /**
     * Create a new {@code SolrDataPump} with the given {@code ReadJsonStream} and {@code WriteJsonStream} and
     * {@code writeQueueMaxSize}
     *
     * @param rs                ReadStream
     * @param ws                WriteStream
     * @param writeQueueMaxSize Maximum allowed size of the write queue
     * @return Returns this
     */
    public static SolrPump createPump(ReadStream<JsonObject> rs, WriteStream<JsonObject> ws, int writeQueueMaxSize) {
        return new SolrPump(rs, ws, writeQueueMaxSize);
    }

    /**
     * Set the write queue max size to {@code writeQueueMaxSize}
     *
     * @param maxSize Maximum size to set for the write queue
     * @return Returns this
     */
    public SolrPump setWriteQueueMaxSize(int maxSize) {
        this.writeJsonStream.setWriteQueueMaxSize(maxSize);
        return this;
    }

    /**
     * Start the Pump. The Pump can be started and stopped multiple times.
     *
     * @return Returns this
     */
    public SolrPump start() {
        readJsonStream.handler(dataHandler);
        return this;
    }

    /**
     * Stop the SolrDataPump. The SolrDataPump can be started and stopped multiple times.
     *
     * @return Returns this
     */
    public SolrPump stop() {
        writeJsonStream.drainHandler(null);
        readJsonStream.handler(null);
        return this;
    }

    /**
     * Return the total number of Json objects pumped by this pump.
     *
     * @return Returns this
     */
    public int objectsPumped() {
        return pumped;
    }

    private final Handler<Void> drainHandler = new Handler<Void>() {
        public void handle(Void v) {
            readJsonStream.resume();
        }
    };

    // defining our data handler, which is a functional object (a parameter that has a method attached to it)
    // passed into the start() method for the Pump
    private final Handler<JsonObject> dataHandler = (JsonObject jsonObject) -> {
            writeJsonStream.write(jsonObject);
            pumped++;
            if (writeJsonStream.writeQueueFull()) {
                readJsonStream.pause();
                writeJsonStream.drainHandler(drainHandler);
            }
    };

    /**
     * Create a new {@code Pump} with the given {@code WriteJsonStream}. Set the write queue max size
     * of the write stream to {@code maxWriteQueueSize}
     */
    private SolrPump(ReadStream<JsonObject> rs, WriteStream<JsonObject> ws, int maxWriteQueueSize) {
        this(rs, ws);
        this.writeJsonStream.setWriteQueueMaxSize(maxWriteQueueSize);
    }

    private SolrPump(ReadStream<JsonObject> rs, WriteStream<JsonObject> ws) {
        this.readJsonStream = rs;
        this.writeJsonStream = ws;
    }

}



