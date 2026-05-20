package net.officefloor.tutorial.catshttpserver

import java.sql.Connection
import cats.effect.IO
import doobie.util.ExecutionContexts
import doobie.util.log.LogHandler
import doobie.util.transactor.Transactor
import net.officefloor.frame.api.build.Indexed
import net.officefloor.frame.api.managedobject.{CoordinatingManagedObject, ObjectRegistry}

import java.util.logging.Logger

/**
 * {@link CoordinatingManagedObject} for the Transactor.
 */
// START SNIPPET: tutorial
class TransactorManagedObject(logger: Logger) extends CoordinatingManagedObject[Indexed] {

  var transactor: Transactor[IO] = null

  override def loadObjects(objectRegistry: ObjectRegistry[Indexed]): Unit = {
    val connection = objectRegistry.getObject(0).asInstanceOf[Connection]
    this.transactor = Transactor.fromConnection[IO](connection, Some(new JavaUtilLogHandler(this.logger)))
  }

  override def getObject: AnyRef = transactor
}
// END SNIPPET: tutorial