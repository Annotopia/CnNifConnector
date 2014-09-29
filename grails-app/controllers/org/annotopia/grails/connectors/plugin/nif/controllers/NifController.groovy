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

import org.annotopia.grails.connectors.BaseController;
import org.annotopia.grails.connectors.IConnectorsParameters;
import org.annotopia.grails.connectors.MiscUtils;
import org.codehaus.groovy.grails.web.json.JSONObject;

/** Controller to handle incoming queries to the NIF text mining service.
 * @author Tom Wilkin */
class NifController extends BaseController {

	/** The API key authentication service. */
	def apiKeyAuthenticationService;
	
	/** The instance of the NIF service to use for the queries. */
	def nifService;
	
	// curl -i -X GET http://localhost:8080/cn/nif/search --header "Content-Type: application/json" --data '{"q":"plank", "resource":"nlx_144509-1", "format":"domeo"}'
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
		
		// perform the query
		HashMap parameters = new HashMap( );
		parameters.put(IConnectorsParameters.RETURN_FORMAT, format);
		parameters.put("resource", resource);
		
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
	
	/** Retrieve the API key value.
	 * @param startTime The time this execution was started.
	 * @return The value of the API key, or null if it is not set. */
	private String retrieveApiKey(final long startTime) {
		// retrieve the API key
		def apiKey = retrieveValue(request.JSON.apiKey, params.apiKey, 
				"Missing required parameter 'apiKey'.", startTime);
		if(!apiKeyAuthenticationService.isApiKeyValid(request.getRemoteAddr( ), apiKey)) {
			invalidApiKey(request.getRemoteAddr( ));
			return null;
		}
		
		return apiKey;
	}
	
	/** Retrieve the user parameter.
	 * @param requestParam The parameter if it exists in the POST data.
	 * @param param The parameter if it exists in the URL.
	 * @param name The name of this parameter.
	 * @param startTime The time this execution was started.
	 * @return The value of this parameter, or null if it is not set. */
	private String retrieveValue(final String requestParam, final String param, final String name, 
			final long startTime)
	{
		def result;
		if(requestParam != null) {
			result = requestParam;
		} else if(param != null) {
			result = URLDecoder.decode(param, MiscUtils.DEFAULT_ENCODING);
		} else {
			error(400, "Missing required parameter '" + name + "'.", startTime);
			return null;
		}
		
		if(result.isEmpty( )) {
			error(400, "Parameter '" + name + "' is empty.", startTime);
		}
		return result;
	}
	
	/** Retrieve the user parameter.
	 * @param requestParam The parameter if it exists in the POST data.
	 * @param param The parameter if it exists in the URL.
	 * @param defaultValue The default value to use if the parameter is not set.
	 * @return The value of this parameter. */
	private String retrieveValue(final String requestParam, final String param, 
			final String defaultValue)
	{
		if(requestParam != null) {
			return requestParam;
		} else if(param != null) {
			return URLDecoder.decode(param, MiscUtils.DEFAULT_ENCODING);
		} 
		return defaultValue;
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
