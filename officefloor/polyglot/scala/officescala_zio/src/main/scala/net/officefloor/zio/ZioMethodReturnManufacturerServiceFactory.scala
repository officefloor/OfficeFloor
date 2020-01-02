package net.officefloor.zio

import net.officefloor.frame.api.source.ServiceContext
import net.officefloor.plugin.managedfunction.method.{MethodReturnManufacturer, MethodReturnManufacturerContext, MethodReturnManufacturerServiceFactory, MethodReturnTranslator}
import zio.{ZEnv, ZIO}

import scala.reflect.runtime.universe._

class ZioMethodReturnManufacturerServiceFactory[A] extends MethodReturnManufacturerServiceFactory with MethodReturnManufacturer[ZIO[Any, _, A], A] {

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

    // Interrogate method for ZIO return
    val method = context.getMethod
    mirror.staticClass(method.getDeclaringClass.getName).info.member(TermName(method.getName)) match {
      case method: MethodSymbol => method.returnType.baseType(typeOf[ZIO[_, _, _]].typeSymbol) match {
        case zioReturnType: TypeRef => {

          // Obtain the ZIO type information
          val runtimeType = zioReturnType.typeArgs(0)
          val failureType = zioReturnType.typeArgs(1)
          val successType = zioReturnType.typeArgs(2)

          // Determine if appropriate environment
          if ((!typeOf[ZEnv].<:<(runtimeType)) && (!Array(typeOf[Any], typeOf[Nothing]).exists(runtimeType.=:=(_)))) {
            throw new IllegalArgumentException("ZIO environment may not be custom (requiring " + runtimeType.typeSymbol.fullName + ")")
          }

          // Determine Java Class from Type
          val classFromType: Type => Class[_] = t => t match {
            case _ if (Array(typeOf[Null], typeOf[Nothing]).exists(t.=:=(_)))  => null
            case _ if (Array(typeOf[Any], typeOf[AnyVal], typeOf[AnyRef]).exists(t.=:=(_))) => classOf[Object]
            case _ => mirror.runtimeClass(t.typeSymbol.asClass)
          }

          // Translate failure/success type to Java Class
          val failureClass = classFromType(failureType)
          val successClass = classFromType(successType)

          // Determine if exception
          if (failureClass != null) {

            // Determine exception
            val throwableClass = if (classOf[Throwable].isAssignableFrom(failureClass)) failureClass.asInstanceOf[Class[_ <: Throwable]] else classOf[ZioException]

            // Load the escalation
            context.addEscalation(throwableClass)
          }

          // Provide translated result type
          context.setTranslatedReturnClass(if (successClass != null) successClass.asInstanceOf[Class[A]] else null)

          // Return translator
          new ZioMethodReturnTranslator[A]()
        }
        case _ => null // not ZIO
      }
      case _ => null // not ZIO
    }
  }

}