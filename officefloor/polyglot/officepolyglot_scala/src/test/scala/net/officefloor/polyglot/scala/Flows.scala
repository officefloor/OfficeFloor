package net.officefloor.polyglot.scala

import net.officefloor.plugin.managedfunction.clazz.FlowInterface
import net.officefloor.frame.api.function.FlowCallback
import java.io.IOException

@FlowInterface
trait Flows {
  def flow()
  def flowWithCallback(callback: FlowCallback)
  def flowWithParameterAndCallback(parameter: String, callback: FlowCallback)
  def flowWithParameter(parameter: String)
  def exception(ex: IOException)
}
