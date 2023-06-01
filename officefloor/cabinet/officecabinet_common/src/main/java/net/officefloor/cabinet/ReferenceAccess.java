package net.officefloor.cabinet;

import java.util.function.Supplier;

/**
 * Provides access to {@link OneToOne} and {@link OneToMany}.
 * 
 * @author Daniel Sagenschneider
 */
public class ReferenceAccess {

	/**
	 * Obtains the current related {@link Document} without triggering a retrieve.
	 * Therefore, this may return <code>null</code> when there is actually a related
	 * {@link Document}.
	 * 
	 * @param <D>      Retrieved {@link Document} type.
	 * @param oneToOne {@link OneToOne}.
	 * @return Related {@link Document} or <code>null</code>.
	 */
	public static <D> D nonRetrievedGet(OneToOne<D> oneToOne) {
		return oneToOne.nonRetrievedGet();
	}

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
