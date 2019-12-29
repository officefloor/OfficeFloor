package net.officefloor.zio

import net.officefloor.activity.impl.procedure.ClassProcedureSource
import net.officefloor.activity.procedure.build.{ProcedureArchitect, ProcedureEmployer}
import net.officefloor.activity.procedure.{ProcedureLoaderUtil, ProcedureTypeBuilder}
import net.officefloor.compile.test.officefloor.CompileOfficeFloor
import net.officefloor.plugin.section.clazz.Parameter
import net.officefloor.polyglot.scalatest.WoofRules
import org.scalatest.FlatSpec
import zio.ZIO

/**
 * Tests resolution of returning ZIO from procedure.
 */
class ZioProcedureTest extends FlatSpec with WoofRules {

  "ZIO" should "resolve int" in {
    test(classOf[Procedure], "zioReturn", 42, { builder =>
      builder.setNextArgumentType(classOf[Int])
    })
  }

  it must "infer types" in {
    doZioTest("zioReturn")
    doZioTest("uioReturn")
    doZioTest("urioReturn")
    doZioTest("taskReturn")
    doZioTest("rioReturn")
    doZioTest("ioReturn")
    doZioTest("zioUnitReturn")
    doZioTest("intReturn")
    doZioTest("unitReturn")
  }

  def doZioTest(methodName: String): Unit = {
    println("TEST: " + methodName)
    inferZio(classOf[Procedure], methodName) match {
      case ZioReturn(runtime, exception, result) => println("ZIO: " + runtime + ", " + exception + ", " + result)
      case NonZio() => println("Not ZIO return")
    }
    println()
  }

  def inferZio(procedureClass: Class[_], methodName: String): ReflectionResult = {
    import scala.reflect.runtime.universe._

    val m = runtimeMirror(procedureClass.getClassLoader())
    val zioSymbol = typeOf[ZIO[_,_,_]].typeSymbol

    // Determine class from type
    val specialScalaTypes = Array(typeOf[Any], typeOf[AnyVal], typeOf[AnyVal], typeOf[Nothing], typeOf[Null])
    val classFromType: Type => Class[_] = (t: Type) => {
      if (specialScalaTypes.contains(t)) null else m.runtimeClass(t.typeSymbol.asClass)
    }

    // Interrogate class for ZIO return
    val procedureClassSymbol = m.staticClass(procedureClass.getName())
    procedureClassSymbol.info.member(TermName(methodName)) match {
      case method: MethodSymbol => method.returnType.baseType(zioSymbol) match {
        case zioReturnType: TypeRef => {
          val runtimeClass = classFromType(zioReturnType.typeArgs(0))
          val exceptionClass = classFromType(zioReturnType.typeArgs(1))
          val resultClass = classFromType(zioReturnType.typeArgs(2))
          ZioReturn(runtimeClass, exceptionClass, resultClass)
        }
        case _ => NonZio()
      }
      case _ => NonZio()
    }
  }

  trait ReflectionResult

  case class ZioReturn(runtime: Class[_], exception: Class[_], result: Class[_]) extends ReflectionResult

  case class NonZio() extends ReflectionResult

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

