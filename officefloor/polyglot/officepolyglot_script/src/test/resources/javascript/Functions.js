
// Java types loaded in setup

function primitives(_boolean, _byte, _short, _char, _int, _long, _float, _double) {
	return new PrimitiveTypes(_boolean, _byte, _short, _char, _int, _long, _float, _double)
}
primitives.officefloor = {
	parameters: [
		{type: 'boolean'},
		{type: 'byte'},
		{type: 'short'},
		{type: 'char'},
		{type: 'int'},
		{type: 'long'},
		{type: 'float'},
		{type: 'double'}
	],
	nextFunction: {name: 'use', argumentType: PrimitiveTypes.class.getName()}
}


function objects(string, object, primitiveArray, objectArray) {
	return new ObjectTypes(string, object, primitiveArray, objectArray)
}
objects.officefloor = {
	parameters: [
		{type: 'java.lang.String'},
		{type: JavaObject.class.getName()},
		{type: 'int[]'},
		{type: JavaObject.class.getName() + '[]'}
	],
	nextFunction: {name: 'use', argumentType: ObjectTypes.class.getName()}
}


function collections(list, set, map) {
	return new CollectionTypes(list, set, map)
}
collections.officefloor = {
	parameters: [
		{type: 'java.util.List'},
		{type: 'java.util.Set'},
		{type: 'java.util.Map'}
	],
	nextFunction: {name: 'use', argumentType: CollectionTypes.class.getName()}
}


function variables(_val, _in, _out, _var) {
	let value = _var.get()
	_var.set(_var.get() + 1)
	_out.set(new JavaObject('test'))
	return new VariableTypes(_val, _in.get(), value)
}
variables.officefloor = {
	parameters: [
		{type: 'char', nature: 'val'},
		{type: 'java.lang.String', nature: 'in'},
		{type: JavaObject.class.getName(), nature: 'out'},
		{type: 'java.lang.Integer', nature: 'var'}
	],
	nextFunction: {name: 'use', argumentType: VariableTypes.class.getName()}
}


function parameter(param) {
	return new ParameterTypes(param)
}
parameter.officefloor = {
	parameters: [
		{type: 'java.lang.String', nature: 'parameter'}
	],
	nextFunction: {name: 'use', argumentType: ParameterTypes.class.getName()}
}


function serviceFlow(flowType, flow, flowWithCallback, flowWithParameterAndCallback, flowWithParameter, exception) {
	switch (flowType) {
	case 'nextFunction':
		return // do nothing so next function fires
	case 'flow':
		flow.doFlow(null, null)
		return
	case 'callbacks':
		flowWithCallback.doFlow(null, (error1) => {
			flowWithParameterAndCallback.doFlow("1", (error2) => {
				flowWithParameter.doFlow("2", null)
			})
		})
		return
	case 'exception':
		exception.doFlow(new IOException(), null)
		return
	default:
		Assert.fail("Invalid flow type: " + flowType);
	}
}
serviceFlow.officefloor = {
	parameters: [
		{type: 'java.lang.String', nature: 'parameter'},
		{name: 'flow', nature: 'flow'},		
		{name: 'flowWithCallback', nature: 'flow'},
		{name: 'flowWithParameterAndCallback', type: 'java.lang.String', nature: 'flow'},
		{name: 'flowWithParameter', type: 'java.lang.String', nature: 'flow'},
		{name: 'exception', nature: 'flow'}
	],
	nextFunction: {name: 'nextFunction'}
}


function web(pathParameter, queryParameter, headerParameter, cookieParameter, httpParameters, httpObject, response) {
	response.send(new WebTypes(pathParameter, queryParameter, headerParameter, cookieParameter, httpParameters, httpObject, new JavaObject(pathParameter)))
}
web.officefloor = {
	parameters: [
		{name: 'param', nature: 'httpPathParameter'},
		{name: 'param', nature: 'httpQueryParameter'},
		{name: 'param', nature: 'httpHeaderParameter'},
		{name: 'param', nature: 'httpCookieParameter'},
		{type: MockHttpParameters.class.getName(), nature: 'httpParameters'},
		{type: MockHttpObject.class.getName(), nature: 'httpObject'},
		{type: ObjectResponse.class.getName()}
	]
}


function asynchronousFlow(flowOne, flowTwo) {
	flowOne.complete(() => {
		flowTwo.complete(null)
	})		
}
asynchronousFlow.officefloor = {
	parameters: [
		{nature: 'asynchronousFlow'},
		{nature: 'asynchronousFlow'}
	]
}