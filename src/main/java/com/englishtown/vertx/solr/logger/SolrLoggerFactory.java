package com.englishtown.vertx.solr.logger;

import java.util.HashMap;
import java.util.Map;

/**
 * LoggerFactory for Solr
 */

public class SolrLoggerFactory {

    private static Map<Class, SolrLogger> loggers = new HashMap<>();

    public static SolrLogger getSolrLogger(Class clazz) {
        if (!loggers.containsKey(clazz)) {
            loggers.put(clazz, new DefaultSolrLogger(clazz));
        }

        return loggers.get(clazz);
    }
}
