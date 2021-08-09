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

const PrimitiveTypes = Java.type("net.officefloor.polyglot.test.PrimitiveTypes");
const ObjectTypes = Java.type("net.officefloor.polyglot.test.ObjectTypes");
const CollectionTypes = Java.type("net.officefloor.polyglot.test.CollectionTypes");
const VariableTypes = Java.type("net.officefloor.polyglot.test.VariableTypes");
const ParameterTypes = Java.type("net.officefloor.polyglot.test.ParameterTypes");
const WebTypes = Java.type("net.officefloor.polyglot.test.WebTypes");
const JavaObject = Java.type("net.officefloor.polyglot.test.JavaObject");
const MockHttpParameters = Java.type("net.officefloor.polyglot.test.MockHttpParameters");
const MockHttpObject = Java.type("net.officefloor.polyglot.test.MockHttpObject");
const ObjectResponse = Java.type("net.officefloor.web.ObjectResponse");
const IOException = Java.type("java.io.IOException");
const HttpException = Java.type("net.officefloor.server.http.HttpException");


function primitives(_boolean, _byte, _short, _char, _int, _long, _float, _double) {
	return new PrimitiveTypes(_boolean, _byte, _short, _char, _int, _long, _float, _double);
}
primitives.officefloor = [
    "boolean", "byte", "short", "char", "int", "long", "float", "double",
    {nextArgumentType: PrimitiveTypes}
]


function objects(string, object, primitiveArray, objectArray) {
	return new ObjectTypes(string, object, primitiveArray, objectArray);
}
objects.officefloor = [
	"java.lang.String", JavaObject, "int[]", [JavaObject],
	{nextArgumentType: ObjectTypes}
]


function collections(list, set, map) {
	return new CollectionTypes(list, set, map);
}
collections.officefloor = [
	"java.util.List", "java.util.Set", "java.util.Map",
	{nextArgumentType: CollectionTypes}
]


function variables(_val, _in, _out, _var) {
	let value = _var.get();
	_var.set(_var.get() + 1);
	_out.set(new JavaObject("test"));
	return new VariableTypes(_val, _in.get(), value);
}
variables.officefloor = [
	{val: "char"}, {in: "java.lang.String"}, {out: JavaObject}, {var: "java.lang.Integer", qualifier: "qualified"},
	{nextArgumentType: VariableTypes}
]


function parameter(param) {
	return new ParameterTypes(param);
}
parameter.officefloor = [
	{param: "java.lang.String"},
	{nextArgumentType: ParameterTypes}
]


function serviceFlow(flowType, flow, flowWithCallback, flowWithParameter, flowWithParameterAndCallback) {
	switch (flowType) {
	case "nextFunction":
		return; // do nothing so next function fires
	case "flow":
		flow.doFlow(null, null);
		return;
	case "callbacks":
		flowWithCallback.doFlow(null, (error1) => {
			flowWithParameterAndCallback.doFlow("1", (error2) => {
				flowWithParameter.doFlow("2", null);
			});
		});
		return;
	case "exception":
		throw new IOException();
	default:
		Assert.fail("Invalid flow type: " + flowType);
	}
}
serviceFlow.officefloor = [
	{param: "java.lang.String"},
	{flow: "flow"},
	{flow: "flowWithCallback"},
	{flow: "flowWithParameter", argumentType: "java.lang.String"},
	{flow: "flowWithParameterAndCallback", argumentType: "java.lang.String"}
]


function web(pathParameter, queryParameter, headerParameter, cookieParameter, httpParameters, httpObject, response) {
	response.send(new WebTypes(pathParameter, queryParameter, headerParameter, cookieParameter, httpParameters, httpObject, new JavaObject(pathParameter)));
}
web.officefloor = [
	{httpPathParameter: "param"},
	{httpQueryParameter: "param"},
	{httpHeaderParameter: "param"},
	{httpCookieParameter: "param"},
	{httpParameters: MockHttpParameters},
	{httpObject: MockHttpObject},
	ObjectResponse
]


function httpException() {
	throw new HttpException(422, "test");
}
httpException.officefloor = []


function asynchronousFlow(flowOne, flowTwo) {
	flowOne.complete(() => {
		flowTwo.complete(null);
	});
}
asynchronousFlow.officefloor = [
	{asynchronousFlow: true},
	{asynchronousFlow: true}
]
