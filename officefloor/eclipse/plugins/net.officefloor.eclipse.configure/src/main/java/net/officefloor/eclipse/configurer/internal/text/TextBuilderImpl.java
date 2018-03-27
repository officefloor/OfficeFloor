/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2018 Daniel Sagenschneider
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package net.officefloor.eclipse.configurer.internal.text;

import java.util.function.Function;

import javafx.beans.value.ObservableStringValue;
import net.officefloor.eclipse.configurer.TextBuilder;
import net.officefloor.eclipse.configurer.ValueLoader;
import net.officefloor.eclipse.configurer.ValueValidator;

/**
 * {@link TextBuilder} implementation.
 * 
 * @author Daniel Sagenschneider
 */
public class TextBuilderImpl<M> implements TextBuilder<M> {

	/**
	 * Label.
	 */
	private final String label;

	/**
	 * {@link ValueLoader}.
	 */
	private final ValueLoader<M, String> textLoader;

	
	public TextBuilderImpl(String label, ValueLoader<M, String> textLoader) {
		super();
		this.label = label;
		this.textLoader = textLoader;
	}

	/*
	 * ================ TextBuilder ===================
	 */

	@Override
	public TextBuilder<M> init(Function<M, String> getInitialValue) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public TextBuilder<M> validate(ValueValidator<String> validator) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ObservableStringValue getValue() {
		// TODO Auto-generated method stub
		return null;
	}

}
