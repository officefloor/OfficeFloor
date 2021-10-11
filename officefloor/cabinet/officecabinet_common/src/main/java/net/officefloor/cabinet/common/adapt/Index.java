package net.officefloor.cabinet.common.adapt;

/**
 * Index.
 * 
 * @author Daniel Sagenschneider
 */
public class Index {

	public static class IndexField {

		public final String fieldName;

		public IndexField(String fieldName) {
			this.fieldName = fieldName;
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
