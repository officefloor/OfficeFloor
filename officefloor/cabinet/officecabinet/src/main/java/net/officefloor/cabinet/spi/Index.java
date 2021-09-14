package net.officefloor.cabinet.spi;

/**
 * Index.
 * 
 * @author Daniel Sagenschneider
 */
public class Index {

	public static class IndexField {

		public final String fieldName;

		public final Object fieldValue;

		public IndexField(String fieldName, Object fieldValue) {
			this.fieldName = fieldName;
			this.fieldValue = fieldValue;
		}
	}

	private final IndexField[] fields;

	public Index(IndexField... fields) {
		this.fields = fields;
	}

	public IndexField[] getFields() {
		return this.fields;
	}
}
