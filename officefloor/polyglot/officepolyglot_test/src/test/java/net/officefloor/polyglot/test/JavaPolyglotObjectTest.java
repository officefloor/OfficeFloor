package net.officefloor.polyglot.test;

import net.officefloor.compile.test.officefloor.CompileOfficeContext;
import net.officefloor.frame.internal.structure.ManagedObjectScope;
import net.officefloor.plugin.managedobject.clazz.ClassManagedObjectSource;
import net.officefloor.plugin.managedobject.clazz.Dependency;

/**
 * Confirms the tests with {@link ClassManagedObjectSource}.
 * 
 * @author Daniel Sagenschneider
 */
public class JavaPolyglotObjectTest extends AbstractPolyglotObjectTest {

	@Override
	protected ObjectInterface create() {
		return new ObjectInterfaceImpl();
	}

	@Override
	protected void object(CompileOfficeContext context) {
		context.addManagedObject("OBJECT", ObjectInterfaceImpl.class, ManagedObjectScope.THREAD);
	}

	public static class ObjectInterfaceImpl implements ObjectInterface {

		@Dependency
		private JavaObject dependency;

		@Override
		public String getValue() {
			return "test";
		}

		@Override
		public JavaObject getDependency() {
			return this.dependency;
		}
	}

}