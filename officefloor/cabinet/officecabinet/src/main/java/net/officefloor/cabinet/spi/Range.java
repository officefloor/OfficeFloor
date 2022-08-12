package net.officefloor.cabinet.spi;

/**
 * Range of results.
 * 
 * @author Daniel Sagenschneider
 */
public class Range {

	public static enum Direction {
		Ascending, Descending
	}

	private final String fieldName;

	private final Direction direction;

	private final int limit;

	private final String nextDocumentBundleToken;

	public Range(String fieldName, Direction direction) {
		this(fieldName, direction, -1, null);
	}

	public Range(String fieldName, Direction direction, int limit) {
		this(fieldName, direction, limit, null);
	}

	public Range(String fieldName, Direction direction, int limit, String nextDocumentBundleToken) {
		this.fieldName = fieldName;
		this.direction = direction;
		this.limit = limit;
		this.nextDocumentBundleToken = nextDocumentBundleToken;
	}

	public String getFieldName() {
		return this.fieldName;
	}

	public Direction getDirection() {
		return this.direction;
	}

	public int getLimit() {
		return this.limit;
	}

	public String getNextDocumentBundleToken() {
		return this.nextDocumentBundleToken;
	}
}