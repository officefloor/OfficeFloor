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

import java.util.concurrent.Executor
import java.util.logging.Logger

import zio.{Runtime, ZEnv, internal}

import scala.concurrent.ExecutionContext

/**
 * ZIO singleton functionality.
 */
object OfficeFloorZio {

  /**
   * {@link Runtime} for ZIO.
   */
  val defaultRuntime: Runtime[ZEnv] = Runtime.default.withFatal(f => false)

  /**
   * Creates {@link Runtime}.
   *
   * @param executor { @link Executor}.
   * @param logger   { @link Logger}.
   * @return { @link Runtime}.
   */
  def runtime(executor: Executor, logger: Logger): Runtime[ZEnv] =
    defaultRuntime
      .withExecutor(internal.Executor.fromExecutionContext(defaultRuntime.platform.executor.yieldOpCount)(ExecutionContext.fromExecutor(executor)))
      .withReportFailure(f => logger.fine(f.prettyPrint))
}
