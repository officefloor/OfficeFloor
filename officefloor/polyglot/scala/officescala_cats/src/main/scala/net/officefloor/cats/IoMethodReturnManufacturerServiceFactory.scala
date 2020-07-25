/*-
 * #%L
 * Cats
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

package net.officefloor.cats

import cats.effect.IO
import net.officefloor.frame.api.source.ServiceContext
import net.officefloor.plugin.clazz.method.{MethodReturnManufacturer, MethodReturnManufacturerContext, MethodReturnManufacturerServiceFactory, MethodReturnTranslator}

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

    // Find the member on possible class
    val clazz = mirror.staticClass(method.getDeclaringClass.getName)
    val clazzMember = clazz.info.member(TermName(method.getName))
    val member = clazzMember match {
      case m: MethodSymbol => clazzMember
      case _ => {
        // Not on class, so determine if on module
        val module = mirror.staticModule(method.getDeclaringClass.getName)
        module.info.member(TermName(method.getName))
      }
    }

    // Attempt to determine if translate
    member match {
      case methodSymbol: MethodSymbol => methodSymbol.returnType.baseType(typeOf[IO[_]].typeSymbol) match {
        case ioReturnType: TypeRef => {

          // Obtain the IO type information
          val successType = ioReturnType.typeArgs(0)

          // Determine Java Class from Type
          val classFromType: Type => Class[_] = t => t match {
            case _ if (Array(typeOf[Null], typeOf[Nothing]).exists(t.=:=(_))) => null
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
