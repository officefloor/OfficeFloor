/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2019 Daniel Sagenschneider
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package net.officefloor.polyglot.scala;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * {@link Collection} types.
 * 
 * @author Daniel Sagenschneider
 */
public class CollectionTypes {

	private final List<Integer> list;

	private final Set<Character> set;

	private final Map<String, JavaObject> map;

	private final Collection<Float> collection;

	public CollectionTypes(List<Integer> list, Set<Character> set, Map<String, JavaObject> map,
			Collection<Float> collection) {
		this.list = list;
		this.set = set;
		this.map = map;
		this.collection = collection;
	}

	public List<Integer> getList() {
		return list;
	}

	public Set<Character> getSet() {
		return set;
	}

	public Map<String, JavaObject> getMap() {
		return map;
	}

	public Collection<Float> getCollection() {
		return collection;
	}

}