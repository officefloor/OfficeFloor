package net.officefloor.cabinet;

import java.util.function.Supplier;

/**
 * Provides access to {@link OneToOne} and {@link OneToMany}.
 * 
 * @author Daniel Sagenschneider
 */
public class ReferenceAccess {

	/**
	 * Specifies the {@link Supplier} to retrieve referenced {@link Document} from
	 * data store.
	 * 
	 * @param <D>       Retrieved {@link Document} type.
	 * @param oneToOne  {@link OneToOne}.
	 * @param retriever {@link Supplier} to retrieve referenced {@link Document}
	 *                  from data store.
	 */
	public static <D> void setRetriever(OneToOne<D> oneToOne, Supplier<D> retriever) {
		oneToOne.setRetriever(retriever);
	}

	/**
	 * All access via static methods.
	 */
	private ReferenceAccess() {
	}
}
