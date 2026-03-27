package net.officefloor.plugin.clazz.dependency.impl;

import net.officefloor.frame.api.source.SourceContext;
import net.officefloor.plugin.clazz.dependency.ClassDependencyFactory;
import net.officefloor.plugin.clazz.dependency.ClassDependencyManufacturerContext;
import net.officefloor.plugin.clazz.flow.ClassFlowBuilder;
import net.officefloor.plugin.clazz.flow.ClassFlowInterfaceFactory;
import net.officefloor.plugin.section.clazz.Flow;

import java.lang.annotation.Annotation;

public class FlowClassDependencyManufacturer extends AbstractFlowClassDependencyManufacturer {

    /**
     * Obtains the name of the {@link net.officefloor.frame.internal.structure.Flow}.
     *
     * @param annotation {@link Annotation}.
     * @param context    {@link ClassDependencyManufacturerContext}.
     * @return Name of the {@link net.officefloor.frame.internal.structure.Flow}.
     */
    protected String getFlowName(Annotation annotation, ClassDependencyManufacturerContext context) {
        Flow flow = (Flow) annotation;
        return flow.value();
    }

    @Override
    protected Class<? extends Annotation> getAnnotationType() {
        return Flow.class;
    }

    /*
     * ======================== ClassDependencyManufacturer =======================
     */

    @Override
    public ClassDependencyFactory createParameterFactory(ClassDependencyManufacturerContext context) throws Exception {

        // Determine if flow
        Class<? extends Annotation> annotationType = this.getAnnotationType();
        Flow annotation = (Flow) context.getDependencyAnnotation(annotationType);
        if (annotation == null) {
            return null; // not flow parameter
        }

        // Obtain details to create flow
        SourceContext sourceContext = context.getSourceContext();

        // Create the flow
        String flowName = this.getFlowName(annotation, context);
        Class<?> dependencyType = context.getDependencyClass();
        ClassFlowBuilder<? extends Annotation> flowBuilder = new ClassFlowBuilder<>(annotationType);
        ClassFlowInterfaceFactory factory = flowBuilder.buildFlowFactory(flowName, dependencyType,
                (flowContext) -> this.buildFlow(this.addFlow(context, flowContext), flowContext), sourceContext);

        // Create and return the factory
        return new FlowClassDependencyFactory(factory);
    }

}
