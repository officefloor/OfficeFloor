package net.officefloor.cabinet;

import java.util.function.Supplier;

/**
 * One to one relationship between {@link Document} instances.
 * 
 * @author Daniel Sagenschneider
 */
public class OneToOne<D> {

	/**
	 * Type of related {@link Document}.
	 */
	private final Class<D> documentType;

	/**
	 * Related {@link Document}.
	 */
	private D relatedDocument = null;

	/**
	 * Indicates if attempted to retrieve related {@link Document} from data store.
	 * Necessary should there be no (<code>null</code>) related {@link Document}.
	 */
	private boolean isRetrieved = false;

	/**
	 * Loads related {@link Document} from data store.
	 */
	private Supplier<D> retriever = null;

	/**
	 * Instantiate.
	 * 
	 * @param documentType Type of related {@link Document}.
	 */
	public OneToOne(Class<D> documentType) {
		this.documentType = documentType;
	}

	/**
	 * Obtains the related {@link Document}.
	 * 
	 * @return Related {@link Document}. May be <code>null</code> if no related
	 *         {@link Document}.
	 */
	public D get() {

		// Ensure retrieve related document
		this.ensureRetrieved();

		// Return the related document
		return this.relatedDocument;
	}

	/**
	 * Specifies the related {@link Document}.
	 * 
	 * @param relatedDocument Related {@link Document}.
	 */
	public void set(D relatedDocument) {

		// As set, no longer need to retrieve
		this.isRetrieved = true;

		// Specify the related document
		this.relatedDocument = relatedDocument;
	}

	/**
	 * Obtains the type of related {@link Document}.
	 * 
	 * @return Type of related {@link Document}.
	 */
	public Class<D> documentType() {
		return this.documentType;
	}

	/**
	 * <p>
	 * Obtains the current related {@link Document} without triggering a retrieve.
	 * Therefore, this may return <code>null</code> when there is actually a related
	 * {@link Document}.
	 * <p>
	 * This <strong>MUST</strong> never be called by application code. This is only
	 * exposed for the Cabinet framework to use.
	 * 
	 * @return Non-retrieved related {@link Document} or <code>null</code>.
	 */
	D nonRetrievedGet() {
		return this.relatedDocument;
	}

	/**
	 * <p>
	 * Specifies the {@link Supplier} to retrieve related {@link Document} from the
	 * data store.
	 * <p>
	 * This <strong>MUST</strong> never be called by application code. This is only
	 * exposed for the Cabinet framework to use.
	 * 
	 * @param retriever {@link Supplier} to retrieve related {@link Document} from
	 *                  the data store.
	 */
	void setRetriever(Supplier<D> retriever) {

		// Invalid to set retriever after retrieving value
		if (this.retriever != null) {
			throw new IllegalStateException("Retriever already specified");
		}
		if (this.isRetrieved) {
			throw new IllegalStateException("Already retrieved releated " + Document.class.getSimpleName());
		}

		// Specifies the retriever
		this.retriever = retriever;
	}

	/**
	 * Ensures the releated {@link Document} has been retrieved from data store.
	 */
	private void ensureRetrieved() {

		// Lazy retrieve the related document
		if (!this.isRetrieved) {

			// Attempt to retrieve related document
			if (this.retriever != null) {
				this.relatedDocument = this.retriever.get();
			}

			// Attempt made
			this.isRetrieved = true;
		}
	}

}