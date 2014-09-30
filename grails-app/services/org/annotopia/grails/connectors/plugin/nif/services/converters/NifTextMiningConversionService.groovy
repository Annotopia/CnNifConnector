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
package org.annotopia.grails.connectors.plugin.nif.services.converters

import java.text.SimpleDateFormat
import java.util.regex.Matcher
import java.util.regex.Pattern

import org.annotopia.grails.connectors.converters.BaseTextMiningConversionService
import org.annotopia.grails.connectors.utils.UUID
import org.annotopia.grails.connectors.vocabularies.IOAccessRestrictions
import org.annotopia.grails.connectors.vocabularies.IODomeo
import org.annotopia.grails.connectors.vocabularies.IODublinCoreTerms
import org.annotopia.grails.connectors.vocabularies.IOFoaf
import org.annotopia.grails.connectors.vocabularies.IOJsonLd
import org.annotopia.grails.connectors.vocabularies.IOOpenAnnotation
import org.annotopia.grails.connectors.vocabularies.IOPav
import org.annotopia.grails.connectors.vocabularies.IORdfs
import org.codehaus.groovy.grails.web.json.JSONArray
import org.codehaus.groovy.grails.web.json.JSONObject

/** Convert the text mining results from the Nif service into the default format.
 * @author Tom Wilkin */
class NifTextMiningConversionService extends BaseTextMiningConversionService {

	/** The return format that this conversion service generates. */
	public static final String RETURN_FORMAT = "annotopia";
	
	/** Convert the returned JSON into the correct format returned by Annotopia.
	 * @param json The JSON response from the web service.
	 * @param resourceURI The URI for the resource that was accessed.
	 * @param contentText The content that was queried.
	 * @param duration The number of milliseconds that they query took.
	 * @return The JSON in the Annotopia format. */
	public JSONObject convert(def json, final String resourceURI, final String contentText,
			final long duration) 
	{
		String snippetUrn = URN_SNIPPET_PREFIX + UUID.uuid( );
		
		JSONObject result = new JSONObject( );
		result.put(IOJsonLd.jsonLdId, snippetUrn);
		result.put(IOJsonLd.jsonLdType, "ao:AnnotationSet");
		result.put(IORdfs.label, "NIF Annotator Results");
		result.put(IODublinCoreTerms.description, "NIF Annotator Results");
		result.put("ao:onResource", URN_SNIPPET_PREFIX + UUID.uuid( ));
		
		// agents
		JSONArray agents = new JSONArray( );
		// connector
		JSONObject connector = getConnectorAgent( );
		result.put(IOPav.importedOn, dateFormat.format(new Date( )));
		result.put(IOPav.importedBy, connector[IOJsonLd.jsonLdId]);
		agents.add(agents.size( ), connector);
		// annotator
		JSONObject annotator = getAnnotatorAgent( );
		result.put(IOPav.importedFrom, annotator[IOJsonLd.jsonLdId]);
		agents.add(agents.size( ), annotator);
		result.put(IODomeo.agents, agents);
		
		// permissions
		result.put(IOAccessRestrictions.permissions, getPublicPermissions( ));
		
		// resources
		JSONArray resources = new JSONArray( );
		JSONObject content = new JSONObject( );
		content.put(IOJsonLd.jsonLdId, snippetUrn);
		content.put(IOJsonLd.jsonLdType, IOOpenAnnotation.ContentAsText);
		content.put(IOOpenAnnotation.chars, contentText);
		content.put(IOPav.derivedFrom, resourceURI);
		resources.add(resources.size( ), content);
		result.put(IODomeo.resources, resources);
		
		// annotations
		JSONArray annotations = new JSONArray( );
		json.each {
			JSONObject selector = findOrCreateAndSaveSelectorUsingStringSearch(
				contentText, it["token"]["value"], it["start"], it["end"]);
			
			if(selector != null) {
				JSONObject annotation = new JSONObject( );
				annotation.put(IOJsonLd.jsonLdId, URN_ANNOTATION_PREFIX + UUID.uuid( ));
				annotation.put(IOJsonLd.jsonLdType, IOOpenAnnotation.Annotation);
				// body
				JSONObject body = new JSONObject( );
				body.put(IOJsonLd.jsonLdId, it["token"]["id"]);
				body.putAt(IOJsonLd.jsonLdType, IOOpenAnnotation.SemanticTag);
				annotation.put(IOOpenAnnotation.hasBody, body);
				// target
				JSONObject target = new JSONObject( );
				target.put(IOJsonLd.jsonLdId, URN_SPECIFIC_RESOURCE_PREFIX + UUID.uuid( ));
				target.put(IOJsonLd.jsonLdType, IOOpenAnnotation.SpecificResource);
				target.put(IOOpenAnnotation.hasSource, snippetUrn);
				target.put(IOOpenAnnotation.hasSelector, selector);
				annotation.put(IOOpenAnnotation.hasTarget, target);
				annotations.add(annotations.size( ), annotation);
			}	
		}
		result.put("items", annotations);
		
		return result;
	}
	

	
	/** Create the selector content.
	 * @param text The text to search the content for.
	 * @param match The string to match in the text content.
	 * @param start The start index from the web service.
	 * @param end The end index from the web service. 
	 * @return The entity created with the search results. */
	private JSONObject findOrCreateAndSaveSelectorUsingStringSearch(final String text, 
		final String match, final int start, final int end)
	{
		Map<String, Object> matches = searchForMatch(text, match, start);
		if(matches != null) {
			JSONObject selector = new JSONObject( );
			selector.put(IOJsonLd.jsonLdId, URN_SELECTOR_PREFIX + UUID.uuid( ));
			selector.put(IOJsonLd.jsonLdType, IOOpenAnnotation.TextQuoteSelector);
			selector.put(IOPav.createdOn, dateFormat.format(new Date( )));
			selector.put(IOOpenAnnotation.prefix, matches.prefix);
			selector.put(IOOpenAnnotation.exact, matches.exact);
			selector.put(IOOpenAnnotation.suffix, matches.suffix);
			return selector;
		}
		
		return null;
	}
	
	/** @return Create the connector agent content. */
	private JSONObject getConnectorAgent( ) {
		return getSoftwareAgent(
			"urn:domeo:software:service:ConnectorNifServer:0.1-001",
			"NifConnector",
			"NifConnector",
			"0.1 b001")		
	}
	
	/** @return Create the annotator agent content. */
	private JSONObject getAnnotatorAgent( ) {
		return getSoftwareAgent(
			"http://nif-services.neuinfo.org/servicesv1/resource_AnnotateService.html",
			"NIF Annotator Web Service",
			"NIF Annotator Web Service",
			"1.0");
	}
};
