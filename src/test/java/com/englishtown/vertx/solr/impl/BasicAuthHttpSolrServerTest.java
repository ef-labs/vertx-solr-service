package com.englishtown.vertx.solr.impl;

import org.apache.http.Header;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.impl.HttpClientUtil;
import org.apache.solr.client.solrj.request.QueryRequest;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class BasicAuthHttpSolrServerTest {

    private String baseUrl = "http://localhost:8983/solr";
    private BasicAuthHttpSolrServer solrServer;

    @Before
    public void setUp() throws Exception {
        solrServer = new BasicAuthHttpSolrServer(baseUrl);
    }

    @Test
    public void testCreateMethod() throws Exception {

        String username = "test_user";
        String password = "test_password";

        SolrQuery query = new SolrQuery();
        query.add(HttpClientUtil.PROP_BASIC_AUTH_USER, username);
        query.add(HttpClientUtil.PROP_BASIC_AUTH_PASS, password);

        QueryRequest queryRequest = new QueryRequest(query);

        HttpRequestBase request = solrServer.createMethod(queryRequest);

        // Ensure authorization header is set
        Header header = request.getFirstHeader("Authorization");
        assertEquals("Basic dGVzdF91c2VyOnRlc3RfcGFzc3dvcmQ=", header.getValue());

        // Ensure user/password have been removed from query params
        assertNull(queryRequest.getParams().get(HttpClientUtil.PROP_BASIC_AUTH_USER));
        assertNull(queryRequest.getParams().get(HttpClientUtil.PROP_BASIC_AUTH_PASS));

        String qs = request.getURI().getQuery();
        assertEquals(-1, qs.indexOf(HttpClientUtil.PROP_BASIC_AUTH_USER));
        assertEquals(-1, qs.indexOf(HttpClientUtil.PROP_BASIC_AUTH_PASS));

    }

}