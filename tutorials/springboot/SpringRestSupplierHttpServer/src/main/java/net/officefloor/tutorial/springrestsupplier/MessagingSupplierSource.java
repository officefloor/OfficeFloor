package net.officefloor.tutorial.springrestsupplier;

import net.officefloor.compile.spi.supplier.source.SupplierSourceContext;
import net.officefloor.compile.spi.supplier.source.impl.AbstractSupplierSource;
import net.officefloor.frame.api.build.None;
import net.officefloor.frame.api.managedobject.ManagedObject;
import net.officefloor.frame.api.managedobject.source.impl.AbstractManagedObjectSource;

import java.util.concurrent.LinkedBlockingDeque;

// START SNIPPET: tutorial
public class MessagingSupplierSource extends AbstractSupplierSource {

	private final LinkedBlockingDeque<String> messageQueue = new LinkedBlockingDeque<>();

	@Override
	protected void loadSpecification(SpecificationContext context) {
	}

	@Override
	public void supply(SupplierSourceContext context) throws Exception {

		// Register MessagePublisher — captures the shared queue via closure
		context.addManagedObjectSource(null, MessagePublisher.class,
				new AbstractManagedObjectSource<None, None>() {
					@Override
					protected void loadSpecification(SpecificationContext context) {}

					@Override
					protected void loadMetaData(MetaDataContext<None, None> context) throws Exception {
						context.setObjectClass(MessagePublisher.class);
					}

					@Override
					protected ManagedObject getManagedObject() throws Throwable {
						return () -> new MessagePublisher(messageQueue);
					}
				});

		// Register MessageSubscriber — captures the same shared queue via closure
		context.addManagedObjectSource(null, MessageSubscriber.class,
				new AbstractManagedObjectSource<None, None>() {
					@Override
					protected void loadSpecification(SpecificationContext context) {}

					@Override
					protected void loadMetaData(MetaDataContext<None, None> context) throws Exception {
						context.setObjectClass(MessageSubscriber.class);
					}

					@Override
					protected ManagedObject getManagedObject() throws Throwable {
						return () -> new MessageSubscriber(messageQueue);
					}
				});
	}

	@Override
	public void terminate() {
		messageQueue.clear();
	}
}
// END SNIPPET: tutorial
