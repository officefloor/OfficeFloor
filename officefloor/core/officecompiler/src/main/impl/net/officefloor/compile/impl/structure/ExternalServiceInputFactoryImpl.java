package net.officefloor.compile.impl.structure;

import net.officefloor.compile.internal.structure.ExternalServiceInputNode;
import net.officefloor.compile.spi.officefloor.DeployedOfficeInput;
import net.officefloor.compile.spi.officefloor.ExternalServiceInput;
import net.officefloor.compile.internal.structure.ExternalServiceInputFactory;
import net.officefloor.compile.spi.officefloor.OfficeFloorManagedObjectFlow;
import net.officefloor.compile.spi.officefloor.OfficeFloorManagedObjectSource;
import net.officefloor.frame.api.build.Indexed;
import net.officefloor.frame.api.build.None;
import net.officefloor.frame.api.function.FlowCallback;
import net.officefloor.frame.api.function.ManagedFunction;
import net.officefloor.frame.api.function.ManagedFunctionContext;
import net.officefloor.frame.api.function.ManagedFunctionFactory;
import net.officefloor.frame.api.manage.ProcessManager;
import net.officefloor.frame.api.managedobject.AsynchronousContext;
import net.officefloor.frame.api.managedobject.AsynchronousManagedObject;
import net.officefloor.frame.api.managedobject.ContextAwareManagedObject;
import net.officefloor.frame.api.managedobject.CoordinatingManagedObject;
import net.officefloor.frame.api.managedobject.InputManagedObject;
import net.officefloor.frame.api.managedobject.ManagedObject;
import net.officefloor.frame.api.managedobject.ManagedObjectContext;
import net.officefloor.frame.api.managedobject.ObjectRegistry;
import net.officefloor.frame.api.managedobject.recycle.CleanupEscalation;
import net.officefloor.frame.api.managedobject.recycle.RecycleManagedObjectParameter;
import net.officefloor.frame.api.managedobject.source.ManagedObjectExecuteContext;
import net.officefloor.frame.api.managedobject.source.impl.AbstractManagedObjectSource;
import net.officefloor.frame.api.source.PrivateSource;
import net.officefloor.frame.impl.execute.service.SafeManagedObjectService;

import java.util.LinkedList;
import java.util.List;

/**
 * {@link ExternalServiceInputFactory} implementation.
 *
 * @param <O> Object Type.
 * @param <M> {@link ManagedObject} Type.
 */
