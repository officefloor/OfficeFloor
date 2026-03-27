/*-
 * #%L
 * JavaScript
 * %%
 * Copyright (C) 2005 - 2020 Daniel Sagenschneider
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

/**
 * Loads the OfficeFloor meta-data for the particular function.
 * 
 * @returns Meta-data for the function.
 */
function OFFICEFLOOR_METADATA__FUNCTION_NAME_() {

	// Extract the OfficeFloor meta-data
	let officefloor = _FUNCTION_NAME_.officefloor;
	if (!officefloor) {
		return {
			error : "No officefloor meta-data on function _FUNCTION_NAME_"
		}
	}
	if (!Array.isArray(officefloor)) {
		return {
			error : "officefloor meta-data must be array for function _FUNCTION_NAME_"
		}
	}

	// OfficeFloor meta-data
	let metaData = {
		parameters : []
	}
	
	// Obtain the type
	let isArray = (type) => Array.isArray(type) && type[0];
	let isClassFunction = (type) => (type.class) && (type.class.getName);
	let isTypeName = (type) => typeof(type) == "string";
	let isType = (type) => isArray(type) || isClassFunction(type) || isTypeName(type);
	let extractType = (type) => {
		if (!type) {
			return null;
		} else if (isArray(type)) {
			return extractType(type[0]) + "[]";
		} else if (isClassFunction(type)) {
			return type.class.getName(); // Java.type
		} else if (isTypeName(type)) {
			return type;
		} else {
			metaData.error = `Unable to extract type from ${type} for officefloor configuration of function _FUNCTION_NAME_`;
		}
	}

	// Iterate over configuration
	for (let index in officefloor) {
		let config = officefloor[index];

		// Determine type of configuration
		if (config.nextArgumentType) {
			// Handle next
			metaData.nextArgumentType = extractType(config.nextArgumentType)
			
		} else if (config.param) {
			// Handle parameter
			metaData.parameters.push({type: extractType(config.param), nature: "parameter"});
			
		} else if (config.val) {
			// Handle value from variable
			metaData.parameters.push({type: extractType(config.val), qualifier: config.qualifier, nature: "val"});

		} else if (config.in) {
			// Handle in from variable
			metaData.parameters.push({type: extractType(config.in), qualifier: config.qualifier, nature: "in"});

		} else if (config.out) {
			// Handle out from variable
			metaData.parameters.push({type: extractType(config.out), qualifier: config.qualifier, nature: "out"});

		} else if (config.var) {
			// Handle var from variable
			metaData.parameters.push({type: extractType(config.var), qualifier: config.qualifier, nature: "var"});
			
		} else if (config.httpPathParameter) {
			// Handle HTTP path parameter
			metaData.parameters.push({name: String(config.httpPathParameter), nature: "httpPathParameter"});

		} else if (config.httpQueryParameter) {
			// Handle HTTP query parameter
			metaData.parameters.push({name: String(config.httpQueryParameter), nature: "httpQueryParameter"});
			
		} else if (config.httpHeaderParameter) {
			// Handle HTTP header parameter
			metaData.parameters.push({name: String(config.httpHeaderParameter), nature: "httpHeaderParameter"});			

		} else if (config.httpCookieParameter) {
			// Handle HTTP cookie parameter
			metaData.parameters.push({name: String(config.httpCookieParameter), nature: "httpCookieParameter"});
			
		} else if (config.httpParameters) {
			// Handle HTTP parameters
			metaData.parameters.push({type: extractType(config.httpParameters), nature: "httpParameters"});

		} else if (config.httpObject) {
			// Handle HTTP object
			metaData.parameters.push({type: extractType(config.httpObject), nature: "httpObject"});
			
		} else if (config.flow) {
			// Flow
			metaData.parameters.push({name: String(config.flow), type: extractType(config.argumentType), nature: "flow"});

		} else if (config.asynchronousFlow) {
			// Asynchronous flow
			metaData.parameters.push({nature: "asynchronousFlow"});

		} else if (isType(config)) {
			// Assume to be parameter type
			metaData.parameters.push({type: extractType(config), nature: "object"});
						
		} else {
			// Indicate invalid configuration
			const safeConfig = {};
			for (const propertyName in config) {
				const propertyValue = config[propertyName];
				safeConfig[propertyName] = isType(propertyValue) ? extractType(propertyValue) : String(propertyValue);
			}
			const safeConfigText = JSON.stringify(safeConfig);
			metaData.error = `Invalid officefloor configuration (index ${index} = ${safeConfigText}) for function _FUNCTION_NAME_`;
		}
		
		// If error, then return
		if (metaData.error) {
			return JSON.stringify(metaData);
		}
	}

	// Return the meta-data
	return JSON.stringify(metaData);
}
