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
package org.annotopia.grails.connectors.plugin.nif.utils.converters

import org.annotopia.grails.connectors.plugin.nif.utils.converters.IConvert;
import org.codehaus.groovy.grails.web.json.JSONArray
import org.codehaus.groovy.grails.web.json.JSONObject


/**
 * @author Paolo Ciccarese <paolo.ciccarese@gmail.com>
 */
class NIfAntibodiesConverter implements IConvert {

	static String RESOURCE_ID = "nlx_154697-1"
	
	static final String ANTIBODY_TERM_URL ="http://ontology.neuinfo.org/NIF/DigitalEntities/NIF-Investigation.owl#birnlex_2110"
	static final String ANTIBODY_REGISTRY_URL = "http://www.antibodyregistry.org"
	static final String ANTIBODY_REGISTRY_LABEL = "Antibodyregistry.org"
	
	public static boolean canConvert(String resource) {
		return resource==RESOURCE_ID;
	}
	
	/** Convert the returned JSON into the general format..
	 * @param response The JSON response from the web service.
	 * @return The JSON in the general format. */
	public JSONObject convert(def response) {

		JSONObject result = new JSONObject( );
		
		// iterate through the items
		JSONArray items = new JSONArray( );
		response.result.result.each { item ->
			JSONObject term = new JSONObject( );
			
			term.put("@id", "http://antibodyregistry.org/" + item.id);
			term.put("@type", ANTIBODY_TERM_URL);
			//term.put("termId", item.ab_id);
			term.put("termUri", item.antibodyUrl);
			term.put("termLabel", item.ab_name);
			term.put("description", item["Antibody Name"]);
			term.put("target", item.ab_target);
			//term.put("targetUri", item.ab_target_entrez_gid);
			term.put("vendor", item.vendor);
			term.put("catalogNumber", item.catalog_num);
			term.put("clonality", item.clonality);
			term.put("cloneId", item.cloneId);
			term.put("hostOrganism", item["Host Organism"]);
			term.put("sourceUri", ANTIBODY_REGISTRY_URL);
			term.put("sourceLabel", ANTIBODY_REGISTRY_LABEL);
			
			JSONObject source = new JSONObject( );
			source.put("@id", "http://www.neuinfo.org");
			source.put("label", "NIF");
			term.put("isDefinedBy", source);
			items.add(term);
		}
		result.put("items", items);
		
		return result;
	}
}
