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
      .withReportFailure(f => logger.info(f.prettyPrint))
}
