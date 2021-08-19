package net.officefloor.cabinet.common;

import java.lang.reflect.Method;
import java.util.concurrent.Callable;

import net.bytebuddy.implementation.bind.annotation.FieldProxy;
import net.bytebuddy.implementation.bind.annotation.SuperCall;
import net.bytebuddy.implementation.bind.annotation.This;
import net.officefloor.cabinet.Document;

/**
 * Intercepts mutators {@link Method} invocations to flag the {@link Document}
 * dirty.
 * 
 * @author Daniel Sagenschneider
 */
public class DirtyInterceptor {

	/**
	 * {@link Method} name to obtain the {@link ManagedDocumentState} from the
	 * {@link ManagedDocument}.
	 */
	public static final String $$OfficeFloor$$_getManagedDocumentState = "$$OfficeFloor$$_getManagedDocumentState";

	public static interface ManagedDocumentField<T> {

		T getManagedDocumentState();

		void setManagedDocumentState(T state);
	}

	public static class DefaultConstructor {
		public static void constructor(
				@FieldProxy($$OfficeFloor$$_getManagedDocumentState) ManagedDocumentField<ManagedDocumentState> field) {
			field.setManagedDocumentState(new ManagedDocumentState());
		}
	}

	public static class ManagedDocumentImpl {
		public static ManagedDocumentState getManagedDocumentState(
				@FieldProxy($$OfficeFloor$$_getManagedDocumentState) ManagedDocumentField<ManagedDocumentState> field) {
			return field.getManagedDocumentState();
		}
	}

	public static class FlagDirty {

		/**
		 * Intercepts the mutator to flag the {@link Document} dirty.
		 * 
		 * @param <R>       Return type.
		 * @param instance  {@link Document} instance.
		 * @param superCall Super mutator {@link Method}.
		 * @return Result of super method.
		 * @throws Exception If fails to invoke super {@link Method}.
		 */
		public static <R> R interceptAndFlagDirty(@This ManagedDocument instance, @SuperCall Callable<R> superCall)
				throws Exception {
			instance.$$OfficeFloor$$_getManagedDocumentState().isDirty = true;
			return superCall.call();
		}
	}
}