package net.officefloor.tutorial.catshttpserver

import java.sql.Connection

import cats.effect.IO
import doobie.util.transactor.Transactor
import net.officefloor.frame.api.build.{Indexed, None}
import net.officefloor.frame.api.managedobject.ManagedObject
import net.officefloor.frame.api.managedobject.source.impl.{AbstractAsyncManagedObjectSource, AbstractManagedObjectSource}

/**
 * {@link ManagedObjectSource} for Transactor.
 */
// START SNIPPET: tutorial
class TransactorManagedObjectSource extends AbstractManagedObjectSource[Indexed, None] {

  override def loadSpecification(specificationContext: AbstractAsyncManagedObjectSource.SpecificationContext): Unit = ()

  override def loadMetaData(metaDataContext: AbstractAsyncManagedObjectSource.MetaDataContext[Indexed, None]): Unit = {
    metaDataContext.setManagedObjectClass(classOf[TransactorManagedObject])
    metaDataContext.setObjectClass(classOf[Transactor[IO]])
    metaDataContext.addDependency(classOf[Connection])
  }

  override def getManagedObject: ManagedObject = new TransactorManagedObject()
}
// END SNIPPET: tutorial