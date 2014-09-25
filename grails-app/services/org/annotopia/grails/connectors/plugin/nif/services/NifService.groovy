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
package org.annotopia.grails.connectors.plugin.nif.services

import groovy.json.JsonSlurper
import groovyx.net.http.ContentType
import groovyx.net.http.HTTPBuilder
import groovyx.net.http.Method

import java.util.HashMap;

import org.annotopia.grails.connectors.BaseConnectorService;
import org.annotopia.grails.connectors.ConnectorsConfigAccessService
import org.annotopia.grails.connectors.IConnectorsParameters
import org.annotopia.grails.connectors.ITermSearchService
import org.annotopia.grails.connectors.plugin.nif.services.converters.NifTermSearchConversionService
import org.annotopia.grails.connectors.plugin.nif.services.converters.NifTermSearchDomeoConversionService
import org.apache.http.HttpHost
import org.apache.http.conn.params.ConnRoutePNames
import org.codehaus.groovy.grails.web.json.JSONObject;

/**
 * Implementation of the Nif Term Search connector for Annotopia.
 * @author Tom Wilkin
 */
class NifService extends BaseConnectorService implements ITermSearchService {
	
	/** The URL to access the Term Search API. */
	private final static String TERM_SEARCH_URL = "http://neuinfo.org/servicesv1/v1/federation/data/";
	
	/** The configuration options for this service. */
	def connectorsConfigAccessService;

	@Override
	public JSONObject search(final String content, final HashMap parameters) {
		
		// create the URL
		String resource = parameters.get("resource");
		def url = TERM_SEARCH_URL + resource + "?exportType=all";
		if(content != null) {
			url += "&q=" + content;
		}
		
		long startTime = System.currentTimeMillis( );
		try {
			def http = new HTTPBuilder(url);
			evaluateProxy(http, url);
			
			http.request(Method.GET, ContentType.JSON) {
				requestContentType = ContentType.URLENC
				
				response.success = { resp, json ->
					long duration = System.currentTimeMillis( ) - startTime;
					
					boolean isFormatDefined = parameters.containsKey(IConnectorsParameters.RETURN_FORMAT);
					if(isFormatDefined && parameters.get(IConnectorsParameters.RETURN_FORMAT)
							.equals(NifTermSearchDomeoConversionService.RETURN_FORMAT))
					{
						return new NifTermSearchDomeoConversionService( ).convert(json);
					} else {
						return new NifTermSearchConversionService( ).convert(json, duration);
					}
				}
			}
		} catch(Exception e) {
			e.printStackTrace( );
			return null;
		}
	}
};
