package net.officefloor.cabinet.common.adapt;

import net.officefloor.cabinet.spi.Range;
import net.officefloor.cabinet.spi.Range.Direction;

/**
 * Internal {@link Range}.
 * 
 * @author Daniel Sagenschneider
 */
public class InternalRange<R> {

	private final String fieldName;

	private final Direction direction;

	private final int limit;

	private final StartAfterDocumentValueGetter startAfterDocumentValueGetter;

	public InternalRange(String fieldName, Direction direction, int limit,
			StartAfterDocumentValueGetter startAfterDocumentValueGetter) {
		this.fieldName = fieldName;
		this.direction = direction;
		this.limit = limit;
		this.startAfterDocumentValueGetter = startAfterDocumentValueGetter;
	}

	public String getFieldName() {
		return fieldName;
	}

	public Direction getDirection() {
		return direction;
	}

	public int getLimit() {
		return limit;
	}

	public StartAfterDocumentValueGetter getStartAfterDocumentValueGetter() {
		return startAfterDocumentValueGetter;
	}

}