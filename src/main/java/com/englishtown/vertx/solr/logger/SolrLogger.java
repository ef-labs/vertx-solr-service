package com.englishtown.vertx.solr.logger;

/**
 * Logger for Solr
 */
public interface SolrLogger {

    public void debug(String message, Object... params);

    public void info(String message, Object... params);

    public void warn(String message, Object... params);

    public void error(String message, Object... params);

}
