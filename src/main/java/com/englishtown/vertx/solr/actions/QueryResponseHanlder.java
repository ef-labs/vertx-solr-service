package com.englishtown.vertx.solr.actions;

import java.io.IOException;

import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.params.SolrParams;

import io.vertx.core.Vertx;

public class QueryResponseHanlder extends AbstractHandler<QueryResponse>{

	protected final SolrParams solrParams ;
	
	public QueryResponseHanlder(Vertx vertx, SolrClient solr, SolrParams solrParams){
		super(vertx, solr);
		this.solrParams = solrParams;
	}

	@Override
	protected QueryResponse execute(SolrClient client) throws SolrServerException, IOException {
			return client.query(solrParams);	
	}

	@Override
	protected String name() {
		// TODO Auto-generated method stub
		return null;
	}
	
}
