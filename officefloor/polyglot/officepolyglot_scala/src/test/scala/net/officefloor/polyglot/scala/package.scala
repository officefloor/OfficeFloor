package net.officefloor.polyglot

import net.officefloor.polyglot.test.{ PrimitiveTypes, ObjectTypes, JavaObject, CollectionTypes, VariableTypes, ParameterTypes }
import net.officefloor.plugin.variable.{ Val, In, Out, Var }
import net.officefloor.plugin.section.clazz.{ Parameter, NextFunction }
import net.officefloor.web.{ HttpPathParameter, HttpQueryParameter, HttpHeaderParameter, HttpCookieParameter }
import net.officefloor.polyglot.test.{ MockHttpParameters, MockHttpObject }
import net.officefloor.web.ObjectResponse
import net.officefloor.polyglot.test.WebTypes
import net.officefloor.frame.api.function.AsynchronousFlow
import net.officefloor.plugin.managedfunction.clazz.FlowInterface
import java.io.IOException
import org.junit.Assert

package object scala {

  @NextFunction("use")
  def primitives(_boolean: Boolean, _byte: Byte, _short: Short, _char: Char, _int: Int, _long: Long, _float: Float, _double: Double): PrimitiveTypes = {
    new PrimitiveTypes(_boolean, _byte, _short, _char, _int, _long, _float, _double)
  }

  @NextFunction("use")
  def objects(string: String, javaObject: JavaObject, primitiveArray: Array[Int], objectArray: Array[JavaObject]): ObjectTypes = {
    new ObjectTypes(string, javaObject, primitiveArray, objectArray)
  }

  @NextFunction("use")
  def collections(_list: java.util.List[java.lang.Integer], _set: java.util.Set[java.lang.Character], _map: java.util.Map[String, JavaObject]): CollectionTypes = {
    new CollectionTypes(_list, _set, _map)
  }

  @NextFunction("use")
  def variables(@Val _val: Character, _in: In[java.lang.String], _out: Out[JavaObject], _var: Var[java.lang.Integer]): VariableTypes = {
    val result = new VariableTypes(_val, _in.get(), _var.get())
    _out.set(new JavaObject("test"))
    _var.set(_var.get() + 1)
    result
  }

  @NextFunction("use")
  def parameter(@Parameter parameter: String): ParameterTypes = {
    new ParameterTypes(parameter)
  }

  def web(
    @HttpPathParameter("param") pathParameter: String,
    @HttpQueryParameter("param") queryParameter: String,
    @HttpHeaderParameter("param") headerParameter: String,
    @HttpCookieParameter("param") cookieParameter: String, httpParameters: MockHttpParameters,
    httpObject: MockHttpObject, response: ObjectResponse[WebTypes]) {
    response.send(new WebTypes(pathParameter, queryParameter, headerParameter, cookieParameter, httpParameters,
      httpObject, new JavaObject(pathParameter)));
  }

  @NextFunction("nextFunction")
  def serviceFlow(@Parameter flowType: String, flows: Flows) {
    flowType match {
      case "nextFunction" => return
      case "flow" => flows.flow()
      case "callbacks" =>
        flows.flowWithCallback((error1) => {
          flows.flowWithParameterAndCallback("1", (error2) => {
            flows.flowWithParameter("2")
          })
        })
      case "exception" => flows.exception(new IOException())
      case _ => Assert.fail("Invalid flow type: " + flowType)
    }
  }

  def asynchronousFlow(flowOne: AsynchronousFlow, flowTwo: AsynchronousFlow) {
    flowOne.complete(() => flowTwo.complete(null))
  }

}