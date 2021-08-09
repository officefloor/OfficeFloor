/*-
 * #%L
 * Cats
 * %%
 * Copyright (C) 2005 - 2020 Daniel Sagenschneider
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

package net.officefloor.cats

import cats.effect.IO
import net.officefloor.activity.impl.procedure.ClassProcedureSource
import net.officefloor.activity.procedure.{ProcedureLoaderUtil, ProcedureTypeBuilder}
import net.officefloor.activity.procedure.build.{ProcedureArchitect, ProcedureEmployer}
import net.officefloor.compile.test.officefloor.CompileOfficeFloor
import net.officefloor.frame.api.manage.OfficeFloor
import net.officefloor.plugin.section.clazz.Parameter
import org.scalatest.flatspec.AnyFlatSpec

import scala.util.{Failure, Try}

/**
 * Test spec.
 */
class TestSpec extends AnyFlatSpec {

  /**
   * Convenience method to create successful IO for Object.
   *
   * @return IO for Object.
   */
  def ioObject = IO.pure(TestSpec.OBJECT)

  /**
   * Undertakes test for successful IO.
   *
   * @param methodName      Name of method for procedure.
   * @param expectedSuccess Expected success.
   * @param typeBuilder     Builds the expected type.
   */
  def success(methodName: String, expectedSuccess: Any, typeBuilder: ProcedureTypeBuilder => Unit): Unit =
    test(methodName, typeBuilder, { officeFloor =>
      assert(TestSpec.failure == null)
      assert(TestSpec.success == expectedSuccess)
    })

  /**
   * Runs test.
   *
   * @param methodName  Name of method for procedure.
   * @param typeBuilder Builds the expected type.
   * @param testRunner  Test runner.
   */
  def test(methodName: String, typeBuilder: ProcedureTypeBuilder => Unit, testRunner: OfficeFloor => Unit): Unit = {

    // Ensure correct type
    val builder = ProcedureLoaderUtil.createProcedureTypeBuilder(methodName, null)
    if (typeBuilder != null) {
      typeBuilder(builder)
    }
    ProcedureLoaderUtil.validateProcedureType(builder, this.getClass.getName, methodName)

    // Ensure can invoke procedure and resolve IO
    val compiler = new CompileOfficeFloor()
    compiler.office { context =>
      val officeArchitect = context.getOfficeArchitect
      val procedureArchitect = ProcedureEmployer.employProcedureArchitect(officeArchitect, context.getOfficeSourceContext)

      // Create procedure under test
      val procedure = procedureArchitect.addProcedure(methodName, this.getClass.getName, ClassProcedureSource.SOURCE_NAME, methodName, true, null)

      // Capture success
      val capture = procedureArchitect.addProcedure("capture", classOf[TestSpec].getName, ClassProcedureSource.SOURCE_NAME, "capture", false, null)
      officeArchitect.link(procedure.getOfficeSectionOutput(ProcedureArchitect.NEXT_OUTPUT_NAME), capture.getOfficeSectionInput(ProcedureArchitect.INPUT_NAME))

      // Handle failure
      val exception = officeArchitect.addOfficeEscalation(classOf[Throwable].getName)
      val handle = procedureArchitect.addProcedure("handle", classOf[TestSpec].getName, ClassProcedureSource.SOURCE_NAME, "handle", false, null)
      officeArchitect.link(exception, handle.getOfficeSectionInput(ProcedureArchitect.INPUT_NAME))
    }
    val officeFloor = compiler.compileAndOpenOfficeFloor()
    try {
      TestSpec.success = null
      TestSpec.failure = null
      CompileOfficeFloor.invokeProcess(officeFloor, methodName + ".procedure", null)
      testRunner(officeFloor)
    } finally {
      officeFloor.close()
    }
  }

  /**
   * Undertake test for failed IO.
   *
   * @param methodName       Name of method for procedure.
   * @param exceptionHandler Handler of the exception.
   * @param typeBuilder      Builds the expected type.
   */
  def failure(methodName: String, exceptionHandler: Throwable => Unit, typeBuilder: ProcedureTypeBuilder => Unit): Unit =
    test(methodName, typeBuilder, { officeFloor =>
      assert(TestSpec.success == null)
      assert(TestSpec.failure != null)
      exceptionHandler(TestSpec.failure)
    })

  /**
   * Undertakes test for invalid IO.
   *
   * @param methodName   Name of method for procedure.
   * @param errorMessage Error message.
   */
  def invalid(methodName: String, errorMessage: String): Unit = {
    // Ensure not able to load type
    val builder = ProcedureLoaderUtil.createProcedureTypeBuilder(methodName, null)
    Try(ProcedureLoaderUtil.validateProcedureType(builder, this.getClass.getName, methodName)) match {
      case Failure(ex) => assert(ex.getMessage.contains(errorMessage))
      case _ => fail("Should not be successful")
    }
  }

}

/**
 * Enable capture of the result.
 */
object TestSpec {

  /**
   * Singleton Object.
   */
  val OBJECT = new Object()

  /**
   * Captured success.
   */
  var success: Any = null

  /**
   * Captured failure.
   */
  var failure: Throwable = null

  /**
   * First-class procedure to capture the success.
   *
   * @param param Success.
   */
  def capture(@Parameter param: Any): Unit =
    success = param

  /**
   * First-class procedure to handle failure.
   *
   * @param param Failure.
   */
  def handle(@Parameter param: Throwable): Unit =
    failure = param

}
