
const PrimitiveTypes = Java.type('net.officefloor.polyglot.test.PrimitiveTypes')
const ObjectTypes = Java.type('net.officefloor.polyglot.test.ObjectTypes')
const CollectionTypes = Java.type('net.officefloor.polyglot.test.CollectionTypes')
const VariableTypes = Java.type('net.officefloor.polyglot.test.VariableTypes')
const ParameterTypes = Java.type('net.officefloor.polyglot.test.ParameterTypes')
const WebTypes = Java.type('net.officefloor.polyglot.test.WebTypes')
const JavaObject = Java.type('net.officefloor.polyglot.test.JavaObject')
const MockHttpParameters = Java.type('net.officefloor.polyglot.test.MockHttpParameters')
const MockHttpObject = Java.type('net.officefloor.polyglot.test.MockHttpObject')
const ObjectResponse = Java.type('net.officefloor.web.ObjectResponse')
const IOException = Java.type('java.io.IOException')


function primitives(_boolean, _byte, _short, _char, _int, _long, _float, _double) {
	return new PrimitiveTypes(_boolean, _byte, _short, _char, _int, _long, _float, _double)
}
primitives.officefloor = [
    'boolean', 'byte', 'short', 'char', 'int', 'long', 'float', 'double',
    {next: 'use', argumentType: PrimitiveTypes}
]


function objects(string, object, primitiveArray, objectArray) {
	return new ObjectTypes(string, object, primitiveArray, objectArray)
}
objects.officefloor = [
	'java.lang.String', JavaObject, 'int[]', [JavaObject],
	{next: 'use', argumentType: ObjectTypes}
]


function collections(list, set, map) {
	return new CollectionTypes(list, set, map)
}
collections.officefloor = [
	'java.util.List', 'java.util.Set', 'java.util.Map',
	{next: 'use', argumentType: CollectionTypes}
]


function variables(_val, _in, _out, _var) {
	let value = _var.get()
	_var.set(_var.get() + 1)
	_out.set(new JavaObject('test'))
	return new VariableTypes(_val, _in.get(), value)
}
variables.officefloor = [
	{val: 'char'}, {in: 'java.lang.String'}, {out: JavaObject}, {var: 'java.lang.Integer'},
	{next: 'use', argumentType: VariableTypes}
]


function parameter(param) {
	return new ParameterTypes(param)
}
parameter.officefloor = [
	{param: 'java.lang.String'},
	{next: 'use', argumentType: ParameterTypes}
]


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
serviceFlow.officefloor = [
	{param: 'java.lang.String'},
	{flow: 'flow'},
	{flow: 'flowWithCallback'},
	{flow: 'flowWithParameterAndCallback', argumentType: 'java.lang.String'},
	{flow: 'flowWithParameter', argumentType: 'java.lang.String'},
	{flow: 'exception', argumentType: IOException},
	{next: 'nextFunction'}
]


function web(pathParameter, queryParameter, headerParameter, cookieParameter, httpParameters, httpObject, response) {
	response.send(new WebTypes(pathParameter, queryParameter, headerParameter, cookieParameter, httpParameters, httpObject, new JavaObject(pathParameter)))
}
web.officefloor = [
	{httpPathParameter: 'param'},
	{httpQueryParameter: 'param'},
	{httpHeaderParameter: 'param'},
	{httpCookieParameter: 'param'},
	{httpParameters: MockHttpParameters},
	{httpObject: MockHttpObject},
	ObjectResponse
]


function asynchronousFlow(flowOne, flowTwo) {
	flowOne.complete(() => {
		flowTwo.complete(null)
	})		
}
asynchronousFlow.officefloor = [
	{asynchronousFlow: true},
	{asynchronousFlow: true}
]
