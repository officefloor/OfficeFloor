package net.officefloor.zio

import zio.DefaultRuntime

/**
 * ZIO singleton functionality.
 */
object OfficeFloorZio {

  /**
   * {@link DefaultRuntime} for ZIO.
   */
  val defaultRuntime = new DefaultRuntime {}

}
