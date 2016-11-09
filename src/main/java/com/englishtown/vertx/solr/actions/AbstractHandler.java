/*
 * Copyright (c) 2011-2014 The original author or authors
 * ------------------------------------------------------
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * and Apache License v2.0 which accompanies this distribution.
 *
 *     The Eclipse Public License is available at
 *     http://www.eclipse.org/legal/epl-v10.html
 *
 *     The Apache License v2.0 is available at
 *     http://www.opensource.org/licenses/apache2.0.php
 *
 * You may elect to redistribute this code under either of these licenses.
 */

package com.englishtown.vertx.solr.actions;

import java.io.IOException;

import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrServerException;

import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;

/**
 * @author <a href="mailto:nscavell@redhat.com">Nick Scavelli</a>
 */
public abstract class AbstractHandler<T> implements Handler<Future<T>> {

//  private static final Logger log = LoggerFactory.getLogger(AbstractHandler.class);

  protected final Vertx vertx;
  protected final SolrClient client;

  protected AbstractHandler(Vertx vertx, SolrClient client) {
    this.vertx = vertx;
    this.client = client;
  }

  @Override
  public void handle(Future<T> future) {
   	try {
		T result = execute(client);
		 future.complete(result);
	} catch (SolrServerException | IOException e) {
		future.fail(e);
	}
     ;
     
  }

  public void execute(Handler<AsyncResult<T>> resultHandler) {
    vertx.executeBlocking(this, resultHandler);
  }

  protected abstract T execute(SolrClient client) throws SolrServerException, IOException;

  protected abstract String name();
}
