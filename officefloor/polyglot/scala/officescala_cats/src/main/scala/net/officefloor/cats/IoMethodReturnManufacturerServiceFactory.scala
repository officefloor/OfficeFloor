package net.officefloor.cats

import cats.effect.IO
import net.officefloor.frame.api.source.ServiceContext
import net.officefloor.plugin.managedfunction.method.{MethodReturnManufacturer, MethodReturnManufacturerContext, MethodReturnManufacturerServiceFactory, MethodReturnTranslator}

import scala.reflect.runtime.universe._

/**
 * {@link MethodReturnManufacturerServiceFactory} for IO.
 *
 * @tparam A Success type.
 */
class IoMethodReturnManufacturerServiceFactory[A] extends MethodReturnManufacturerServiceFactory with MethodReturnManufacturer[IO[A], A] {

  /*
   * ================== MethodReturnManufacturerServiceFactory ==================
   */

  override def createService(serviceContext: ServiceContext): MethodReturnManufacturer[IO[A], A] = this

  /*
 * ========================= MethodReturnManufacturer ===========================
 */

  override def createReturnTranslator(context: MethodReturnManufacturerContext[A]): MethodReturnTranslator[IO[A], A] = {

    // Create the mirror
    val mirror = runtimeMirror(context.getSourceContext.getClassLoader)

    // Interrogate method for IO return
    val method = context.getMethod
    mirror.staticClass(method.getDeclaringClass.getName).info.member(TermName(method.getName)) match {
      case method: MethodSymbol => method.returnType.baseType(typeOf[IO[_]].typeSymbol) match {
        case ioReturnType: TypeRef => {

          // Obtain the IO type information
          val successType = ioReturnType.typeArgs(0)

          // Determine Java Class from Type
          val classFromType: Type => Class[_] = t => t match {
            case _ if (Array(typeOf[Null], typeOf[Nothing]).exists(t.=:=(_)))  => null
            case _ if (Array(typeOf[Any], typeOf[AnyVal], typeOf[AnyRef]).exists(t.=:=(_))) => classOf[Object]
            case _ => mirror.runtimeClass(t.typeSymbol.asClass)
          }

          // Translate failure/success type to Java Class
          val successClass = classFromType(successType)

          // Provide translated result type
          context.setTranslatedReturnClass(if (successClass != null) successClass.asInstanceOf[Class[A]] else null)

          // Always Throwable from IO
          context.addEscalation(classOf[Throwable])

          // Return translator
          new IoMethodReturnTranslator[A]()
        }
        case _ => null // not IO
      }
      case _ => null // not IO
    }
  }

}