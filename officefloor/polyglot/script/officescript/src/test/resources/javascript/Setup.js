/*-
 * #%L
 * PolyglotScript
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

// Allows loading functionality to be used by scripts

// Testing by loading Java types
const PrimitiveTypes = Java.type("net.officefloor.polyglot.test.PrimitiveTypes");
const ObjectTypes = Java.type("net.officefloor.polyglot.test.ObjectTypes");
const CollectionTypes = Java.type("net.officefloor.polyglot.test.CollectionTypes");
const VariableTypes = Java.type("net.officefloor.polyglot.test.VariableTypes");
const ParameterTypes = Java.type("net.officefloor.polyglot.test.ParameterTypes");
const WebTypes = Java.type("net.officefloor.polyglot.test.WebTypes");
const JavaObject = Java.type("net.officefloor.polyglot.test.JavaObject");
const IOException = Java.type("java.io.IOException");
const Assert = Java.type("org.junit.Assert");
const MockHttpParameters = Java.type("net.officefloor.polyglot.test.MockHttpParameters");
const MockHttpObject = Java.type("net.officefloor.polyglot.test.MockHttpObject");
const ObjectResponse = Java.type("net.officefloor.web.ObjectResponse");
const HttpException = Java.type("net.officefloor.server.http.HttpException");