@PrivateSource
public class ExternalServiceInputFactoryImpl<O, M extends InputManagedObject>
        extends AbstractManagedObjectSource<None, Indexed>
        implements ExternalServiceInputFactory<O, M>, ManagedFunction<None, None> {

    /**
     * {@link ExternalServiceInput} object type.
     */
    private final Class<O> objectType;

    /**
     * {@link ManagedObject} type.
     */
    private final Class<M> managedObjectType;

    /**
     * {@link OfficeFloorManagedObjectSource} for this instance.
     */
    private OfficeFloorManagedObjectSource managedObjectSource;

    /**
     * {@link SafeManagedObjectService}.
     */
    private SafeManagedObjectService<Indexed> servicer;

    /**
     * Names of the flows.
     */
    private List<String> flows = new LinkedList<>();

    /**
     * Instantiate.
     *
     * @param objectType        {@link ExternalServiceInput} object type.
     * @param managedObjectType {@link ManagedObject} type.
     */
    public ExternalServiceInputFactoryImpl(Class<O> objectType, Class<M> managedObjectType) {
        this.objectType = objectType;
        this.managedObjectType = managedObjectType;
    }

    /**
     * Obtains the {@link ManagedObject} type.
     *
     * @return {@link ManagedObject} type.
     */
    public Class<M> getManagedObjectType() {
        return this.managedObjectType;
    }

    /**
     * Specifies the {@link OfficeFloorManagedObjectSource}.
     *
     * @param managedObjectSource {@link OfficeFloorManagedObjectSource}.
     */
    public void setManagedObjectSource(OfficeFloorManagedObjectSource managedObjectSource) {
        this.managedObjectSource = managedObjectSource;
    }

    /*
     * =========== ExternalServiceInputFactory ============
     */

    @Override
    public ExternalServiceInputNode<O, M> createExternalServiceInput(DeployedOfficeInput deployedOfficeInput) {

        // Obtain the flow name
        String flowName = deployedOfficeInput.getDeployedOfficeSectionName() +
                "_" + deployedOfficeInput.getDeployedOfficeInputName();

        // Obtain the flow index and register flow
        int flowIndex = this.flows.size();
        this.flows.add(flowName);

        // Create the external service input
        ExternalServiceInput<O, M> externalServiceInput = new ExternalServiceInputImpl(flowIndex);

        // Create the flow
        OfficeFloorManagedObjectFlow flow = this.managedObjectSource.getOfficeFloorManagedObjectFlow(flowName);

        // Create and return the external service input node
        return new ExternalServiceInputNodeImpl(flow, externalServiceInput);
    }

    /*
     * ================ ManagedObjectSource =================
     */

    @Override
    protected void loadSpecification(SpecificationContext context) {
    }

    @Override
    protected void loadMetaData(MetaDataContext<None, Indexed> context) throws Exception {
        context.setObjectClass(this.objectType);
        context.setManagedObjectClass(this.managedObjectType);
        for (String flowName : this.flows) {
            context.addFlow(null).setLabel(flowName);
        }

        // Configure clean up escalation handling
        context.getManagedObjectSourceContext().getRecycleFunction(new ManagedFunctionFactory<None, None>() {
            @Override
            public ManagedFunction<None, None> createManagedFunction() throws Throwable {
                return ExternalServiceInputFactoryImpl.this;
            }
        }).linkParameter(0, RecycleManagedObjectParameter.class);
    }

    @Override
    public void start(ManagedObjectExecuteContext<Indexed> context) throws Exception {
        this.servicer = new SafeManagedObjectService<>(context);
    }

    @Override
    protected ManagedObject getManagedObject() throws Throwable {
        // Not externally servicing, so no object
        return new NullManagedObject();
    }

    /*
     * ================ Recycle ManagedFunction ======================
     */

    @Override
    public void execute(ManagedFunctionContext<None, None> context) throws Throwable {

        // Obtain the recycle parameter
        RecycleManagedObjectParameter<M> parameter = RecycleManagedObjectParameter
                .getRecycleManagedObjectParameter(context);

        // Obtain the managed object
        M managedObject = parameter.getManagedObject();

        // Clean the managed object
        managedObject.clean(parameter.getCleanupEscalations());

        // Enable re-use of the object
        parameter.reuseManagedObject();
    }

    /**
     * {@link ExternalServiceInputNode} implementation.
     */
    private class ExternalServiceInputNodeImpl implements ExternalServiceInputNode<O, M> {

        private final OfficeFloorManagedObjectFlow flow;

        private final ExternalServiceInput<O, M> input;

        private ExternalServiceInputNodeImpl(OfficeFloorManagedObjectFlow flow, ExternalServiceInput<O, M> input) {
            this.flow = flow;
            this.input = input;
        }

        /*
         * ================== ExternalServiceInputNode ==================
         */

        @Override
        public OfficeFloorManagedObjectFlow getOfficeFloorManagedObjectFlow() {
            return this.flow;
        }

        @Override
        public ExternalServiceInput<O, M> getExternalServiceInput() {
            return this.input;
        }
    }

    /**
     * {@link ExternalServiceInput} implementation.
     */
    private class ExternalServiceInputImpl implements ExternalServiceInput<O, M> {

        private final int flowIndex;

        private ExternalServiceInputImpl(int flowIndex) {
            this.flowIndex = flowIndex;
        }

        /*
         * ================== ExternalServiceInput ==================
         */

        @Override
        public ProcessManager service(InputManagedObject managedObject, FlowCallback callback) {
            return ExternalServiceInputFactoryImpl.this.servicer.invokeProcess(this.flowIndex, null, managedObject, 0, callback);
        }
    }

    /**
     * {@link ManagedObject} providing <code>null</code> value. Must implement all
     * {@link ManagedObject} interfaces to avoid {@link ClassCastException}.
     */
    private static class NullManagedObject implements ManagedObject, ContextAwareManagedObject,
            CoordinatingManagedObject<None>, AsynchronousManagedObject, InputManagedObject {

        /*
         * =================== ManagedObject ====================
         */

        @Override
        public Object getObject() throws Throwable {
            return null;
        }

        @Override
        public void setManagedObjectContext(ManagedObjectContext context) {
            // Ignored
        }

        @Override
        public void setAsynchronousContext(AsynchronousContext context) {
            // Ignored
        }

        @Override
        public void loadObjects(ObjectRegistry<None> registry) throws Throwable {
            // Ignored
        }

        @Override
        public void clean(CleanupEscalation[] cleanupEscalations) throws Throwable {
            // Ignored
        }
    }

}
