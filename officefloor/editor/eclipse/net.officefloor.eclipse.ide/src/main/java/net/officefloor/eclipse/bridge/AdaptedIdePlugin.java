package net.officefloor.eclipse.bridge;

import java.io.Closeable;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.ui.plugin.AbstractUIPlugin;

import net.officefloor.gef.bridge.EnvironmentBridge.PreferenceEvent;
import net.officefloor.gef.bridge.EnvironmentBridge.PreferenceListener;

/**
 * Adapted IDE {@link AbstractUIPlugin}.
 * 
 * @author Daniel Sagenschneider
 */
public class AdaptedIdePlugin extends AbstractUIPlugin {

	/**
	 * Default instance.
	 */
	private static AdaptedIdePlugin INSTANCE;

	/**
	 * Obtains the default {@link AdaptedIdePlugin} instances.
	 * 
	 * @return Default {@link AdaptedIdePlugin} instances.
	 */
	public static AdaptedIdePlugin getDefault() {
		return INSTANCE;
	}

	/**
	 * Instantiate, capturing instance.
	 */
	public AdaptedIdePlugin() {
		INSTANCE = this;
	}

	/**
	 * Provide means to obtain {@link IPreferenceStore}.
	 * 
	 * @return {@link IPreferenceStore}.
	 */
	public IPreferenceStore getPreferenceStore() {
		return super.getPreferenceStore();
	}

	/**
	 * Adds a {@link PreferenceListener}.
	 * 
	 * @param listener {@link PreferenceListener}.
	 * @return {@link Closeable} to stop listening.
	 */
	public Closeable addPreferenceListener(PreferenceListener listener) {

		// Handle change listener
		IPropertyChangeListener changeListener;

		// Listen to preference changes
		IPreferenceStore preferences = AdaptedIdePlugin.getDefault().getPreferenceStore();
		if (preferences != null) {

			// Create and register listening to preference change
			changeListener = (event) -> listener.changedPreference(new PreferenceEvent(event.getProperty()));
			preferences.addPropertyChangeListener(changeListener);

		} else {
			changeListener = null;
		}

		// Provide means to remove listening
		return () -> {
			if (changeListener != null) {
				preferences.removePropertyChangeListener(changeListener);
			}
		};
	}

}