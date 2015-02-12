package com.englishtown.vertx.solr;

/**
 * Provides solr configuration
 */
public interface SolrConfigurator {

    VertxSolrServer createSolrServer();

}
