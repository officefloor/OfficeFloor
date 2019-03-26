package net.officefloor.polyglot

import net.officefloor.plugin.variable.{ Val, In, Out, Var }
import net.officefloor.plugin.section.clazz.{ Parameter, NextFunction }

package object scala {

  def primitiveTypes(_byte: Byte, _short: Short, _char: Char, _int: Int, _long: Long, _float: Float, _double: Double): PrimitiveTypes = {
    new PrimitiveTypes(_byte, _short, _char, _int, _long, _float, _double)
  }

  def objects(_java: JavaObject): JavaObject = _java

  def collections(_list: java.util.List[java.lang.Integer], _set: java.util.Set[java.lang.Character], _map: java.util.Map[String, JavaObject], _collection: java.util.Collection[java.lang.Float]): CollectionTypes = {
    new CollectionTypes(_list, _set, _map, _collection)
  }

  def variables(@Val _val: Character, _in: In[java.lang.String], _out: Out[JavaObject], _var: Var[java.lang.Integer]): VariableTypes = {
    val result = new VariableTypes(_val, _in.get(), _var.get())
    _out.set(new JavaObject("test"))
    _var.set(_var.get() + 1)
    result
  }

  def parameters(@Parameter parameter: String): String = parameter

  @NextFunction("next")
  def nextFunction() = Unit

}