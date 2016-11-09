package com.englishtown.vertx.solr.actions;

import java.io.IOException;

import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.params.SolrParams;

import com.englishtown.vertx.solr.utils.JsonUtils;

import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;

public class JsonResponseHandler extends AbstractHandler<JsonObject>{

	protected final SolrParams solrParams ;
	
	public JsonResponseHandler(Vertx vertx, SolrClient solr, SolrParams solrParams){
		super(vertx, solr);
		this.solrParams = solrParams;
	}
	@Override
	protected JsonObject execute(SolrClient client) throws SolrServerException, IOException {
		QueryResponse response = client.query(solrParams);
		
		if (null != response && null != response.getResults() && response.getResults().isEmpty()){
			return new JsonObject();
		}else{
			System.out.println("inside json decode");
//			return new JsonObject(JsonUtils.toJson(response.getResults()));
			return new JsonObject(JsonUtils.toJson(new TestPojo(response.getResults())));
			//return new JsonObject().put("response", JsonUtils.toJson(response.getResults()));
//			return new JsonObject().put("response", response.getResults().);
			// TODO : json encode is failing.. working on it.
		}
/*		JsonObject json = new JsonObject();
		json.put("respone", client.query(solrParams).getResults().toString());
		return json;
*/	}

	@Override
	protected String name() {
		// TODO Auto-generated method stub
		return null;
	}

}
