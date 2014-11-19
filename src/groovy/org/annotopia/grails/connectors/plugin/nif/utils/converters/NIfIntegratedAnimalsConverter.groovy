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
class NIfIntegratedAnimalsConverter implements IConvert {

	static String RESOURCE_ID = "nlx_154697-1"
	
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
		response.result.result.each {
			JSONObject item = new JSONObject( );
			
			item.put("label", it["Name"]);
			item.put("description", it["description"]);
			item.put("@id", it["url_p"]);
			
			JSONObject source = new JSONObject( );
			source.put("@id", "http://www.neuinfo.org");
			source.put("label", "NIF");
			item.put("isDefinedBy", source);
			items.add(item);
		}
		result.put("items", items);
		
		return result;
	}
}
