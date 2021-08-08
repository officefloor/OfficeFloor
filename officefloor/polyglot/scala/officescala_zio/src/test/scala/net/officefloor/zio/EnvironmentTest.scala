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

import zio.{ZEnv, ZIO}
import zio.clock.Clock
import zio.console.Console
import zio.system.System
import zio.random.Random
import zio.blocking.Blocking

import scala.reflect.runtime.universe._

/**
 * Tests environments.
 */
class EnvironmentTest extends TestSpec {

  type Env[R] = ZIO[R, Any, Any]

  def envZEnv: Env[ZEnv] = zioObject

  it can "ZEnv" in {
    valid("ZEnv")
  }

  def envClock: Env[Clock] = zioObject


  it can "Clock" in {
    valid("Clock")
  }

  def envConsole: Env[Console] = zioObject


  it can "Console" in {
    valid("Console")
  }

  def envSystem: Env[System] = zioObject


  it can "System" in {
    valid("System")
  }

  def envRandom: Env[Random] = zioObject


  it can "Random" in {
    valid("Random")
  }

  def envBlocking: Env[Blocking] = zioObject


  it can "Blocking" in {
    valid("Blocking")
  }

  def envAny: Env[Any] = zioObject


  it can "Any" in {
    valid("Any")
  }

  def envAnyRef: Env[Any] = zioObject


  it can "AnyRef" in {
    valid("AnyRef")
  }

  def envNothing: Env[Nothing] = zioObject

  it can "Nothing" in {
    valid("Nothing")
  }

  def envObject: Env[Object] = zioObject

  it can "Object" in {
    valid("Object")
  }

  def envAnyVal: Env[AnyVal] = zioObject

  it should "not AnyVal" in {
    invalid("AnyVal", typeOf[AnyVal])
  }

  def envInt: Env[Int] = zioObject

  it should "not Int" in {
    invalid("Int", typeOf[Int])
  }

  def envString: Env[String] = ZIO.fromFunction((i: String) => String.valueOf(i))

  it should "not String" in {
    invalid("String", typeOf[String])
  }

  def valid(environment: String): Unit =
    success("env" + environment, TestSpec.OBJECT, { builder =>
      builder.addEscalationType(classOf[ZioException])
      builder.setNextArgumentType(classOf[Object])
    })

  def invalid(environment: String, environmentType: Type): Unit =
    invalid("env" + environment, classOf[IllegalArgumentException].getName + ": ZIO environment may not be custom (requiring " + environmentType.typeSymbol.fullName + ")")

}
