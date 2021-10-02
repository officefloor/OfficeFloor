package net.officefloor.cabinet.spi;

/**
 * Query.
 * 
 * @author Daniel Sagenschneider
 */
public class Query {

	public static class QueryField {

		public final String fieldName;

		public final Object fieldValue;

		public QueryField(String fieldName, Object fieldValue) {
			this.fieldName = fieldName;
			this.fieldValue = fieldValue;
		}
	}

	private final QueryField[] fields;

	public Query(QueryField... fields) {
		this.fields = fields;
	}

	public QueryField[] getFields() {
		return this.fields;
	}
}
