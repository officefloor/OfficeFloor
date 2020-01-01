package net.officefloor.zio

import junit.framework.AssertionFailedError
import net.officefloor.activity.impl.procedure.ClassProcedureSource
import net.officefloor.activity.procedure.build.{ProcedureArchitect, ProcedureEmployer}
import net.officefloor.activity.procedure.{ProcedureLoaderUtil, ProcedureTypeBuilder}
import net.officefloor.compile.test.officefloor.CompileOfficeFloor
import net.officefloor.plugin.section.clazz.Parameter
import org.scalatest.FlatSpec

/**
 * Test spec.
 */
trait TestSpec extends FlatSpec {

  /**
   * Undertakes test for valid ZIO.
   *
   * @param methodName      Name of method for procedure.
   * @param expectedSuccess Expected success.
   * @param typeBuilder     Builds the expected type.
   */
  def valid(methodName: String, expectedSuccess: Any, typeBuilder: ProcedureTypeBuilder => Unit): Unit = {

    // Ensure correct type
    val builder = ProcedureLoaderUtil.createProcedureTypeBuilder(methodName, null)
    if (typeBuilder != null) {
      typeBuilder(builder)
    }
    ProcedureLoaderUtil.validateProcedureType(builder, classOf[Procedures].getName, methodName)

    // Ensure can invoke procedure and resolve ZIO
    val compiler = new CompileOfficeFloor()
    compiler.office { context =>
      val officeArchitect = context.getOfficeArchitect
      val procedureArchitect = ProcedureEmployer.employProcedureArchitect(officeArchitect, context.getOfficeSourceContext)
      val procedure = procedureArchitect.addProcedure(methodName, classOf[Procedures].getName, ClassProcedureSource.SOURCE_NAME, methodName, true, null)
      val capture = procedureArchitect.addProcedure("capture", classOf[TestSpec].getName, ClassProcedureSource.SOURCE_NAME, "capture", false, null)
      officeArchitect.link(procedure.getOfficeSectionOutput(ProcedureArchitect.NEXT_OUTPUT_NAME), capture.getOfficeSectionInput(ProcedureArchitect.INPUT_NAME))
    }
    val officeFloor = compiler.compileAndOpenOfficeFloor()
    try {
      TestSpec.result = null
      CompileOfficeFloor.invokeProcess(officeFloor, methodName + ".procedure", null)
      assert(TestSpec.result == expectedSuccess)
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
      ProcedureLoaderUtil.validateProcedureType(builder, classOf[Procedures].getName, methodName)
      fail("Should not be successful")
    } catch {
      case ex: AssertionFailedError => assert(ex.getMessage.contains(errorMessage))
    }
  }

  def log(msg: String): String = {
    println(msg)
    msg
  }

}

/**
 * Enable capture of the result.
 */
object TestSpec {

  /**
   * Captured result.
   */
  var result: Any = null

  /**
   * First-class procedure to capture the result.
   *
   * @param param Result.
   */
  def capture(@Parameter param: Any): Unit =
    result = param

}

