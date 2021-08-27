package net.officefloor.cabinet;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import lombok.Data;

/**
 * {@link Document} to ensure storing embedded child objects.
 * 
 * @author Daniel Sagenschneider
 */
@Data
@Document
public class HierarchicalDocument {

	@Data
	public static class Child {
		private String name;

		public Child(int offset) {
			this.name = String.valueOf(offset);
		}

		public Child() {
		}

		public void assertChildEquals(Child child, String messagePrefix) {
			assertEquals(this.name, child.name, "child.name");
		}
	}

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

	private Child child;

	private Child childNull = null;

	private List<Child> children;

	private Set<Child> uniqueChildren;

	public HierarchicalDocument() {
	}

	public HierarchicalDocument(int offset) {
		this.child = new Child(offset);
		this.children = Arrays.asList(new Child(++offset));
		this.uniqueChildren = new HashSet<>(Arrays.asList(new Child(++offset)));
	}

	public void assertDocumentEquals(HierarchicalDocument document) {
		assertNullableEquals(this.child, document.child, Child::assertChildEquals, "child");
	}
}