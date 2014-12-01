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

import org.annotopia.grails.connectors.converters.BaseTextMiningConversionService
import org.annotopia.grails.connectors.utils.UUID;
import org.annotopia.grails.connectors.vocabularies.IOAccessRestrictions;
import org.annotopia.grails.connectors.vocabularies.IODomeo;
import org.annotopia.grails.connectors.vocabularies.IOFoaf;
import org.annotopia.grails.connectors.vocabularies.IODublinCoreTerms;
import org.annotopia.grails.connectors.vocabularies.IOJsonLd;
import org.annotopia.grails.connectors.vocabularies.IOPav;
import org.annotopia.grails.connectors.vocabularies.IORdfs;
import org.codehaus.groovy.grails.web.json.JSONArray;
import org.codehaus.groovy.grails.web.json.JSONObject;

/** Convert the text mining results returned by the Nif service into the Domeo format.
 * @author Tom Wilkin */
@Deprecated
class NifTextMiningDomeoConversionService extends BaseTextMiningConversionService {

	/** The return format that this conversion service generates. */
	public static final String RETURN_FORMAT = "domeo";
	
	/** Convert the returned JSON into the correct format for Domeo.
	 * @param json The JSON response from the web service.
	 * @param resourceURI The URI for the resource that was accessed.
	 * @param contentText The content that was queried.
	 * @return The JSON in the Domeo format. */
	public JSONObject convert(def json, final String resourceURI, final String contentText) {
		String snippetUrn = URN_SNIPPET_PREFIX + UUID.uuid( );
		
		JSONObject result = new JSONObject( );
		result.put(IOJsonLd.jsonLdId, URN_ANNOTATION_SET_PREFIX + UUID.uuid( ));
		result.put(IOJsonLd.jsonLdType, "ao:AnnotationSet");
		result.put(IORdfs.label, "NIF Annotator Results");
		result.put(IODublinCoreTerms.description, "NIF Annotator Results");
		
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
		// Domeo
		JSONObject domeo = getDomeo( );
		agents.add(agents.size( ), domeo);
		result.put(IODomeo.agents, agents);
		
		// permissions
		result.put(IOAccessRestrictions.permissions, getPublicPermissions( ));
		
		// annotations
		JSONArray annotations = new JSONArray( );
		json.each {
			JSONObject selector = findOrCreateAndSaveSelectorUsingStringSearch(
				contentText, it["token"]["value"], it["start"], it["end"]);
			
			if(selector != null) {			
				JSONObject annotation = new JSONObject( );
				annotation.put(IOJsonLd.jsonLdId, URN_ANNOTATION_PREFIX + UUID.uuid( ));
				annotation.put(IOJsonLd.jsonLdType, "ao:Qualifier");
				annotation.put(IORdfs.label, "Qualifier");
				annotation.put("pav:createdWith", "urn:domeo:software:id:Domeo-2.0alpha-040");
				annotation.put("pav:importedBy", "urn:domeo:software:id:NifConnector-0.1-001");
				annotation.put("pav:createdBy", "http://nif-services.neuinfo.org/servicesv1/resource_AnnotateService.html");
				annotation.put("pav:importedFrom","http://nif-services.neuinfo.org/servicesv1/resource_AnnotateService.html");
				annotation.put("pav:lastSavedOn", dateFormat.format(new Date( )));
				annotation.put("pav:versionNumber", "");
				
				// body
				JSONObject body = new JSONObject( );
				body.put(IORdfs.label, it["token"]["value"]);
				body.put(IOJsonLd.jsonLdId, it["token"]["id"]);
				body.put("domeo:category", "NIF concept");
				body.put("http://www.w3.org/2004/02/skos/core#prefLabel", it["token"]["value"])
				// source
				JSONObject source = new JSONObject( );
				source.put(IORdfs.label, "Neuroscience Information Framework (NIF) Standard Ontology");
				source.put(IOJsonLd.jsonLdId, "http://data.bioontology.org/ontologies/NIFSTD");
				body.put("dct:source", source);
				
				// bodies
				JSONArray bodies = new JSONArray( );
				bodies.add(body);
				annotation.put("ao:hasTopic", bodies);
				
				annotation.put("pav:previousVersion", "");
				annotation.put("pav:createdOn", dateFormat.format(new Date( )));
				
				JSONObject target = new JSONObject( );
				target.put(IOJsonLd.jsonLdId, URN_SPECIFIC_RESOURCE_PREFIX + UUID.uuid( ));
				target.put(IOJsonLd.jsonLdType, "ao:SpecificResource");
				target.put("ao:hasSource", snippetUrn);
				target.put("ao:hasSelector", selector);
				JSONArray context = new JSONArray( );
				context.add(target);
				annotation.put("ao:context", context);
				
				annotations.add(annotations.size( ), annotation);
			}
		}
		result.put("ao:item", annotations);
		
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
		Map<String,Object> matchInfo = searchForMatch(text, match, start);
		
		if(matchInfo != null) {
			JSONObject selector = new JSONObject();
			selector.put(IOJsonLd.jsonLdId, URN_SELECTOR_PREFIX + UUID.uuid());
			selector.put(IOJsonLd.jsonLdType, "ao:PrefixSuffixTextSelector");
			selector.put(IOPav.createdOn, dateFormat.format(new Date( )));
			selector.put("ao:prefix", matchInfo.prefix);
			selector.put("ao:exact", matchInfo.exact);
			selector.put("ao:suffix", matchInfo.suffix);
			return selector;
		}
		
		return null;
	}
	
	/** @return Create the connector agent content. */
	private JSONObject getConnectorAgent( ) {
		JSONObject result = getSoftwareAgent(
			"http://nif-services.neuinfo.org/servicesv1/resource_AnnotateService.html",
			"NIF Annotator Web Service",
			"NIF Annotator Web Service",
			"1.0"
		);
		result.remove(IOPav.version);
		result.put("foafx:build", "001");
		result.put("foafx:version", "0.1");
		return result;
	}
	
	/** @return Create the annotator agent content. */
	private JSONObject getAnnotatorAgent( ) {
		JSONObject annotator = getSoftwareAgent(
				"http://nif-services.neuinfo.org/servicesv1/resource_AnnotateService.html",
				"NIF Annotator Web Service",
				"NIF Annotator Web Service",
				"1.0"
		);
		annotator.remove(IOPav.version);
		annotator.put("foafx:build", "001");
		annotator.put("foafx:version", "1.0");
		return annotator;
	}
	
	/** @return The Domeo specific agent content. */
	private JSONObject getDomeo( ) {
		JSONObject domeo = getSoftwareAgent("urn:domeo:software:id:Domeo-2.0alpha-040",
				"Domeo Annotation Toolkit", "Domeo", "1.0");
		domeo.remove(IOPav.version);
		domeo.put("foafx:build", "040");
		domeo.put("foafx:version", "1.0");
		return domeo;
	}
	
	/**
	 * It creates a JSON object for the connector agent (software).
	 * @param uri	The Agent URI
	 * @param label	The Agent label
	 * @param name	The Agent name, can be the same as the label
	 * @param ver	The Agent version
	 * @return Create the connector agent content.
	 */
	protected JSONObject getSoftwareAgent(String uri, String label, String name, String ver) {
		JSONObject result = new JSONObject( );
		result.put(IOJsonLd.jsonLdId, uri);
		result.put(IOJsonLd.jsonLdType, "foafx:Software");
		result.put(IORdfs.label, label);
		result.put("foafx:name", name);
		result.put(IOPav.version, ver);
		return result;
	}
};
