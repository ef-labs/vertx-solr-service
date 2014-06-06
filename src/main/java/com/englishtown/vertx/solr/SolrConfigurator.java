package com.englishtown.vertx.solr;

import org.apache.solr.client.solrj.SolrServer;

/**
 * Provides solr configuration
 */
public interface SolrConfigurator {

    SolrServer createSolrServer();

}
