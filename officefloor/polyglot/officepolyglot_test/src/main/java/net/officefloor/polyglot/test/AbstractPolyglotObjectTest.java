package net.officefloor.polyglot.test;

import net.officefloor.compile.test.officefloor.CompileOfficeContext;
import net.officefloor.compile.test.officefloor.CompileOfficeFloor;
import net.officefloor.compile.test.officefloor.CompileVar;
import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.frame.api.managedobject.source.ManagedObjectSource;
import net.officefloor.frame.test.OfficeFrameTestCase;
import net.officefloor.plugin.managedobject.singleton.Singleton;
import net.officefloor.plugin.variable.Out;

/**
 * Abstract tests for polyglot object via {@link ManagedObjectSource}.
 * 
 * @author Daniel Sagenschneider
 */
public abstract class AbstractPolyglotObjectTest extends OfficeFrameTestCase {

	/**
	 * {@link OfficeFloor}.
	 */
	private OfficeFloor officeFloor;

	@Override
	protected void tearDown() throws Exception {
		if (this.officeFloor != null) {
			this.officeFloor.closeOfficeFloor();
		}
	}

	/**
	 * Direct object.
	 */
	public void testDirectObject() {
		ObjectInterface object = this.create();
		assertEquals("Incorrect value", "test", object.getValue());
		assertNull("Should not dependency inject", object.getDependency());
	}

	protected abstract ObjectInterface create();

	/**
	 * Invoke object.
	 */
	public void testInvokeObject() throws Throwable {
		CompileOfficeFloor compiler = new CompileOfficeFloor();
		CompileVar<ObjectInterface> var = new CompileVar<>();
		JavaObject dependency = new JavaObject("DEPENDENCY");
		compiler.office((context) -> {

			// Configure in the object
			this.object(context);

			// Configure dependency
			value(context, dependency);

			// Capture object
			context.variable(null, ObjectInterface.class, var);

			// Function to use the object
			context.addSection("SECTION", ObjectLogic.class);
		});
		this.officeFloor = compiler.compileAndOpenOfficeFloor();
		CompileOfficeFloor.invokeProcess(this.officeFloor, "SECTION.service", null);
		ObjectInterface object = var.getValue();
		assertNotNull("Should have object", object);
		assertEquals("Incorrect value", "test", object.getValue());
		assertSame("Incorrect dependency", dependency, object.getDependency());
	}

	public static class ObjectLogic {
		public void service(ObjectInterface object, Out<ObjectInterface> out) {
			out.set(object);
		}
	}

	protected abstract void object(CompileOfficeContext context);

	/**
	 * Loads a value.
	 * 
	 * @param context {@link CompileOfficeContext}.
	 * @param value   Value.
	 */
	private static void value(CompileOfficeContext context, Object value) {
		Singleton.load(context.getOfficeArchitect(), value);
	}

}