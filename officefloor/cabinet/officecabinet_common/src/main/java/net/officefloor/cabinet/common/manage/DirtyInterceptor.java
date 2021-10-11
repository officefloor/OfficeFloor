package net.officefloor.cabinet.common.manage;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.concurrent.Callable;

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
	 * {@link Field} name to obtain the {@link ManagedDocumentState} from the
	 * {@link ManagedDocument}.
	 */
	public static final String $$OfficeFloor$$_managedDocumentState = "$$OfficeFloor$$_managedDocumentState";

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
		instance.get$$OfficeFloor$$_managedDocumentState().isDirty = true;
		return superCall.call();
	}
}