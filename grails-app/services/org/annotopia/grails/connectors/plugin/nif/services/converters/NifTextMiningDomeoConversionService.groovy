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

import org.codehaus.groovy.grails.web.json.JSONObject

/** Convert the text mining results returned by the Nif service into the Domeo format.
 * @author Tom Wilkin */
class NifTextMiningDomeoConversionService {

	/** The return format that this conversion service generates. */
	public static final String RETURN_FORMAT = "domeo";
	
	/** Convert the returned JSON into the correct format for Domeo.
	 * @param json The JSON response from the web service.
	 * @param resourceURI The URI for the resource that was accessed.
	 * @param contentText The content that was queried.
	 * @return The JSON in the Domeo format. */
	public JSONObject convert(def json, final String resourceURI, final String contentText) {
		JSONObject result = new JSONObject( );
		
		// TODO output Domeo format
		
		return result;
	}
	
};
