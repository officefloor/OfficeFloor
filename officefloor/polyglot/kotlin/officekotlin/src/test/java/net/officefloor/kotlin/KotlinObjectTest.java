package net.officefloor.kotlin;

import net.officefloor.compile.test.officefloor.CompileOfficeContext;
import net.officefloor.frame.api.managedobject.ManagedObject;
import net.officefloor.frame.internal.structure.ManagedObjectScope;
import net.officefloor.polyglot.test.AbstractPolyglotObjectTest;
import net.officefloor.polyglot.test.ObjectInterface;

/**
 * Tests adapting Kotlin {@link Object} for {@link ManagedObject}.
 * 
 * @author Daniel Sagenschneider
 */
public class KotlinObjectTest extends AbstractPolyglotObjectTest {

	@Override
	protected ObjectInterface create() {
		return new KotlinObject();
	}

	@Override
	protected void object(CompileOfficeContext context) {
		context.addManagedObject("OBJECT", KotlinObject.class, ManagedObjectScope.THREAD);
	}

}