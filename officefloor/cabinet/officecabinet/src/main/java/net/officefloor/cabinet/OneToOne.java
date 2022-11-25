package net.officefloor.cabinet;

/**
 * One to one relationship between {@link Document} instances.
 * 
 * @author Daniel Sagenschneider
 */
public class OneToOne<T> {

	/**
	 * Related {@link Document}.
	 */
	private T relatedDocument;

	/**
	 * Obtains the related {@link Document}.
	 * 
	 * @return Related {@link Document}.
	 */
	public T get() {
		return this.relatedDocument;
	}

	/**
	 * Specifies the related {@link Document}.
	 * 
	 * @param relatedDocument Related {@link Document}.
	 */
	public void set(T relatedDocument) {
		this.relatedDocument = relatedDocument;
	}
}