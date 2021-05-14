/*-
 * #%L
 * Reactor
 * %%
 * Copyright (C) 2005 - 2020 Daniel Sagenschneider
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */

package net.officefloor.reactor;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.fail;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

import io.vertx.core.Vertx;
import net.officefloor.activity.impl.procedure.ClassProcedureSource;
import net.officefloor.activity.procedure.ProcedureLoaderUtil;
import net.officefloor.activity.procedure.ProcedureType;
import net.officefloor.activity.procedure.ProcedureTypeBuilder;
import net.officefloor.activity.procedure.build.ProcedureArchitect;
import net.officefloor.activity.procedure.build.ProcedureEmployer;
import net.officefloor.compile.spi.office.OfficeArchitect;
import net.officefloor.compile.spi.office.OfficeEscalation;
import net.officefloor.compile.spi.office.OfficeSection;
import net.officefloor.compile.test.officefloor.CompileOfficeFloor;
import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.plugin.section.clazz.Parameter;

/**
 * Abstract {@link Vertx} test functionality.
 * 
 * @author Daniel Sagenschneider
 */
public abstract class AbstractVertxTestCase {

	/**
	 * Success.
	 */
	private static Object success;

	/**
	 * Failure.
	 */
	private static Throwable failure;

	/**
	 * Undertake test for failure.
	 * 
	 * @param methodName       Name of method for procedure.
	 * @param exceptionHandler Handler of the exception.
	 * @param typeBuilder      Builds the expected type.
	 */
	protected void failure(String methodName, Consumer<Throwable> exceptionHandler,
			Consumer<ProcedureTypeBuilder> typeBuilder) {
		this.test(methodName, typeBuilder, (failure, success) -> {
			assertNull(success, "Should be no success");
			assertNotNull(failure, "Should be a failure");
			exceptionHandler.accept(failure);
		});
	}

	/**
	 * Runs test.
	 * 
	 * @param methodName     Name of method for procedure.
	 * @param typeBuilder    Builds the expected {@link ProcedureType}.
	 * @param testAssertions Confirms result of test.
	 */
	protected void test(String methodName, Consumer<ProcedureTypeBuilder> typeBuilder,
			BiConsumer<Throwable, Object> testAssertions) {

		// Ensure correct type
		ProcedureTypeBuilder builder = ProcedureLoaderUtil.createProcedureTypeBuilder(methodName, null);
		if (typeBuilder != null) {
			typeBuilder.accept(builder);
		}
		ProcedureLoaderUtil.validateProcedureType(builder, this.getClass().getName(), methodName);

		// Ensure can invoke procedure and resolve Mono
		CompileOfficeFloor compiler = new CompileOfficeFloor();
		compiler.office((context) -> {
			OfficeArchitect officeArchitect = context.getOfficeArchitect();
			ProcedureArchitect<OfficeSection> procedureArchitect = ProcedureEmployer
					.employProcedureArchitect(officeArchitect, context.getOfficeSourceContext());

			// Create the procedure under test
			OfficeSection procedure = procedureArchitect.addProcedure(methodName, this.getClass().getName(),
					ClassProcedureSource.SOURCE_NAME, methodName, true, null);

			// Capture success
			OfficeSection capture = procedureArchitect.addProcedure("capture", this.getClass().getName(),
					ClassProcedureSource.SOURCE_NAME, "capture", false, null);
			officeArchitect.link(procedure.getOfficeSectionOutput(ProcedureArchitect.NEXT_OUTPUT_NAME),
					capture.getOfficeSectionInput(ProcedureArchitect.INPUT_NAME));

			// Handle failure
			OfficeEscalation escalation = officeArchitect.addOfficeEscalation(Throwable.class.getName());
			OfficeSection handle = procedureArchitect.addProcedure("handle", this.getClass().getName(),
					ClassProcedureSource.SOURCE_NAME, "handle", false, null);
			officeArchitect.link(escalation, handle.getOfficeSectionInput(ProcedureArchitect.INPUT_NAME));
		});
		try (OfficeFloor officeFloor = compiler.compileAndOpenOfficeFloor()) {
			success = null;
			failure = null;
			CompileOfficeFloor.invokeProcess(officeFloor, methodName + ".procedure", null);
			testAssertions.accept(failure, success);
		} catch (Throwable ex) {
			fail(ex);
		}
	}

	public void capture(@Parameter Object parameter) {
		success = parameter;
	}

	public void handle(@Parameter Throwable parameter) {
		failure = parameter;
	}

}
