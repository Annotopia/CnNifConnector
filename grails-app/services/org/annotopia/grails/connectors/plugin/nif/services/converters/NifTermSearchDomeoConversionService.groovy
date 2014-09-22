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

import org.codehaus.groovy.grails.web.json.JSONArray
import org.codehaus.groovy.grails.web.json.JSONObject

/** Convert the terms returned by the Nif Service into the Domeo format.
 * @author Tom Wilkin */
class NifTermSearchDomeoConversionService {

	/** Convert the returned JSON into the correct format for consumption by Domeo.
	 * @param response The JSON response from the web service.
	 * @return The JSON in the Domeo format. */
	public JSONObject convert(def response) {
		JSONObject result = new JSONObject( );
		result.put("pagesize", 1);
		result.put("pagenumber", 1);
		result.put("totalpages", 1);
		
		// iterate through the terms
		JSONArray terms = new JSONArray( );
		response.result.result.each {
			JSONObject term = new JSONObject( );
			
			term.put("termUri", it["url"]);
			term.put("description", it["description"]);
			term.put("termLabel", it["resource_name"]);			
			term.put("sourceUri", "http://www.neuinfo.org");
			term.put("sourceLabel", "NIF");
			terms.add(term);
		}
		result.put("terms", terms);
		
		return result;
	}
	
};
