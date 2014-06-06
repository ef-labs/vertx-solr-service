package com.englishtown.vertx.solr.hk2;

import com.englishtown.vertx.solr.SolrConfigurator;
import com.englishtown.vertx.solr.SolrQuerySerializer;
import com.englishtown.vertx.solr.impl.DefaultSolrQuerySerializer;
import com.englishtown.vertx.solr.impl.JsonConfigSolrConfigurator;
import org.glassfish.hk2.utilities.binding.AbstractBinder;

import javax.inject.Singleton;

/**
 * HK2 binder for solr
 */
public class SolrBinder extends AbstractBinder {
    /**
     * Implement to provide binding definitions using the exposed binding
     * methods.
     */
    @Override
    protected void configure() {

        bind(JsonConfigSolrConfigurator.class).to(SolrConfigurator.class).in(Singleton.class);
        bind(DefaultSolrQuerySerializer.class).to(SolrQuerySerializer.class).in(Singleton.class);

    }
}
