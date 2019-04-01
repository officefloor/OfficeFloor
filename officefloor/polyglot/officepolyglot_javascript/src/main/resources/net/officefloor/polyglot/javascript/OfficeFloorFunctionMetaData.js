/**
 * Loads the OfficeFloor meta-data for the particular function.
 * 
 * @returns Meta-data for the function.
 */
function OFFICEFLOOR_METADATA__FUNCTION_NAME_() {

	// Extract the OfficeFloor meta-data
	let officefloor = _FUNCTION_NAME_.officefloor
	if (!officefloor) {
		return {
			error : 'No officefloor meta-data on function _FUNCTION_NAME_'
		}
	}
	if (!Array.isArray(officefloor)) {
		return {
			error : 'officefloor meta-data must be array for function _FUNCTION_NAME_'
		}
	}

	// OfficeFloor meta-data
	let metaData = {
		parameters : []
	}
	
	// Obtain the type
	let isArray = (type) => Array.isArray(type) && type[0]
	let isClassFunction = (type) => (typeof(type) == 'function') && (type.class) && (type.class.getName)
	let isTypeName = (type) => typeof(type) == 'string'
	let isType = (type) => isArray(type) || isClassFunction(type) || isTypeName(type)
	let extractType = (type) => {
		if (!type) {
			return null
		} else if (isArray(type)) {
			return extractType(type[0]) + '[]'
		} else if (isClassFunction(type)) {
			return type.class.getName() // Java.type
		} else if (isTypeName(type)) {
			return type
		} else {
			metaData.error = `Unable to extract type from ${type} for officefloor configuration of function _FUNCTION_NAME_`
		}
	}

	// Iterate over configuration
	for (let index in officefloor) {
		let config = officefloor[index]

		// Determine type of configuration
		if (config.next) {
			// Handle next
			metaData.nextFunction = {
				name: config.next,
				argumentType: extractType(config.argumentType)
			}
			
		} else if (config.param) {
			// Handle parameter
			metaData.parameters.push({type: extractType(config.param), nature: 'parameter'})
			
		} else if (config.val) {
			// Handle value from variable
			metaData.parameters.push({type: extractType(config.val), nature: 'val'})

		} else if (config.in) {
			// Handle in from variable
			metaData.parameters.push({type: extractType(config.in), nature: 'in'})

		} else if (config.out) {
			// Handle out from variable
			metaData.parameters.push({type: extractType(config.out), nature: 'out'})

		} else if (config.var) {
			// Handle var from variable
			metaData.parameters.push({type: extractType(config.var), nature: 'var'})

		} else if (isType(config)) {
			// Assume to be parameter type
			metaData.parameters.push({type: extractType(config), nature: 'object'})
		} else {
			// Indicate invalid configuration
			metaData.error = `Invalid officefloor configuration (index ${index} = ${config}) for function _FUNCTION_NAME_`
		}
	}

	// Return the meta-data
	return JSON.stringify(metaData)
}