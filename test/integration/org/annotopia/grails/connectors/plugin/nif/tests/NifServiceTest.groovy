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
package org.annotopia.grails.connectors.plugin.nif.tests

import static org.junit.Assert.*
import grails.test.mixin.TestFor

import org.annotopia.grails.connectors.ITextMiningService
import org.annotopia.grails.connectors.plugin.nif.services.NifService
import org.codehaus.groovy.grails.web.json.JSONObject
import org.junit.BeforeClass
import org.junit.Test

/** 
 * JUnit test case for the term search.
 * @author Tom Wilkin 
 */
@TestFor(NifService)
class NifServiceTest {
	
	/** The instance of the Nif Service. */
	def static nifService;
	
	@BeforeClass
	public static void initialise( ) {
		nifService = new NifService( );
	}

	@Test
	public void termSearchTest( ) {
		log.info("TEST:termSearchTest");
		
		HashMap parameters = new HashMap( );
		parameters.put("resource", "nlx_144509-1");

		JSONObject result = nifService.search("plank", parameters);
		log.trace("\n");
		log.trace(result);
		assertNotNull(result);
	}
	
	@Test
	public void termSearchDomeoTest( ) {
		log.info("TEST:termSearchDomeoTest");
		
		HashMap parameters = new HashMap( );
		parameters.put("resource", "nlx_144509-1");
		parameters.put("returnFormat", "domeo");

		JSONObject result = nifService.search("plank", parameters);
		log.trace("\n");
		log.trace(result);
		assertNotNull(result);
	}
	
	@Test
	public void textMiningTest( ) {
		System.out.println("TEST:textMiningTest");
		
		HashMap parameters = new HashMap( );
		
		JSONObject result = nifService.textmine("url", "APP is bad for you.", parameters);
		System.out.println("\n");
		System.out.println(result);
		assertNotNull(result);
	}
	
};
