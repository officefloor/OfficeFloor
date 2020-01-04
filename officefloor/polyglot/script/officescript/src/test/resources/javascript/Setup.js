/*-
 * #%L
 * PolyglotScript
 * %%
 * Copyright (C) 2005 - 2020 Daniel Sagenschneider
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
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
