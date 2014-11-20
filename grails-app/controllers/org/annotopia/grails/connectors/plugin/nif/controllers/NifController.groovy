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

import org.annotopia.grails.connectors.BaseConnectorController;
import org.annotopia.grails.connectors.IConnectorsParameters;
import org.annotopia.grails.connectors.MiscUtils;
import org.codehaus.groovy.grails.web.json.JSONObject;

/** Controller to handle incoming queries to the NIF text mining service.
 * @author Tom Wilkin */
class NifController extends BaseConnectorController {

	/** Used by superclass **/
	def connectorsConfigAccessService;
	def configAccessService;
	
	/** The API key authentication service. */
	def apiKeyAuthenticationService;
	
	/** The instance of the NIF service to use for the queries. */
	def nifService;
	
	// curl -i -X GET http://localhost:8090/cn/nif/search --header "Content-Type: application/json" --data '{"q":"APP", "resource":"nif-0000-07730-1", "format":"domeo", "apiKey":"164bb0e0-248f-11e4-8c21-0800200c9a66"}'
	// curl -i -X GET http://localhost:8090/cn/nif/search --header "Content-Type: application/json" --data '{"q":"Rat", "resource":"nlx_154697-1", "format":"domeo", "apiKey":"164bb0e0-248f-11e4-8c21-0800200c9a66"}'
	// curl -i -X GET http://localhost:8090/cn/nif/search --header "Content-Type: application/json" --data '{"q":"max", "resource":"nlx_144509-1", "format":"domeo", "apiKey":"164bb0e0-248f-11e4-8c21-0800200c9a66"}'
	/** Perform a term search on NIF and return the JSON results. 
	 * @param resource The resource to perform the term search on.
	 * @param q The term to search for.
	 * @param format The output format to return. */
	def search = {
		long startTime = System.currentTimeMillis( );
		
		// retrieve the API key
		def apiKey = retrieveApiKey(startTime);
		if(!apiKey) {
			return;
		}
		
		// retrieve the return format
		def format = retrieveValue(request.JSON.format, params.format, "annotopia");
		
		// retrieve the resource
		def resource = retrieveValue(request.JSON.resource, params.resource, 
			"resource", startTime);
		if(!resource) {
			return;
		}
		
		// retrieve the query
		def query = retrieveValue(request.JSON.q, params.q, "q", startTime);
		if(!query) {
			return;
		}
		
		def type = retrieveValue(request.JSON.type, params.type, "");
		def vendor = retrieveValue(request.JSON.vendor, params.vendor, "");
		
		// perform the query
		HashMap parameters = new HashMap( );
		parameters.put(IConnectorsParameters.RETURN_FORMAT, format);
		parameters.put("resource", resource);
		parameters.put("vendor", vendor);
		parameters.put("type", type);
		
		JSONObject results = nifService.search(query, parameters);
		
		response.outputStream << results.toString( );
		response.outputStream.flush( );	
	}
	
	// curl -i -X POST http://localhost:8080/cn/nif/textmine --header "Content-Type: application/json" --data '{"apiKey":"testKey","text":"APP is bad for you","offset":"1","format":"annotopia"}'
	/** Perform text mining and return the JSON results.
	 * @param apiKey The API key to communicate with Annotopia.
	 * @param format The output format to return.
	 * @param text The text to text mine. */
	def textmine = {
		long startTime = System.currentTimeMillis( );
		
		// retrieve the API key
		def apiKey = retrieveApiKey(startTime);
		if(!apiKey) {
			return;
		}
		
		// retrieve the return format
		def format = retrieveValue(request.JSON.format, params.format, "annotopia");
		
		// retrieve the text to mine
		def text = retrieveValue(request.JSON.text, params.text, "text", startTime);
		if(!text) {
			return;
		}
		
		// perform the query
		HashMap parameters = new HashMap( );
		parameters.put(IConnectorsParameters.RETURN_FORMAT, format);
		JSONObject results = nifService.textmine(null, text, parameters);
		
		response.outputStream << results.toString( );
		response.outputStream.flush( );
	}
	
	/** Generate an error with the specified details.
	 * @param code The HTTP error code to use.
	 * @param message The error message to report to the user.
	 * @param startTime The time the web service call was initiated. */
	private void error(final int code, final String message, final long startTime) {
		log.warn(message);
		render(
			status: code,
			text: returnMessage("", "nocontent", message.replace("\"", "\\\""), startTime),
			contentType: "text/json",
			encoding: "UTF-8"
		);
	}
	
};
