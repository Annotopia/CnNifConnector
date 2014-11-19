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

import groovyx.net.http.ContentType
import groovyx.net.http.HTTPBuilder
import groovyx.net.http.Method

import org.annotopia.grails.connectors.BaseConnectorService
import org.annotopia.grails.connectors.ConnectorHttpResponseException
import org.annotopia.grails.connectors.IConnectorsParameters
import org.annotopia.grails.connectors.ITermSearchService
import org.annotopia.grails.connectors.ITextMiningService
import org.annotopia.grails.connectors.MiscUtils
import org.annotopia.grails.connectors.plugin.nif.services.converters.NifTermSearchConversionService
import org.annotopia.grails.connectors.plugin.nif.services.converters.NifTermSearchDomeoConversionService
import org.annotopia.grails.connectors.plugin.nif.services.converters.NifTextMiningConversionService
import org.annotopia.grails.connectors.plugin.nif.services.converters.NifTextMiningDomeoConversionService
import org.codehaus.groovy.grails.web.json.JSONObject

/**
 * Implementation of the Nif Term Search connector for Annotopia.
 * @author Tom Wilkin
 */
class NifService extends BaseConnectorService implements ITermSearchService, ITextMiningService {
	
	/** The URL to access the Term Search API. */
	private final static String TERM_SEARCH_URL = "http://neuinfo.org/servicesv1/v1/federation/data/";
	
	/** The URL to access the Text Mining API. */
	private final static String TEXT_MINING_URL = "http://beta.neuinfo.org/services/v1/annotate/entities";
	
	/** The configuration options for this service. */
	def connectorsConfigAccessService;

	@Override
	public JSONObject search(final String content, final HashMap parameters) {
				
		try {
			// create the URL
			String resource = parameters.get("resource");
			def url = TERM_SEARCH_URL + resource + "?exportType=all";
			if(content != null) {
				url += "&q=" + encodeContent(content);
			}
		
			// perform the query
			long startTime = System.currentTimeMillis( );
			try {
				def http = new HTTPBuilder(url);
				evaluateProxy(http, url);
	
				http.request(Method.GET, ContentType.JSON) {
					requestContentType = ContentType.URLENC
					
					response.success = { resp, json ->
						long duration = System.currentTimeMillis( ) - startTime;
						
						// convert the result
						boolean isFormatDefined = parameters.containsKey(IConnectorsParameters.RETURN_FORMAT);
						if(isFormatDefined && parameters.get(IConnectorsParameters.RETURN_FORMAT)
								.equals(NifTermSearchDomeoConversionService.RETURN_FORMAT))
						{
							return new NifTermSearchDomeoConversionService( ).convert(resource, json);
						} else {
							return new NifTermSearchConversionService( ).convert(resource, json, duration);
						}
					}
					
					response.'404' = { resp ->
						log.error('Not found: ' + resp.getStatusLine())
						throw new ConnectorHttpResponseException(resp, 404, 'Service not found. The problem has been reported')
					}
				 
					response.'503' = { resp ->
						log.error('Not available: ' + resp.getStatusLine())
						throw new ConnectorHttpResponseException(resp, 503, 'Service temporarily not available. Try again later.')
					}
					
					response.failure = { resp, xml ->
						log.error('failure: ' + resp.getStatusLine())
						throw new ConnectorHttpResponseException(resp, resp.getStatusLine())
					}
				}
			} catch (groovyx.net.http.HttpResponseException ex) {
				log.error("HttpResponseException: Service " + ex.getMessage())
				throw new RuntimeException(ex);
			} catch (java.net.ConnectException ex) {
				log.error("ConnectException: " + ex.getMessage())
				throw new RuntimeException(ex);
			}
		} catch(Exception e) {
			JSONObject returnMessage = new JSONObject();
			returnMessage.put("error", e.getMessage());
			log.error("Exception: " + e.getMessage() + " " + e.getClass().getName());
			return returnMessage;
		}
	}
	
	@Override
	public JSONObject textmine(final String resourceURI, final String content, 
		final HashMap parameters)
	{
		try {
			// create the URL
			def url = TEXT_MINING_URL + "?content=" + encodeContent(content);
			String contentText = encodeContent(content);
			
			// perform the query
			long startTime = System.currentTimeMillis( );
			try {
				def http = new HTTPBuilder(url);
				evaluateProxy(http, url);
				
				http.request(Method.GET, ContentType.JSON) {
					requestContentType = ContentType.URLENC;
					
					contentText = URLDecoder.decode(contentText, MiscUtils.DEFAULT_ENCODING);
					
					response.success = { resp, json ->
						long duration = System.currentTimeMillis( ) - startTime;
						
						// convert the result
						boolean isFormatDefined = parameters.containsKey(IConnectorsParameters.RETURN_FORMAT);
						if(isFormatDefined && parameters.get(IConnectorsParameters.RETURN_FORMAT)
							.equals(NifTextMiningDomeoConversionService.RETURN_FORMAT))
						{
							return new NifTextMiningDomeoConversionService( ).convert(json, resourceURI, contentText);
						} else {
							return new NifTextMiningConversionService( ).convert(json, resourceURI, contentText, duration);
						}
					}
					
					response.'404' = { resp ->
						log.error('Not found: ' + resp.getStatusLine())
						throw new ConnectorHttpResponseException(resp, 404, 'Service not found. The problem has been reported')
					}
				 
					response.'503' = { resp ->
						log.error('Not available: ' + resp.getStatusLine())
						throw new ConnectorHttpResponseException(resp, 503, 'Service temporarily not available. Try again later.')
					}
					
					response.failure = { resp, xml ->
						println "Unexpected error: ${resp.statusLine.statusCode} : ${resp.statusLine.reasonPhrase}"
						log.error('failure: ' + resp.getStatusLine())
						throw new ConnectorHttpResponseException(resp, resp.getStatusLine())
					}
				} 
			} catch (groovyx.net.http.HttpResponseException ex) {
				log.error("HttpResponseException: " + ex.getMessage())
				throw new RuntimeException(ex);
			} catch (java.net.ConnectException ex) {
				log.error("ConnectException: " + ex.getMessage())
				throw new RuntimeException(ex);
			}
		} catch(Exception e) {
			JSONObject returnMessage = new JSONObject();
			returnMessage.put("error", e.getMessage());
			log.error("Exception: " + e.getMessage() + " " + e.getClass().getName());
			return returnMessage;
		}
	}
};
