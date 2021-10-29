package net.officefloor.cabinet.spi;

/**
 * Index.
 * 
 * @author Daniel Sagenschneider
 */
public class Index {

	public static Index of(String... fieldNames) {
		IndexField[] fields = new IndexField[fieldNames.length];
		for (int i = 0; i < fields.length; i++) {
			fields[i] = new IndexField(fieldNames[i]);
		}
		return new Index(fields);
	}

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

	/*
	 * ================== Object ==================
	 */

	@Override
	public int hashCode() {
		int hashCode = 0;
		for (IndexField field : this.fields) {
			hashCode += field.fieldName.hashCode();
		}
		return hashCode;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (!(obj instanceof Index)) {
			return false;
		}
		Index that = (Index) obj;
		if (this.fields.length != that.fields.length) {
			return false;
		}
		for (int i = 0; i < this.fields.length; i++) {
			if (!this.fields[i].fieldName.equals(that.fields[i].fieldName)) {
				return false;
			}
		}
		return true;
	}

}
