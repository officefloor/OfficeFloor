/*-
 * #%L
 * [bundle] OfficeFloor Eclipse IDE
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

package net.officefloor.eclipse.ide.swt;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.e4.ui.css.swt.theme.ITheme;
import org.eclipse.e4.ui.css.swt.theme.IThemeEngine;
import org.eclipse.e4.ui.css.swt.theme.IThemeManager;
import org.eclipse.jface.fieldassist.ControlDecoration;
import org.eclipse.jface.fieldassist.FieldDecoration;
import org.eclipse.jface.fieldassist.FieldDecorationRegistry;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Widget;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceReference;
import org.w3c.dom.css.CSSStyleDeclaration;
import org.w3c.dom.css.CSSValue;
import org.w3c.dom.css.RGBColor;

import javafx.scene.paint.Color;

/**
 * Utility methods for the SWT.
 * 
 * @author Daniel Sagenschneider
 */
@SuppressWarnings("restriction")
public class SwtUtil {

	/**
	 * Name of the background.
	 */
	public static final String BACKGROUND_COLOR = "background-color";

	/**
	 * Provides auto hiding of the scroll bars.
	 * 
	 * @param text {@link Text}.
	 */
	public static void autoHideScrollbars(Text text) {
		Listener scrollBarListener = (Event event) -> {
			Rectangle r1 = text.getClientArea();
			Rectangle r2 = text.computeTrim(r1.x, r1.y, r1.width, r1.height);
			Point p = text.computeSize(SWT.DEFAULT, SWT.DEFAULT, true);
			text.getHorizontalBar().setVisible(r2.width <= p.x);
			text.getVerticalBar().setVisible(r2.height <= p.y);
			if (event.type == SWT.Modify) {
				text.getParent().layout(true);
				text.showSelection();
			}
		};
		text.addListener(SWT.Resize, scrollBarListener);
		text.addListener(SWT.Modify, scrollBarListener);
	}

	/**
	 * Provides auto hiding of the scroll bars.
	 * 
	 * @param text {@link StyledText}.
	 */
	public static void autoHideScrollbars(StyledText text) {
		Listener scrollBarListener = (Event event) -> {
			Rectangle r1 = text.getClientArea();
			Rectangle r2 = text.computeTrim(r1.x, r1.y, r1.width, r1.height);
			Point p = text.computeSize(SWT.DEFAULT, SWT.DEFAULT, true);
			text.getHorizontalBar().setVisible(r2.width <= p.x);
			text.getVerticalBar().setVisible(r2.height <= p.y);
			if (event.type == SWT.Modify) {
				text.getParent().layout(true);
				text.showSelection();
			}
		};
		text.addListener(SWT.Resize, scrollBarListener);
		text.addListener(SWT.Modify, scrollBarListener);
	}

	/**
	 * Creates error decoration on the {@link Control}.
	 * 
	 * @param control {@link Control} to have error decoration.
	 * @param style   {@link SWT} style.
	 * @return {@link ControlDecoration}.
	 */
	public static ControlDecoration errorDecoration(Control control, int style) {
		ControlDecoration decorator = new ControlDecoration(control, style);
		FieldDecoration fieldDecoration = FieldDecorationRegistry.getDefault()
				.getFieldDecoration(FieldDecorationRegistry.DEC_ERROR);
		Image img = fieldDecoration.getImage();
		decorator.setImage(img);
		decorator.hide();
		return decorator;
	}

	/**
	 * Loads the {@link ITheme} {@link Color} instances.
	 * 
	 * @param uiObject  UI object to extract {@link Color} instances.
	 * @param isDispose Indicates to dispose the {@link Widget} once complete.
	 * @return {@link Map} of CSS property to {@link Color}.
	 */
	public static Map<String, Color> loadThemeColours(Object uiObject, boolean isDispose) {

		// Obtain the widget
		Widget widget;
		if (uiObject instanceof Widget) {
			widget = (Widget) uiObject;
		} else if (uiObject instanceof Viewer) {
			widget = ((Viewer) uiObject).getControl();
		} else {
			throw new IllegalStateException("Unknown UI object type " + uiObject.getClass().getName());
		}

		// Obtain the theme details
		Bundle bundle = FrameworkUtil.getBundle(SwtUtil.class);
		BundleContext bundleContext = bundle.getBundleContext();
		ServiceReference<IThemeManager> themeManagerReference = bundleContext.getServiceReference(IThemeManager.class);

		// Obtain the theme manager
		IThemeManager themeManager = bundle.getBundleContext().getService(themeManagerReference);
		IThemeEngine themeEngine = themeManager.getEngineForDisplay(widget.getDisplay());

		// Style the widget (so has CSS loaded)
		themeEngine.applyStyles(widget, true);

		// Extract the colours from the widget
		Map<String, Color> colours = new HashMap<>();
		extractColours(widget, "", colours, themeEngine);

		// Determine if dipose widget (now that have colours)
		if (isDispose) {
			widget.dispose();
		}

		// Ensure a background colour
		Color backgroundColour = colours.get(BACKGROUND_COLOR);
		if (backgroundColour == null) {
			org.eclipse.swt.graphics.Color swtBackgroundColor = widget.getDisplay()
					.getSystemColor(SWT.COLOR_WIDGET_BACKGROUND);
			backgroundColour = Color.color(swtBackgroundColor.getRed() / 255, swtBackgroundColor.getGreen() / 255,
					swtBackgroundColor.getBlue() / 255, swtBackgroundColor.getAlpha() / 255);
		}

		// Return the colours
		return colours;
	}

	/**
	 * Extracts the colours from the {@link ITheme}.
	 * 
	 * @param widget      {@link Widget} to inspect.
	 * @param prefix      Prefix on the return colour.
	 * @param colours     {@link Map} to load the {@link Color}.
	 * @param themeEngine {@link IThemeEngine}.
	 */
	private static void extractColours(Widget widget, String prefix, Map<String, Color> colours,
			IThemeEngine themeEngine) {

		// Extract the colours from the widget
		CSSStyleDeclaration style = themeEngine.getStyle(widget);
		if (style != null) {
			for (int i = 0; i < style.getLength(); i++) {
				String name = style.item(i);
				CSSValue value = style.getPropertyCSSValue(name);
				if (value instanceof RGBColor) {
					RGBColor valueColour = (RGBColor) value;
					double red = Double.parseDouble(valueColour.getRed().getCssText()) / 255;
					double green = Double.parseDouble(valueColour.getGreen().getCssText()) / 255;
					double blue = Double.parseDouble(valueColour.getBlue().getCssText()) / 255;
					Color colour = Color.color(red, green, blue);
					colours.put(prefix + name, colour);
				}
			}
		}

		// Extract colours from possible children
		if (widget instanceof Composite) {
			Composite composite = (Composite) widget;
			for (Control child : composite.getChildren()) {
				extractColours(child, prefix + widget.getClass().getSimpleName() + ".", colours, themeEngine);
			}
		}
	}

	/**
	 * All access via static methods.
	 */
	private SwtUtil() {
	}
}
