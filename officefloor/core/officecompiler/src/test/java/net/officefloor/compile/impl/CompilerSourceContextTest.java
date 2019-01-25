package net.officefloor.compile.impl;

import java.io.ByteArrayInputStream;
import java.io.InputStreamReader;
import java.io.StringReader;

import net.officefloor.compile.OfficeFloorCompiler;
import net.officefloor.frame.api.source.SourceContext;
import net.officefloor.frame.test.MockClockFactory;
import net.officefloor.frame.test.OfficeFrameTestCase;

/**
 * Ensure able to create root {@link SourceContext} from
 * {@link OfficeFloorCompiler}.
 * 
 * @author Daniel Sagenschneider
 */
public class CompilerSourceContextTest extends OfficeFrameTestCase {

	/**
	 * Ensure able to create {@link SourceContext}.
	 */
	public void testCreateSourceContext() {

		// Create the class loader
		ClassLoader classLoader = createNewClassLoader();

		// Create the compiler with class loader
		OfficeFloorCompiler compiler = OfficeFloorCompiler.newOfficeFloorCompiler(classLoader);

		// Create clock factory
		long mockCurrentTime = 2000;
		MockClockFactory clockFactory = new MockClockFactory(mockCurrentTime);

		// Load details
		compiler.setClockFactory(clockFactory);
		compiler.addResources((location) -> {
			if (!"TEST".equals(location)) {
				return null;
			}
			return new ByteArrayInputStream("RESOURCE".getBytes());
		});

		// Create the source context
		SourceContext context = compiler.createRootSourceContext();

		// Ensure correct details
		assertSame("Incorrect class loader", classLoader, context.getClassLoader());
		assertEquals("Incorrect clock", Long.valueOf(mockCurrentTime), context.getClock((time) -> time).getTime());
		assertContents(new StringReader("RESOURCE"), new InputStreamReader(context.getResource("TEST")));
	}

}