package net.officefloor.polyglot.scala

import net.officefloor.frame.api.function.FlowCallback
import net.officefloor.plugin.clazz.FlowInterface

@FlowInterface
trait Flows {
  def flow()
  def flowWithCallback(callback: FlowCallback)
  def flowWithParameterAndCallback(parameter: String, callback: FlowCallback)
  def flowWithParameter(parameter: String)
}