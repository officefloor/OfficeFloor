package net.officefloor.polyglot.test;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import lombok.Value;

/**
 * {@link Collection} types.
 * 
 * @author Daniel Sagenschneider
 */
@Value
public class CollectionTypes {

	private final List<Integer> list;

	private final Set<Character> set;

	private final Map<String, JavaObject> map;

}