package net.officefloor.zio

import junit.framework.AssertionFailedError
import net.officefloor.activity.impl.procedure.ClassProcedureSource
import net.officefloor.activity.procedure.build.{ProcedureArchitect, ProcedureEmployer}
import net.officefloor.activity.procedure.{ProcedureLoaderUtil, ProcedureTypeBuilder}
import net.officefloor.compile.test.officefloor.CompileOfficeFloor
import net.officefloor.plugin.section.clazz.Parameter
import org.scalatest.FlatSpec

import scala.reflect.runtime.universe._

/**
 * Test spec.
 */
trait TestSpec extends FlatSpec {

  val OBJECT_CLASS = classOf[Object]

  /**
   * Undertakes test of type.
   *
   * @param methodName   Name of method for procedure.
   * @param failureClass Expected failure class.
   * @param successClass Expected success class.
   */
  def testType(methodName: String, failureClass: Class[_], successClass: Class[_]): Unit = test(methodName, Procedures.OBJECT, { builder =>
    if (failureClass != null) {
      // TODO load expected exception
    }
    if (successClass != null) {
      builder.setNextArgumentType(successClass)
    }
  })

  /**
   * Undertakes test against Procedure class.
   *
   * @param methodName      Name of method for procedure.
   * @param expectedSuccess Expected success.
   * @param typeBuilder     Builds the expected type.
   */
  def test(methodName: String, expectedSuccess: Any, typeBuilder: ProcedureTypeBuilder => Unit): Unit = {

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
   * Undertake test for invalid environment.
   *
   * @param methodName  Name of method for procedure.
   * @param runtimeType Runtime { @link Type}.
   */
  def testInvalidEnvironment(methodName: String, runtimeType: Type): Unit = {
    // Ensure not able to load type
    val builder = ProcedureLoaderUtil.createProcedureTypeBuilder(methodName, null)
    try {
      ProcedureLoaderUtil.validateProcedureType(builder, classOf[Procedures].getName, methodName)
      fail("Should not be successful")
    } catch {
      case ex: AssertionFailedError => assert(ex.getMessage.contains("ZIO environment may not be custom (requiring " + runtimeType.typeSymbol.fullName + ")"))
    }
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

