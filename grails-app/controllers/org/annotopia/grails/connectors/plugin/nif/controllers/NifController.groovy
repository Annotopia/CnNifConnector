/*
 * Copyright 2014 Massachusetts General Hospital
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.annotopia.grails.connectors.plugin.nif.controllers;

import org.annotopia.grails.connectors.BaseConnectorController
import org.annotopia.grails.connectors.IConnectorsParameters
import org.codehaus.groovy.grails.web.json.JSONObject

/** Controller to handle incoming queries to the NIF text mining service.
 * @author Tom Wilkin */
class NifController extends BaseConnectorController {

	/** The instance of the NIF service to use for the queries. */
	def nifService;
	
	// curl -i -X GET http://localhost:8080/cn/nif/search --header "Content-Type: application/json" --data '{"q":"plank", "resource":"nlx_144509-1", "format":"domeo"}'
	/** Perform a term search on NIF and return the JSON results. 
	 * @param resource The resource to perform the term search on.
	 * @param q The term to search for.
	 * @param format The output format to return. */
	def search = {
		long startTime = System.currentTimeMillis( );
		
		// retrieve the return format
		def format;
		if(request.JSON.format != null) {
			format = request.JSON.format;
		} else if(params.format != null) {
			format = params.format;
		} else {
			format = "annotopia";
		}
		
		// retrieve the resource
		def resource;
		if(request.JSON.resource != null) {
			resource = request.JSON.resource;
		} else if(params.resource != null) {
			resource = params.resourcel
		}
		
		// retrieve the query
		def query;
		if(request.JSON.q != null) {
			query = request.JSON.q;
		} else if(params.q != null) {
			query = params.q;
		} else {
			query = "";
		}
		
		// perform the query
		if(query != null && !query.empty && resource != null && !resource.empty) {
			HashMap parameters = new HashMap( );
			parameters.put(IConnectorsParameters.RETURN_FORMAT, format);
			parameters.put("resource", resource);
			
			JSONObject results = nifService.search(query, parameters);
			
			response.outputStream << results.toString( );
			response.outputStream.flush( );
		} else {
			def message = "Query text is null.";
			render(
				status: 200,
				text: returnMessage("", "nocontent", message, startTime),
				contentType: "text/json",
				encoding: "UTF-8"
			);
			return;
		}		
	}
	
};
