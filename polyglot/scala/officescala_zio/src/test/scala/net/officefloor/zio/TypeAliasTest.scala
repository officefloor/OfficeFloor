/*-
 * #%L
 * ZIO
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

package net.officefloor.zio

import zio._

/**
 * Tests type aliases.
 */
class TypeAliasTest extends TestSpec {

  type CustomZio = ZIO[ZEnvironment[Any], Throwable, Object]

  it can "ZIO" in {
    typeAlias("ZIO", classOf[Throwable], classOf[Object])
  }

  def typeZIO: ZIO[Any, Throwable, Object] = zioObject

  it can "UIO" in {
    typeAlias("UIO", null, classOf[Object])
  }

  def typeUIO: UIO[Object] = zioObject

  it can "URIO" in {
    typeAlias("URIO", null, classOf[Object])
  }

  def typeURIO: URIO[ZEnvironment[Any], Object] = zioObject

  it can "Task" in {
    typeAlias("Task", classOf[Throwable], classOf[Object])
  }

  def typeTask: Task[Object] = zioObject

  it can "RIO" in {
    typeAlias("RIO", classOf[Throwable], classOf[Object])
  }

  def typeRIO: RIO[ZEnvironment[Any], Object] = zioObject

  it can "IO" in {
    typeAlias("IO", classOf[Throwable], classOf[Object])
  }

  def typeIO: IO[Throwable, Object] = zioObject

  def typeCustomZio: CustomZio = zioObject

  it can "CustomZIO" in {
    typeAlias("CustomZio", classOf[Throwable], classOf[Object])
  }

  def typeAlias(typeAlias: String, failureClass: Class[_ <: Throwable], successClass: Class[_]): Unit =
    success("type" + typeAlias, TestSpec.OBJECT, { builder =>
      if (failureClass != null) {
        builder.addEscalationType(failureClass)
      }
      if (successClass != null) {
        builder.setNextArgumentType(successClass)
      }
    })

}
