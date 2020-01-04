/*-
 * #%L
 * ZIO
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

package net.officefloor.zio

import java.util.concurrent.Executor
import java.util.logging.Logger

import zio.{DefaultRuntime, Runtime, ZEnv, internal}

import scala.concurrent.ExecutionContext

/**
 * ZIO singleton functionality.
 */
object OfficeFloorZio {

  /**
   * {@link DefaultRuntime} for ZIO.
   */
  val defaultRuntime: Runtime[ZEnv] = new DefaultRuntime {}.withFatal(f => false)

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
