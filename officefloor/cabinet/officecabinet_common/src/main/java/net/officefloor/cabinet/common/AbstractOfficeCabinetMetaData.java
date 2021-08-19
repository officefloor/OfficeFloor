package net.officefloor.cabinet.common;

import java.lang.reflect.Modifier;

import net.bytebuddy.ByteBuddy;
import net.bytebuddy.implementation.MethodDelegation;
import net.bytebuddy.implementation.SuperMethodCall;
import net.bytebuddy.implementation.bind.annotation.FieldProxy;
import net.bytebuddy.implementation.bind.annotation.TargetMethodAnnotationDrivenBinder.ParameterBinder;
import net.bytebuddy.matcher.ElementMatchers;
import net.bytebuddy.matcher.MethodParametersMatcher;
import net.officefloor.cabinet.Document;
import net.officefloor.cabinet.OfficeCabinet;
import net.officefloor.cabinet.common.DirtyInterceptor.ManagedDocumentField;

/**
 * Meta-data for the {@link OfficeCabinet}.
 * 
 * @author Daniel Sagenschneider
 */
public class AbstractOfficeCabinetMetaData<D> {

	/**
	 * {@link Document} type.
	 */
	public final Class<D> documentType;

	/**
	 * {@link ManagedDocument} type.
	 */
	public final Class<? extends D> managedDocumentType;

	/**
	 * {@link DocumentKey}.
	 */
	public final DocumentKey<D> documentKey;

	/**
	 * Instantiate the meta-data.
	 * 
	 * @param documentType {@link Document} type.
	 * @throws Exception If fails to create abstract meta-data.
	 */
	public AbstractOfficeCabinetMetaData(Class<D> documentType) throws Exception {
		this.documentType = documentType;

		// Obtain the document key
		this.documentKey = CabinetUtil.getDocumentKey(documentType);

		// Implement the managed document type
		ParameterBinder<FieldProxy> stateField = FieldProxy.Binder.install(ManagedDocumentField.class);
		this.managedDocumentType = new ByteBuddy().subclass(this.documentType)

				// Intercept setting methods to flag dirty
				.method(new MethodParametersMatcher<>((parameterList) -> parameterList.size() > 0))
				.intercept(MethodDelegation.to(DirtyInterceptor.FlagDirty.class))

				// Ignore object and lombok methods
				.ignoreAlso(ElementMatchers.named("equals")).ignoreAlso(ElementMatchers.named("canEqual"))

				// Field maintaining dirty state
				.defineField(DirtyInterceptor.$$OfficeFloor$$_getManagedDocumentState, ManagedDocumentState.class,
						Modifier.PRIVATE)

				// Constructor to default the dirty state
				.constructor(ElementMatchers.isDefaultConstructor())
				.intercept(SuperMethodCall.INSTANCE.andThen(MethodDelegation.withDefaultConfiguration()
						.withBinders(stateField).to(DirtyInterceptor.DefaultConstructor.class)))

				// Interface for managing the document
				.implement(ManagedDocument.class)

				.defineMethod(DirtyInterceptor.$$OfficeFloor$$_getManagedDocumentState, ManagedDocumentState.class,
						Modifier.PUBLIC)
				.intercept(MethodDelegation.withDefaultConfiguration().withBinders(stateField)
						.to(DirtyInterceptor.ManagedDocumentImpl.class))

				.make().load(this.documentType.getClassLoader()).getLoaded();
	}

}