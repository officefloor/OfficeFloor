
var PrimitiveTypes = Java.type('net.officefloor.polyglot.test.PrimitiveTypes')
var ObjectTypes = Java.type('net.officefloor.polyglot.test.ObjectTypes')
var CollectionTypes = Java.type('net.officefloor.polyglot.test.CollectionTypes')
var VariableTypes = Java.type('net.officefloor.polyglot.test.VariableTypes')
var ParameterTypes = Java.type('net.officefloor.polyglot.test.ParameterTypes')
var JavaObject = Java.type('net.officefloor.polyglot.test.JavaObject')


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
