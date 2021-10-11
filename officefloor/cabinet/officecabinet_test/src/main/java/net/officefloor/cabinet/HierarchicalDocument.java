package net.officefloor.cabinet;

import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.concurrent.atomic.AtomicInteger;

import lombok.Data;

/**
 * {@link Document} to ensure storing embedded child objects.
 * 
 * @author Daniel Sagenschneider
 */
@Data
@Document
public class HierarchicalDocument {

	private static final AtomicInteger nextQueryValue = new AtomicInteger(0);

	@FunctionalInterface
	private static interface NotNullEquals<T> {
		void assertEquals(T a, T b, String messagePrefix);
	}

	private static <T> void assertNullableEquals(T a, T b, NotNullEquals<T> compareNotNull, String messagePrefix) {
		if (a == null) {
			assertNull(b, messagePrefix + " should be null");
		} else {
			compareNotNull.assertEquals(a, b, messagePrefix);
		}
	}

	@Key
	private String key;
	
	private int queryValue = nextQueryValue.incrementAndGet();

	private AttributeTypesDocument child;

	private AttributeTypesDocument childNull = null;

//	private List<Child> children;
//
//	private Set<Child> uniqueChildren;

	public HierarchicalDocument() {
	}

	public HierarchicalDocument(int offset) {
		this.child = new AttributeTypesDocument(offset);
//		this.children = Arrays.asList(new Child(++offset));
//		this.uniqueChildren = new HashSet<>(Arrays.asList(new Child(++offset)));
	}

	public void assertDocumentEquals(HierarchicalDocument document) {
		assertNullableEquals(this.child, document.child, AttributeTypesDocument::assertDocumentEquals, "child");
	}
}