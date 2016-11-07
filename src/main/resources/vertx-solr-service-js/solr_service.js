/*
 * Copyright 2014 Red Hat, Inc.
 *
 * Red Hat licenses this file to you under the Apache License, version 2.0
 * (the "License"); you may not use this file except in compliance with the
 * License.  You may obtain a copy of the License at:
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */

/** @module vertx-solr-service-js/solr_service */
var utils = require('vertx-js/util/utils');
var Vertx = require('vertx-js/vertx');

var io = Packages.io;
var JsonObject = io.vertx.core.json.JsonObject;
var JSolrService = com.englishtown.vertx.solr.SolrService;
var QueryOptions = com.englishtown.vertx.solr.QueryOptions;

/**
 Vert.x solr service

 @class
*/
var SolrService = function(j_val) {

  var j_solrService = j_val;
  var that = this;

  /**

   @public

   */
  this.start = function() {
    var __args = arguments;
    if (__args.length === 0) {
      j_solrService["start()"]();
    } else throw new TypeError('function invoked with invalid arguments');
  };

  /**

   @public

   */
  this.stop = function() {
    var __args = arguments;
    if (__args.length === 0) {
      j_solrService["stop()"]();
    } else throw new TypeError('function invoked with invalid arguments');
  };

  /**

   @public
   @param query {Object} 
   @param options {Object} 
   @param resultHandler {function} 
   */
  this.query = function(query, options, resultHandler) {
    var __args = arguments;
    if (__args.length === 3 && (typeof __args[0] === 'object' && __args[0] != null) && (typeof __args[1] === 'object' && __args[1] != null) && typeof __args[2] === 'function') {
      j_solrService["query(io.vertx.core.json.JsonObject,com.englishtown.vertx.solr.QueryOptions,io.vertx.core.Handler)"](utils.convParamJsonObject(query), options != null ? new QueryOptions(new JsonObject(JSON.stringify(options))) : null, function(ar) {
      if (ar.succeeded()) {
        resultHandler(utils.convReturnJson(ar.result()), null);
      } else {
        resultHandler(null, ar.cause());
      }
    });
    } else throw new TypeError('function invoked with invalid arguments');
  };

  // A reference to the underlying Java delegate
  // NOTE! This is an internal API and must not be used in user code.
  // If you rely on this property your code is likely to break if we change it / remove it without warning.
  this._jdel = j_solrService;
};

/**

 @memberof module:vertx-solr-service-js/solr_service
 @param vertx {Vertx} 
 @param address {string} 
 @return {SolrService}
 */
SolrService.createEventBusProxy = function(vertx, address) {
  var __args = arguments;
  if (__args.length === 2 && typeof __args[0] === 'object' && __args[0]._jdel && typeof __args[1] === 'string') {
    return utils.convReturnVertxGen(JSolrService["createEventBusProxy(io.vertx.core.Vertx,java.lang.String)"](vertx._jdel, address), SolrService);
  } else throw new TypeError('function invoked with invalid arguments');
};

// We export the Constructor function
module.exports = SolrService;