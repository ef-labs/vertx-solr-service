package com.englishtown.vertx.solr;

import io.vertx.core.AbstractVerticle;
import io.vertx.serviceproxy.ProxyHelper;

import javax.inject.Inject;

/**
 * Solr client worker verticle
 */
public class SolrServiceVerticle extends AbstractVerticle {

    public static final String DEFAULT_ADDRESS = "et.solr";

    public final SolrService service;

    @Inject
    public SolrServiceVerticle(SolrService solrService) {
        service = solrService;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void start() throws Exception {
        String address = config().getString("address");
        if (address == null || address.isEmpty()) {
            throw new IllegalStateException("address field must be specified in config for service verticle");
        }

        // Register service as an event bus proxy
        ProxyHelper.registerService(SolrService.class, vertx, service, address);

        // Start the service
        service.start();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void stop() throws Exception {
        service.stop();
    }


}
