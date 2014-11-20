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
package org.annotopia.grails.connectors.plugin.nif.utils

import org.annotopia.grails.connectors.MiscUtils

/**
 * @author Paolo Ciccarese <paolo.ciccarese@gmail.com>
 */
class NifAntibodiesUrlComposer {

	public String getUrl(String SERVICE_URL, String query, HashMap parameters) {
		composeUrl(SERVICE_URL, parameters.get("resource"), query, parameters.get("type"), parameters.get("vendor"))
	}
	
	/**
	 * Definition of the search URL.
	 * @param resource  The antibody resource
	 * @param query     The search query
	 * @param vendor    The antibody vendor
	 * @param type      The type (Name, Catalog Number or Clone Number)
	 * @return The URL for search
	 */
	private String composeUrl(String SERVICE_URL, String resource, String  query, String  type, String vendor) {
		if(type=="catalog") {
			return SERVICE_URL + resource + '?q=*' +
				((query!=null&&query.trim().length()>0)?'&filter=Cat%20Num:' + URLEncoder.encode(query, MiscUtils.DEFAULT_ENCODING):'') +
				((vendor!=null&&vendor.trim().length()>0)?'&filter=Vendor:' + URLEncoder.encode(vendor, MiscUtils.DEFAULT_ENCODING):'') +
				"&exportType=all";
		} else if(type=="clone") {
			return SERVICE_URL + resource + '?q=*' +
				((query!=null&&query.trim().length()>0)?'&filter=Clone%20ID:' + URLEncoder.encode(query, MiscUtils.DEFAULT_ENCODING):'') +
				((vendor!=null&&vendor.trim().length()>0)?'&filter=Vendor:' + URLEncoder.encode(vendor, MiscUtils.DEFAULT_ENCODING):'') +
				"&exportType=all";
		}
		
		// TODO manage filters
		return SERVICE_URL + resource + '?q=' +
			((query!=null&&query.trim().length()>0)?URLEncoder.encode(query,MiscUtils.DEFAULT_ENCODING):'*') + (
				(vendor!=null&&vendor.trim().length()>0)?'&filter=Vendor:' + URLEncoder.encode(vendor, MiscUtils.DEFAULT_ENCODING):'') +
				"&exportType=all";
	}
}
