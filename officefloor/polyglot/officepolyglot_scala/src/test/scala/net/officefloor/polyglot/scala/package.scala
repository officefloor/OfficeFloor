package net.officefloor.polyglot

import net.officefloor.polyglot.test.{ PrimitiveTypes, ObjectTypes, JavaObject, CollectionTypes, VariableTypes, ParameterTypes }
import net.officefloor.plugin.variable.{ Val, In, Out, Var }
import net.officefloor.plugin.section.clazz.{ Parameter, NextFunction }

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

}