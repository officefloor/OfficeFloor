package net.officefloor.cabinet.common.adapt;

import net.officefloor.cabinet.spi.Range;
import net.officefloor.cabinet.spi.Range.Direction;

/**
 * Internal {@link Range}.
 * 
 * @author Daniel Sagenschneider
 */
public class InternalRange {

	private final String fieldName;

	private final Direction direction;

	private final int limit;

	private final String nextDocumentBundleToken;

	public InternalRange(String fieldName, Direction direction, int limit, String nextDocumentBundleToken) {
		this.fieldName = fieldName;
		this.direction = direction;
		this.limit = limit;
		this.nextDocumentBundleToken = nextDocumentBundleToken;
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

	public String getNextDocumentBundleToken() {
		return this.nextDocumentBundleToken;
	}

}