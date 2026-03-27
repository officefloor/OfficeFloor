package net.officefloor.eclipse.ide;

import org.eclipse.gef.fx.swt.canvas.FXCanvasEx;
import org.eclipse.gef.fx.swt.canvas.IFXCanvasFactory;
import org.eclipse.gef.mvc.fx.ui.MvcFxUiModule;
import org.eclipse.swt.widgets.Composite;

import javafx.embed.swt.FXCanvas;

/**
 * {@link MvcFxUiModule} with fixes for:
 * <ul>
 * <li>https://bugs.eclipse.org/bugs/show_bug.cgi?id=546011 : at moment having
 * to add <code>-Dswt.autoScale=false</code> to avoid SWT scaling on Linux</li>
 * </ul>
 * 
 * @author Daniel Sagenschneider
 */
public class AdaptedMvcFxUiModule extends MvcFxUiModule {

	/*
	 * ================= MvcFxUiModule ====================
	 */

	@Override
	protected void bindFXCanvasFactory() {
		binder().bind(IFXCanvasFactory.class).toInstance(new IFXCanvasFactory() {
			@Override
			public FXCanvas createCanvas(Composite parent, int style) {

				// Provide overridden implementation
				return new AdaptedFxCanvas(parent, style);
			}
		});
	}

	/**
	 * Provides bug fixes for {@link FXCanvas}.
	 */
	public static class AdaptedFxCanvas extends FXCanvasEx {

		/**
		 * Required constructor.
		 * 
		 * @param parent Parent.
		 * @param style  Style.
		 */
		public AdaptedFxCanvas(Composite parent, int style) {
			super(parent, style);
		}
		
		// TODO override getScaleFactor() for linux determination
	}
}
