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

import cats.effect.unsafe.{IORuntime, IORuntimeConfig, Scheduler}
import net.officefloor.cats.IORuntimeClassDependencyFactory.createIORuntime
import net.officefloor.frame.api.administration.AdministrationContext
import net.officefloor.frame.api.build.Indexed
import net.officefloor.frame.api.function.ManagedFunctionContext
import net.officefloor.frame.api.managedobject.{ManagedObject, ManagedObjectContext, ObjectRegistry}
import net.officefloor.plugin.clazz.dependency.ClassDependencyFactory

import java.util
import java.util.concurrent.{Callable, Executor, Future, ScheduledExecutorService, ScheduledFuture, TimeUnit}
import scala.concurrent.ExecutionContext

/**
 * {@link ClassDependencyFactory} for {@link IORuntime}.
 */
class IORuntimeClassDependencyFactory extends ClassDependencyFactory {

  /*
   * ======================== MethodParameterFactory =========================
   */

  override def createDependency(managedObject: ManagedObject, managedObjectContext: ManagedObjectContext, objectRegistry: ObjectRegistry[Indexed]): AnyRef =
    throw new IllegalStateException(s"Can not obtain IORuntime for ${classOf[ManagedObject].getSimpleName}")

  override def createDependency(context: ManagedFunctionContext[Indexed, Indexed]): AnyRef =
    createIORuntime(context.getExecutor)

  override def createDependency(context: AdministrationContext[AnyRef, Indexed, Indexed]): AnyRef =
    createIORuntime(context.getExecutor)
}

object IORuntimeClassDependencyFactory {

  def createIORuntime(executor: Executor): IORuntime = {
    val executionContext = ExecutionContext.fromExecutor(executor)
    val scheduler: Scheduler = Scheduler.fromScheduledExecutor(UnsupportedScheduledExecutor)
    IORuntime(executionContext, executionContext, scheduler, () => (), IORuntimeConfig.apply())
  }
}

object UnsupportedScheduledExecutor extends ScheduledExecutorService {

  def unsupported[R](): R = throw new UnsupportedOperationException("Scheduling not supported for IO")

  override def schedule(runnable: Runnable, l: Long, timeUnit: TimeUnit): ScheduledFuture[_] = unsupported()

  override def schedule[V](callable: Callable[V], l: Long, timeUnit: TimeUnit): ScheduledFuture[V] = unsupported()

  override def scheduleAtFixedRate(runnable: Runnable, l: Long, l1: Long, timeUnit: TimeUnit): ScheduledFuture[_] = unsupported()

  override def scheduleWithFixedDelay(runnable: Runnable, l: Long, l1: Long, timeUnit: TimeUnit): ScheduledFuture[_] = unsupported()

  override def shutdown(): Unit = unsupported()

  override def shutdownNow(): util.List[Runnable] = unsupported()

  override def isShutdown: Boolean = unsupported()

  override def isTerminated: Boolean = unsupported()

  override def awaitTermination(l: Long, timeUnit: TimeUnit): Boolean = unsupported()

  override def submit[T](callable: Callable[T]): Future[T] = unsupported()

  override def submit[T](runnable: Runnable, t: T): Future[T] = unsupported()

  override def submit(runnable: Runnable): Future[_] = unsupported()

  override def invokeAll[T](collection: util.Collection[_ <: Callable[T]]): util.List[Future[T]] = unsupported()

  override def invokeAll[T](collection: util.Collection[_ <: Callable[T]], l: Long, timeUnit: TimeUnit): util.List[Future[T]] = unsupported()

  override def invokeAny[T](collection: util.Collection[_ <: Callable[T]]): T = unsupported()

  override def invokeAny[T](collection: util.Collection[_ <: Callable[T]], l: Long, timeUnit: TimeUnit): T = unsupported()

  override def execute(runnable: Runnable): Unit = unsupported()
}
