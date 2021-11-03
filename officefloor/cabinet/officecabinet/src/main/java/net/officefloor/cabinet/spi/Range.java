package net.officefloor.cabinet.spi;

/**
 * Range of results.
 * 
 * @author Daniel Sagenschneider
 */
public class Range<D> {

	public static enum Direction {
		Ascending, Descending
	}

	private final String fieldName;

	private final Direction direction;

	private final int limit;

	private final D startAfterDocument;

	public Range(String fieldName, Direction direction, int limit) {
		this(fieldName, direction, limit, null);
	}

	public Range(String fieldName, Direction direction, int limit, D startAfterDocument) {
		this.fieldName = fieldName;
		this.direction = direction;
		this.limit = limit;
		this.startAfterDocument = startAfterDocument;
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

	public D startAfterDocument() {
		return this.startAfterDocument;
	}
}