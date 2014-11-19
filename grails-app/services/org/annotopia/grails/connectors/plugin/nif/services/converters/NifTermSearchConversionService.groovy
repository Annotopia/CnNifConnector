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

import org.annotopia.grails.connectors.plugin.nif.utils.converters.NIfIntegratedAnimalsConverter
import org.annotopia.grails.connectors.plugin.nif.utils.converters.NIfRegistryConverter
import org.codehaus.groovy.grails.web.json.JSONObject

/** Convert the terms returned by the Nif Service into the default format.
 * @author Tom Wilkin */
class NifTermSearchConversionService {

	/** The return format that this conversion service generates. */
	public static final String RETURN_FORMAT = "annotopia";
	
	/** Convert the returned JSON into the correct format returned by Annotopia.
	 * @param response The JSON response from the web service.
	 * @param duration The number of milliseconds that the query took.
	 * @return The JSON in the Annotopia format. */
	public JSONObject convert(def resource, def response, final long duration) {	
		
		JSONObject result = new JSONObject();
		result.put("duration", duration + "ms");
		result.put("total", 1);
		result.put("max", 1);
		result.put("offset", 0);
		result.put("currentPage", 1);
		result.put("totalPages", 1);
		result.put("nextPage", "none");
		result.put("prevPage", "none");
		
		if(NIfRegistryConverter.canConvert(resource)) {
			NIfRegistryConverter converter = new NIfRegistryConverter();
			result.put("items",converter.convert(response).get("items"));
		} else if(NIfIntegratedAnimalsConverter.canConvert(resource)) {
			NIfIntegratedAnimalsConverter converter = new NIfIntegratedAnimalsConverter();
			result.put("items",converter.convert(response).get("items"));
		}
					
		JSONObject converted = new JSONObject( );
		converted.put("status", "results");
		converted.put("result", result);
		
		return converted;
	}
	
};
