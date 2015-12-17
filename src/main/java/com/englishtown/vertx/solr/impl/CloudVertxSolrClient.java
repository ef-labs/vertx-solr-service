package com.englishtown.vertx.solr.impl;

import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrRequest;
import org.apache.solr.client.solrj.impl.CloudSolrClient;
import org.apache.solr.common.params.ModifiableSolrParams;
import org.apache.solr.common.params.SolrParams;

import com.englishtown.vertx.solr.VertxSolrClient;
import com.englishtown.vertx.solr.actions.JsonResponseHandler;

import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;

public class CloudVertxSolrClient implements VertxSolrClient{

	protected ModifiableSolrParams invariantParams = new ModifiableSolrParams();
	protected String baseUrl;
	private static final String DEFAULT_PATH = "/select";
	protected SolrClient cloudServer = null;
	protected Vertx vertx = null;
	
	// TODO : add a constructor to accept solr options
	public CloudVertxSolrClient(Vertx vertx ,String zkHost){
		this.cloudServer = new CloudSolrClient(zkHost);
		this.vertx = vertx;
	}
	
	@Override
	public void setInvariantParams(SolrParams params) {
		 if (params != null) {
	            invariantParams.add(params);
	     }
	}

	public void request(SolrRequest request, Handler<AsyncResult<JsonObject>> resultHandler) {
        String path = request.getPath();
        if (path == null || !path.startsWith("/")) {
            path = DEFAULT_PATH;
        }

        ModifiableSolrParams params = new ModifiableSolrParams(request.getParams());
        if (invariantParams != null) {
            params.add(invariantParams);
        }

        new JsonResponseHandler(vertx, cloudServer, params).execute(resultHandler);
	}

	@Override
	public void stop() {
		// TODO Auto-generated method stub
		
	}

	

}
