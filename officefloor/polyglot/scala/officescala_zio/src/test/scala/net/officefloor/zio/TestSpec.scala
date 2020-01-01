package net.officefloor.zio

import net.officefloor.frame.api.manage.OfficeFloor
import junit.framework.AssertionFailedError
import net.officefloor.activity.impl.procedure.ClassProcedureSource
import net.officefloor.activity.procedure.build.{ProcedureArchitect, ProcedureEmployer}
import net.officefloor.activity.procedure.{ProcedureLoaderUtil, ProcedureTypeBuilder}
import net.officefloor.compile.test.officefloor.CompileOfficeFloor
import net.officefloor.plugin.section.clazz.Parameter
import org.scalatest.FlatSpec
import zio.ZIO

/**
 * Test spec.
 */
trait TestSpec extends FlatSpec {

  /**
   * Convenience method to create successful ZIO for Object.
   *
   * @return ZIO for Object.
   */
  def zioObject = ZIO.succeed(TestSpec.OBJECT)

  /**
   * Undertakes test for successful ZIO.
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
   * Undertake test for failed ZIO.
   *
   * @param methodName    Name of method for procedure.
   * @param exceptionType Expected failure type.
   * @param message       Expected content of failure.
   * @param typeBuilder   Builds the expected type.
   */
  def failure(methodName: String, exceptionType: Class[_ <: Throwable], message: String, typeBuilder: ProcedureTypeBuilder => Unit): Unit =
    test(methodName, typeBuilder, { officeFloor =>
      assert(TestSpec.success == null)
      assert(TestSpec.failure != null)
      assert(TestSpec.failure.getClass == exceptionType)
      assert(TestSpec.failure.getMessage.contains(message))
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

    // Ensure can invoke procedure and resolve ZIO
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
   * Undertakes test for invalid ZIO.
   *
   * @param methodName   Name of method for procedure.
   * @param errorMessage Error message.
   */
  def invalid(methodName: String, errorMessage: String): Unit = {
    // Ensure not able to load type
    val builder = ProcedureLoaderUtil.createProcedureTypeBuilder(methodName, null)
    try {
      ProcedureLoaderUtil.validateProcedureType(builder, this.getClass.getName, methodName)
      fail("Should not be successful")
    } catch {
      case ex: AssertionFailedError => assert(ex.getMessage.contains(errorMessage))
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