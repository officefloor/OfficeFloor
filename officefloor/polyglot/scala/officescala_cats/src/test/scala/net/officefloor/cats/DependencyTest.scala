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
import net.officefloor.activity.impl.procedure.ClassProcedureSource
import net.officefloor.activity.procedure.build.{ProcedureArchitect, ProcedureEmployer}
import net.officefloor.compile.test.officefloor.CompileOfficeFloor
import net.officefloor.plugin.managedobject.singleton.Singleton
import net.officefloor.plugin.section.clazz.Parameter
import org.scalatest.FlatSpec

/**
 * Tests providing dependency.
 */
class DependencyTest extends FlatSpec {

  def service(@Parameter param: Int)(implicit dependency: String): IO[String] =
    for {
      d <- effectWithImplicitDependency
      v = s"$d - $param"
    } yield v

  def effectWithImplicitDependency(implicit dependency: String): IO[String] = IO.apply(dependency)

  it can "use implicit dependency" in {

    // Ensure can invoke procedure and resolve IO
    val compiler = new CompileOfficeFloor()
    compiler.office { context =>
      val officeArchitect = context.getOfficeArchitect
      val procedureArchitect = ProcedureEmployer.employProcedureArchitect(officeArchitect, context.getOfficeSourceContext)

      // Provide dependency
      Singleton.load(officeArchitect, "DEPENDENCY")

      // Create procedure with implicit dependency
      val procedure = procedureArchitect.addProcedure("service", this.getClass.getName, ClassProcedureSource.SOURCE_NAME, "service", true, null)

      // Capture success
      val capture = procedureArchitect.addProcedure("capture", classOf[DependencyTest].getName, ClassProcedureSource.SOURCE_NAME, "capture", false, null)
      officeArchitect.link(procedure.getOfficeSectionOutput(ProcedureArchitect.NEXT_OUTPUT_NAME), capture.getOfficeSectionInput(ProcedureArchitect.INPUT_NAME))
    }
    val officeFloor = compiler.compileAndOpenOfficeFloor()
    try {
      DependencyTest.success = null
      CompileOfficeFloor.invokeProcess(officeFloor, "service.procedure", 1)
      assert(DependencyTest.success == "DEPENDENCY - 1")
    } finally {
      officeFloor.close()
    }
  }

}

/**
 * Enable capture of the result.
 */
object DependencyTest {

  /**
   * Captured success.
   */
  var success: String = null

  /**
   * First-class procedure to capture the success.
   *
   * @param param Success.
   */
  def capture(@Parameter param: String): Unit =
    success = param

}
