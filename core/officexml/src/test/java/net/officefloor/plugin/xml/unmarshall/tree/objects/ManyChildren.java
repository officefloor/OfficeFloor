/*-
 * #%L
 * OfficeXml
 * %%
 * Copyright (C) 2005 - 2020 Daniel Sagenschneider
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

package net.officefloor.plugin.xml.unmarshall.tree.objects;

import java.util.LinkedList;
import java.util.List;

/**
 * Many children target object to be loaded.
 * 
 * @author Daniel Sagenschneider
 */
public class ManyChildren {
	
	protected String name;
	
	public String getName() {
		return this.name;
	}
	
	public void setName(String name) {
		this.name = name;
	}

	protected List<ManyChildren> children = new LinkedList<ManyChildren>();

	public ManyChildren[] getChildren() {
		return children.toArray(new ManyChildren[0]);
	}

	public void addChild(ManyChildren child) {
		this.children.add(child);
	}
}
