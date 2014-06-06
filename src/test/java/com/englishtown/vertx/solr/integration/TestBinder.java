package com.englishtown.vertx.solr.integration;

import com.englishtown.vertx.solr.SolrConfigurator;
import com.englishtown.vertx.solr.hk2.SolrBinder;
import org.glassfish.hk2.utilities.binding.AbstractBinder;

/**
 * HK2 binder for integration tests to avoid calling solr
 */
public class TestBinder extends AbstractBinder {
    /**
     * Implement to provide binding definitions using the exposed binding
     * methods.
     */
    @Override
    protected void configure() {

        install(new SolrBinder());

        // Replace configurator with mock
        bind(MockSolrConfigurator.class).to(SolrConfigurator.class).ranked(10);

    }
}
