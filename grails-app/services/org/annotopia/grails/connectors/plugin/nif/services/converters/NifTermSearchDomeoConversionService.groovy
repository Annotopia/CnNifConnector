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

import org.annotopia.grails.connectors.plugin.nif.utils.converters.domeo.NIfAntibodiesDomeoConverter
import org.annotopia.grails.connectors.plugin.nif.utils.converters.domeo.NIfIntegratedAnimalsDomeoConverter
import org.annotopia.grails.connectors.plugin.nif.utils.converters.domeo.NIfRegistryDomeoConverter
import org.codehaus.groovy.grails.web.json.JSONObject

/** Convert the terms returned by the Nif Service into the Domeo format.
 * @author Tom Wilkin */
class NifTermSearchDomeoConversionService {
	
	/** The return format that this conversion service generates. */
	public static final String RETURN_FORMAT = "domeo";

	/** Convert the returned JSON into the correct format for consumption by Domeo.
	 * @param response The JSON response from the web service.
	 * @return The JSON in the Domeo format. */
	public JSONObject convert(def resource, def response) {
		
		if(NIfRegistryDomeoConverter.canConvert(resource)) {
			NIfRegistryDomeoConverter converter = new NIfRegistryDomeoConverter();
			return converter.convert(response);
		} else if(NIfIntegratedAnimalsDomeoConverter.canConvert(resource)) {
			NIfIntegratedAnimalsDomeoConverter converter = new NIfIntegratedAnimalsDomeoConverter();
			return converter.convert(response);
		} else if(NIfAntibodiesDomeoConverter.canConvert(resource)) {
			NIfAntibodiesDomeoConverter converter = new NIfAntibodiesDomeoConverter();
			return converter.convert(response);
		}
	}
};
