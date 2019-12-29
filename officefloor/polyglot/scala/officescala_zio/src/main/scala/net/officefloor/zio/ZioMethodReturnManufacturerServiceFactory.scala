package net.officefloor.zio

import net.officefloor.frame.api.source.ServiceContext
import net.officefloor.plugin.managedfunction.method.{MethodReturnManufacturer, MethodReturnManufacturerContext, MethodReturnManufacturerServiceFactory, MethodReturnTranslator}
import zio.ZIO

import scala.reflect.runtime.universe._

class ZioMethodReturnManufacturerServiceFactory[A] extends MethodReturnManufacturerServiceFactory with MethodReturnManufacturer[ZIO[Any, _, A], A] {

  /**
   * {@link Type} instances that can not be converted to a Java {@link Class}.
   */
  val nonJavaTypes = Array(typeOf[Any], typeOf[AnyVal], typeOf[AnyVal], typeOf[Nothing], typeOf[Null])

  /*
   * ================== MethodReturnManufacturerServiceFactory ==================
   */

  override def createService(serviceContext: ServiceContext): MethodReturnManufacturer[ZIO[Any, _, A], A] = this

  /*
 * ========================= MethodReturnManufacturer ===========================
 */

  override def createReturnTranslator(context: MethodReturnManufacturerContext[A]): MethodReturnTranslator[ZIO[Any, _, A], A] = {

    // Create the mirror
    val mirror = runtimeMirror(context.getSourceContext.getClassLoader)
    val zioSymbol = typeOf[ZIO[_, _, _]].typeSymbol

    // Determine class from type
    val classFromType: Type => Class[_] = (t: Type) => if (nonJavaTypes.contains(t)) null else mirror.runtimeClass(t.typeSymbol.asClass)

    // Obtain the method
    val method = context.getMethod

    // Interrogate class for ZIO return
    mirror.staticClass(method.getDeclaringClass.getName).info.member(TermName(method.getName)) match {
      case method: MethodSymbol => method.returnType.baseType(zioSymbol) match {
        case zioReturnType: TypeRef => {

          // Obtain the ZIO type information
          val runtimeClass = classFromType(zioReturnType.typeArgs(0))
          val exceptionClass = classFromType(zioReturnType.typeArgs(1))
          val resultClass = classFromType(zioReturnType.typeArgs(2))

          // Ensure not custom environment (Any that should result in null)
          if (runtimeClass != null) {
            throw new IllegalArgumentException("ZIO environment may not be custom")
          }

          // Determine if exception
          if (exceptionClass != null) {
            // TODO allow adding exception
          }

          // Provide translated result
          context.setTranslatedReturnClass(resultClass.asInstanceOf[Class[A]])

          // Return translator
          new ZioMethodReturnTranslator[A]()
        }
        case _ => null // not ZIO return
      }
      case _ => null // not ZIO return
    }
  }

}