package net.officefloor.scala

import scala.concurrent.ExecutionContext

object ExecutionContextProcedure {

  var executionContext: ExecutionContext = null

  def reset: Unit = executionContext = null

  def procedure(implicit ec: ExecutionContext): Unit =
    executionContext = ec

}