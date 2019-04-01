package net.officefloor.polyglot.kotlin

import net.officefloor.plugin.section.clazz.NextFunction
import net.officefloor.plugin.section.clazz.Parameter
import net.officefloor.plugin.variable.In
import net.officefloor.plugin.variable.Out
import net.officefloor.plugin.variable.Val
import net.officefloor.plugin.variable.Var
import net.officefloor.polyglot.test.CollectionTypes
import net.officefloor.polyglot.test.JavaObject
import net.officefloor.polyglot.test.ObjectTypes
import net.officefloor.polyglot.test.ParameterTypes
import net.officefloor.polyglot.test.PrimitiveTypes
import net.officefloor.polyglot.test.VariableTypes

@NextFunction("use")
fun primitives(
	_boolean: Boolean,
	_byte: Byte,
	_short: Short,
	_char: Char,
	_int: Int,
	_long: Long,
	_float: Float,
	_double: Double
): PrimitiveTypes {
	return PrimitiveTypes(_boolean, _byte, _short, _char, _int, _long, _float, _double)
}

@NextFunction("use")
fun objects(
	_string: String,
	_object: JavaObject,
	_primitiveArray: IntArray,
	_objectArray: Array<JavaObject>
): ObjectTypes {
	return ObjectTypes(_string, _object, _primitiveArray, _objectArray)
}

@NextFunction("use")
fun collections(_list: List<Int>, _set: Set<Char>, _map: Map<String, JavaObject>): CollectionTypes {
	return CollectionTypes(_list, _set, _map)
}

@NextFunction("use")
fun variables(@Val _val: Char, _in: In<String>, _out: Out<JavaObject>, _var: Var<Int>): VariableTypes {
	_out.set(JavaObject("test"))
	val captureVar = _var.get()
	_var.set(_var.get() + 1)
	return VariableTypes(_val, _in.get(), captureVar)
}

@NextFunction("use")
fun parameters(@Parameter param: String): ParameterTypes {
	return ParameterTypes(param)
}