package com.englishtown.vertx.solr.streams.impl;

import com.englishtown.vertx.solr.streams.ReadJsonStream;
import com.englishtown.vertx.solr.streams.WriteJsonStream;
import org.vertx.java.core.Handler;
import org.vertx.java.core.json.JsonObject;

/**
 * A Solr implementation of the vertx Pump using JsonObjects instead of Buffers
 */
public class SolrPump {

    private final WriteJsonStream<?> writeJsonStream;
    private final ReadJsonStream<?> readJsonStream;
    private int pumped;

    /**
     * Pumps data from a {@link com.englishtown.vertx.solr.streams.ReadJsonStream} to a {@link com.englishtown.vertx.solr.streams.WriteJsonStream}
     * and performs flow control where necessary to prevent the write stream buffer from getting overfull.<p>
     * Instances of this class read bytes from a {@link com.englishtown.vertx.solr.streams.ReadJsonStream} and write them to
     * a {@link com.englishtown.vertx.solr.streams.WriteJsonStream}.
     * This class can be used to pump from any {@link com.englishtown.vertx.solr.streams.ReadJsonStream} to any {@link com.englishtown.vertx.solr.streams.WriteJsonStream},
     * e.g. from an {@link com.englishtown.vertx.solr.streams.impl.OffsetReadJsonStream} to an {@link WriteJsonStreamBase},
     * <p>
     * Instances of this class are not thread-safe.<p>
     * <p>
     * <p>
     * private final WriteJsonStream<?> writeStream;
     * private final ReadJsonStream<?> readJsonStream;
     * private int pumped;
     * <p>
     *
     * /**
     * Create a new {@code SolrDataPump} with the given {@code ReadJsonStream} and {@code WriteJsonStream}
     *
     * @param rs ReadStream
     * @param ws WriteStream
     *
     * @return Returns this
     */
    public static SolrPump createPump(ReadJsonStream<?> rs, WriteJsonStream<?> ws) {
        return new SolrPump(rs, ws);
    }

    /**
     * Create a new {@code SolrDataPump} with the given {@code ReadJsonStream} and {@code WriteJsonStream} and
     * {@code writeQueueMaxSize}
     *
     * @param rs ReadStream
     * @param ws WriteStream
     * @param writeQueueMaxSize Maximum allowed size of the write queue
     * @return Returns this
     *
     */
    public static SolrPump createPump(ReadJsonStream<?> rs, WriteJsonStream<?> ws, int writeQueueMaxSize) {
        return new SolrPump(rs, ws, writeQueueMaxSize);
    }

    /**
     * Set the write queue max size to {@code writeQueueMaxSize}
     *
     * @param maxSize Maximum size to set for the write queue
     *
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
        readJsonStream.dataHandler(dataHandler);
        return this;
    }

    /**
     * Stop the SolrDataPump. The SolrDataPump can be started and stopped multiple times.
     *
     * @return Returns this
     */
    public SolrPump stop() {
        writeJsonStream.drainHandler(null);
        readJsonStream.dataHandler(null);
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
    private final Handler<JsonObject> dataHandler = new Handler<JsonObject>() {
        public void handle(JsonObject jsonObject) {
            writeJsonStream.write(jsonObject);
            pumped++;
            if (writeJsonStream.writeQueueFull()) {
                readJsonStream.pause();
                writeJsonStream.drainHandler(drainHandler);
            }
        }
    };

    /**
     * Create a new {@code Pump} with the given {@code WriteJsonStream}. Set the write queue max size
     * of the write stream to {@code maxWriteQueueSize}
     *
     */
    private SolrPump(ReadJsonStream<?> rs, WriteJsonStream<?> ws, int maxWriteQueueSize) {
        this(rs, ws);
        this.writeJsonStream.setWriteQueueMaxSize(maxWriteQueueSize);
    }

    private SolrPump(ReadJsonStream<?> rs, WriteJsonStream<?> ws) {
        this.readJsonStream = rs;
        this.writeJsonStream = ws;
    }

}



