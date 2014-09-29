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

import java.text.SimpleDateFormat;
import java.util.regex.Matcher
import java.util.regex.Pattern
import org.annotopia.grails.connectors.utils.UUID;
import org.annotopia.grails.connectors.vocabularies.IOAccessRestrictions;
import org.annotopia.grails.connectors.vocabularies.IODomeo;
import org.annotopia.grails.connectors.vocabularies.IODublinCoreTerms;
import org.annotopia.grails.connectors.vocabularies.IOFoaf;
import org.annotopia.grails.connectors.vocabularies.IOJsonLd;
import org.annotopia.grails.connectors.vocabularies.IOOpenAnnotation;
import org.annotopia.grails.connectors.vocabularies.IOPav;
import org.annotopia.grails.connectors.vocabularies.IORdfs;
import org.codehaus.groovy.grails.web.json.JSONArray;
import org.codehaus.groovy.grails.web.json.JSONObject;

/** Convert the text mining results from the Nif service into the default format.
 * @author Tom Wilkin */
class NifTextMiningConversionService {

	/** The return format that this conversion service generates. */
	public static final String RETURN_FORMAT = "annotopia";
	
	/** Constants for the URNs. */
	private static final String URN_SNIPPET_PREFIX = "urn:domeo:contentsnippet:uuid:";
	private static final String URN_ANNOTATION_SET_PREFIX = "urn:domeo:annotationset:uuid:";
	private static final String URN_ANNOTATION_PREFIX = "urn:domeo:annotation:uuid:";
	private static final String URN_SPECIFIC_RESOURCE_PREFIX = "urn:domeo:specificresource:uuid:";
	private static final String URN_SELECTOR_PREFIX = "urn:domeo:selector:uuid:";
	
	/* Maximum lengths for prefixes and suffixes. */
	private static final int MAX_LENGTH_PREFIX_AND_SUFFIX = 50;
	
	/** The date format for the creation date stamps. */
	private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss Z");
	
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
	
	/** Search for a match in the selected text.
	 * @param textToAnnotate The text to search within.
	 * @param putativeExactMatch The exact text to match.
	 * @param start The start index for the search.
	 * @return The matches, or null if it cannot be found. */
	private def searchForMatch(final String textToAnnotate, final String putativeExactMatch, 
			final int start)
	{
		String matchRegex = putativeExactMatch.replaceAll(/\s+/,"\\\\s+")
		matchRegex = matchRegex.replaceAll("[)]", "\\\\)")
		matchRegex = matchRegex.replaceAll("[(]", "\\\\(")
		Pattern pattern = Pattern.compile("\\b${matchRegex}\\b", Pattern.MULTILINE | Pattern.CASE_INSENSITIVE)
		Matcher matcher = pattern.matcher(textToAnnotate)
		int startPos = -1
		int endPos = -1
		if (matcher.find(start)) {
			println 'in'
			startPos = matcher.start()
			endPos = matcher.end()
			String exactMatch = textToAnnotate[startPos..endPos - 1]
			
			String prefix = null;
			if(startPos == 0) {
				prefix = '';
			} else {
				 prefix = textToAnnotate.getAt([
					 Math.max(startPos - (MAX_LENGTH_PREFIX_AND_SUFFIX + 1), 0)..Math.max(0, startPos - 1)
				])
			}
			
			String suffix = null;
			if(Math.min(endPos, textToAnnotate.length() - 1)==Math.min(startPos + MAX_LENGTH_PREFIX_AND_SUFFIX, textToAnnotate.length()-1)) {
				suffix = "";
			} else {
				suffix = textToAnnotate.getAt([
					Math.min(endPos, textToAnnotate.length() - 1)..Math.min(startPos + MAX_LENGTH_PREFIX_AND_SUFFIX, textToAnnotate.length()-1)
				])
			}
			
			return ['offset':startPos,'prefix': prefix, 'exact': exactMatch, 'suffix': suffix]
		}else{
			println 'out'
			return null
		}

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
		JSONObject result = new JSONObject( );
		result.put(IOJsonLd.jsonLdId, "urn:domeo:software:service:ConnectorNifServer:0.1-001");
		result.put(IOJsonLd.jsonLdType, "foafx:Software");
		result.put(IORdfs.label, "NifConnector");
		result.put(IOFoaf.name, "NifConnector");
		result.put(IOPav.version, "0.1 b001");		
		return result;
	}
	
	/** @return Create the annotator agent content. */
	private JSONObject getAnnotatorAgent( ) {
		JSONObject annotator = new JSONObject( );
		annotator.put(IOJsonLd.jsonLdId, "http://nif-services.neuinfo.org/servicesv1/resource_AnnotateService.html");
		annotator.put(IOJsonLd.jsonLdType, "foafx:Software");
		annotator.put(IORdfs.label, "NIF Annotator Web Service");
		annotator.put(IOFoaf.name, "NIF Annotator Web Service");
		annotator.put(IOPav.version, "1.0");
		return annotator;
	}
	
	/** @return Create the public permissions content. */
	private JSONObject getPublicPermissions( ) {
		JSONObject permissions = new JSONObject( );
		permissions.put("permissions:isLocked", "false");
		permissions.put("permissions:accessType", "urn:domeo:access:public");
		return permissions;
	}
	
};
