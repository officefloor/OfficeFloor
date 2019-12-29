package net.officefloor.zio

import net.officefloor.activity.impl.procedure.ClassProcedureSource
import net.officefloor.activity.procedure.build.{ProcedureArchitect, ProcedureEmployer}
import net.officefloor.activity.procedure.{ProcedureLoaderUtil, ProcedureTypeBuilder}
import net.officefloor.compile.test.officefloor.CompileOfficeFloor
import net.officefloor.plugin.section.clazz.Parameter
import net.officefloor.polyglot.scalatest.WoofRules
import org.scalatest.FlatSpec

/**
 * Tests resolution of returning ZIO from procedure.
 */
class ZioProcedureTest extends FlatSpec with WoofRules {

  "ZIO" should "resolve int" in {
    test(classOf[Procedure], "zioReturn", 42, { builder =>
      builder.setNextArgumentType(classOf[Int])
    })
  }

  /**
   * Undertakes test.
   *
   * @param procedureClass Class containing the method.
   * @param methodName     Name of method for procedure.
   * @param expectedResult Expected result.
   */
  def test(procedureClass: Class[_], methodName: String, expectedResult: Any, typeBuilder: ProcedureTypeBuilder => Unit): Unit = {

    // Ensure correct type
    val builder = ProcedureLoaderUtil.createProcedureTypeBuilder(methodName, null)
    typeBuilder(builder)
    ProcedureLoaderUtil.validateProcedureType(builder, procedureClass.getName, methodName)

    // Ensure can invoke procedure and resolve ZIO
    val compiler = new CompileOfficeFloor()
    compiler.office { context =>
      val officeArchitect = context.getOfficeArchitect
      val procedureArchitect = ProcedureEmployer.employProcedureArchitect(officeArchitect, context.getOfficeSourceContext)
      val procedure = procedureArchitect.addProcedure(methodName, procedureClass.getName, ClassProcedureSource.SOURCE_NAME, methodName, true, null)
      val capture = procedureArchitect.addProcedure("capture", classOf[ZioProcedureTest].getName, ClassProcedureSource.SOURCE_NAME, "capture", false, null)
      officeArchitect.link(procedure.getOfficeSectionOutput(ProcedureArchitect.NEXT_OUTPUT_NAME), capture.getOfficeSectionInput(ProcedureArchitect.INPUT_NAME))
    }
    val officeFloor = compiler.compileAndOpenOfficeFloor()
    try {
      ZioProcedureTest.result = null
      CompileOfficeFloor.invokeProcess(officeFloor, methodName + ".procedure", null)
      assert(ZioProcedureTest.result == expectedResult)
    } finally {
      officeFloor.close()
    }
  }

}

/**
 * Enable capture of the result.
 */
object ZioProcedureTest {

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

