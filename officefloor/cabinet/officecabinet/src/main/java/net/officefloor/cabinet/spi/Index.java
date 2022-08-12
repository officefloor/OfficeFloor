package net.officefloor.cabinet.spi;

import java.util.Objects;

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

	private final String sortFieldName;

	public Index(IndexField... fields) {
		this(null, fields);
	}

	public Index(String sortFieldName, IndexField... fields) {
		this.fields = fields;
		this.sortFieldName = sortFieldName;
	}

	public IndexField[] getFields() {
		return this.fields;
	}

	public String getSortFieldName() {
		return this.sortFieldName;
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
		if (!Objects.equals(this.sortFieldName, that.sortFieldName)) {
			return false;
		}
		return true;
	}

}
