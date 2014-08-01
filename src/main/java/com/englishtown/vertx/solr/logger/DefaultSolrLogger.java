package com.englishtown.vertx.solr.logger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Default implementation of {@link com.englishtown.vertx.solr.logger.SolrLogger}
 */
public class DefaultSolrLogger implements SolrLogger {

    private Logger logger;

    public DefaultSolrLogger(Class clazz) {
        logger = LoggerFactory.getLogger(clazz);
    }

    @Override
    public void debug(String message, Object... params) {
        logger.debug(message, params);
    }

    @Override
    public void info(String message, Object... params) {
        logger.info(message, params);
    }

    @Override
    public void warn(String message, Object... params) {
        logger.warn(message, params);
    }

    @Override
    public void error(String message, Object... params) {
        logger.error(message, params);
    }

}
