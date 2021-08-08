/*-
 * #%L
 * Cats
 * %%
 * Copyright (C) 2005 - 2020 Daniel Sagenschneider
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

package net.officefloor.cats

import cats.effect.IO
import net.officefloor.activity.impl.procedure.ClassProcedureSource
import net.officefloor.activity.procedure.build.{ProcedureArchitect, ProcedureEmployer}
import net.officefloor.compile.test.officefloor.CompileOfficeFloor
import net.officefloor.plugin.managedobject.singleton.Singleton
import net.officefloor.plugin.section.clazz.Parameter
import org.scalatest.flatspec.AnyFlatSpec

/**
 * Tests providing dependency.
 */
class DependencyTest extends AnyFlatSpec {

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
