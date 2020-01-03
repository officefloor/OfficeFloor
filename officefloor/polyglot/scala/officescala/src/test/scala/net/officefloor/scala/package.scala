/*-
 * #%L
 * Scala
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

package net.officefloor

import java.io.IOException

import net.officefloor.frame.api.function.AsynchronousFlow
import net.officefloor.plugin.clazz.Qualified
import net.officefloor.plugin.section.clazz.Parameter
import net.officefloor.plugin.variable.{In, Out, Val, Var}
import net.officefloor.polyglot.test._
import net.officefloor.server.http.HttpException
import net.officefloor.web._
import org.junit.Assert

package object scala {

  def primitives(_boolean: Boolean, _byte: Byte, _short: Short, _char: Char, _int: Int, _long: Long, _float: Float, _double: Double): PrimitiveTypes = {
    new PrimitiveTypes(_boolean, _byte, _short, _char, _int, _long, _float, _double)
  }

  def objects(string: String, javaObject: JavaObject, primitiveArray: Array[Int], objectArray: Array[JavaObject]): ObjectTypes = {
    new ObjectTypes(string, javaObject, primitiveArray, objectArray)
  }

  def collections(_list: java.util.List[java.lang.Integer], _set: java.util.Set[java.lang.Character], _map: java.util.Map[String, JavaObject]): CollectionTypes = {
    new CollectionTypes(_list, _set, _map)
  }

  def variables(@Val _val: Character, _in: In[java.lang.String], _out: Out[JavaObject], @Qualified("qualified") _var: Var[java.lang.Integer]): VariableTypes = {
    val result = new VariableTypes(_val, _in.get(), _var.get())
    _out.set(new JavaObject("test"))
    _var.set(_var.get() + 1)
    result
  }

  def parameter(@Parameter parameter: String): ParameterTypes = {
    new ParameterTypes(parameter)
  }

  def web(@HttpPathParameter("param") pathParameter: String,
          @HttpQueryParameter("param") queryParameter: String,
          @HttpHeaderParameter("param") headerParameter: String,
          @HttpCookieParameter("param") cookieParameter: String, httpParameters: MockHttpParameters,
          httpObject: MockHttpObject, response: ObjectResponse[WebTypes]) {
    response.send(new WebTypes(pathParameter, queryParameter, headerParameter, cookieParameter, httpParameters,
      httpObject, new JavaObject(pathParameter)));
  }

  def httpException() {
    throw new HttpException(422, "test")
  }

  def serviceFlow(@Parameter flowType: String, flows: Flows) {
    flowType match {
      case "nextFunction" => Unit
      case "flow" => flows.flow()
      case "callbacks" =>
        flows.flowWithCallback(_ => {
          flows.flowWithParameterAndCallback("1", _ => {
            flows.flowWithParameter("2")
          })
        })
      case "exception" => throw new IOException()
      case _ => Assert.fail("Invalid flow type: " + flowType)
    }
  }

  def asynchronousFlow(flowOne: AsynchronousFlow, flowTwo: AsynchronousFlow) {
    flowOne.complete(() => flowTwo.complete(null))
  }

}
