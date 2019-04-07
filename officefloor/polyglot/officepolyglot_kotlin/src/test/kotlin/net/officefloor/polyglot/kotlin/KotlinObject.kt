package net.officefloor.polyglot.kotlin

import net.officefloor.plugin.managedobject.clazz.Dependency
import net.officefloor.polyglot.test.JavaObject
import net.officefloor.polyglot.test.ObjectInterface

/**
 * Kotlin object.
 */
class KotlinObject : ObjectInterface {

	@Dependency
	private var dependency: JavaObject? = null

	override fun getValue() = "test"

	override fun getDependency() = this.dependency
}