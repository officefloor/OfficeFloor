/*-
 * #%L
 * OfficeCompiler
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

package net.officefloor.plugin.clazz.flow;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import net.officefloor.frame.api.function.FlowCallback;
import net.officefloor.frame.api.source.SourceContext;
import net.officefloor.frame.internal.structure.Flow;
import net.officefloor.plugin.clazz.FlowInterface;

/**
 * Factory to create the {@link Flow} instances.
 *
 * @author Daniel Sagenschneider
 */
public class ClassFlowBuilder<A extends Annotation> {

    /**
     * {@link Class} of the {@link Annotation}.
     */
    private final Class<A> annotationClass;

    /**
     * Instantiate.
     *
     * @param annotationClass {@link Class} of the {@link Annotation}.
     */
    public ClassFlowBuilder(Class<A> annotationClass) {
        this.annotationClass = annotationClass;
    }

    /**
     * Builds the {@link ClassFlowInterfaceFactory} for the {@link net.officefloor.plugin.section.clazz.Flow} injected interface.
     *
     * @param flowName          Name of the {@link Flow}.
     * @param flowInterfaceType Type of interface. Should only have a single {@link Method}.
     * @param flowRegistry      {@link ClassFlowRegistry}.
     * @param sourceContext     {@link SourceContext}.
     * @return {@link ClassFlowInterfaceFactory}.
     * @throws Exception If fails to build the {@link ClassFlowInterfaceFactory}.
     */
    public ClassFlowInterfaceFactory buildFlowFactory(String flowName, Class<?> flowInterfaceType, ClassFlowRegistry flowRegistry, SourceContext sourceContext) throws Exception {

        // Ensure is an interface
        if (!flowInterfaceType.isInterface()) {
            throw new Exception("Parameter of type " + flowInterfaceType.getSimpleName()
                    + " must be an interface as parameter annotated with " + this.annotationClass.getSimpleName());
        }

        // Ensure only a single method (ignoring default methods)
        Method[] flowMethods = Arrays.stream(flowInterfaceType.getMethods())
                .filter(method -> !method.isDefault())
                .toArray(Method[]::new);
        if (flowMethods.length != 1) {
            throw new Exception("Parameter of type " + flowInterfaceType.getSimpleName()
                    + " may only have one method as parameter annotated with " + this.annotationClass.getSimpleName());
        }
        Method flowMethod = flowMethods[0];

        // Build the flow meta-data
        ClassFlowMethodMetaData flowMethodMetaData = this.buildFlow(flowName, flowInterfaceType, flowMethod, flowRegistry, "error");

        // Only the single flow meta-data
        Map<String, ClassFlowMethodMetaData> flowMethodMetaDatas = new HashMap<>();
        flowMethodMetaDatas.put(flowName, flowMethodMetaData);

        // Create and return the flow interface parameter factory
        return new ClassFlowInterfaceFactory(sourceContext, flowInterfaceType, flowMethodMetaDatas);
    }

    /**
     * Builds the {@link ClassFlowInterfaceFactory} for the {@link FlowInterface}.
     *
     * @param flowInterfaceType Interface {@link Class} for the
     *                          {@link FlowInterface}.
     * @param flowRegistry      {@link ClassFlowRegistry}.
     * @param sourceContext     {@link SourceContext}.
     * @return {@link ClassFlowInterfaceFactory} or <code>null</code> if parameter
     * is not a {@link FlowInterface}.
     * @throws Exception If fails to build the {@link ClassFlowInterfaceFactory}.
     */
    public ClassFlowInterfaceFactory buildFlowInterfaceFactory(Class<?> flowInterfaceType,
                                                               ClassFlowRegistry flowRegistry, SourceContext sourceContext) throws Exception {

        // Determine if flow interface
        if (!flowInterfaceType.isAnnotationPresent(this.annotationClass)) {
            throw new Exception("Dependency " + flowInterfaceType.getSimpleName() + " not annotated with "
                    + this.annotationClass.getSimpleName());
        }

        // Ensure is an interface
        if (!flowInterfaceType.isInterface()) {
            throw new Exception("Dependency " + flowInterfaceType.getSimpleName()
                    + " must be an interface as annotated with " + this.annotationClass.getSimpleName());
        }

        // Obtain the methods sorted (deterministic order)
        Method[] flowMethods = flowInterfaceType.getMethods();
        Arrays.sort(flowMethods, (a, b) -> a.getName().compareTo(b.getName()));

        // Create a flow for each method of the interface
        Map<String, ClassFlowMethodMetaData> flowMethodMetaDatas = new HashMap<String, ClassFlowMethodMetaData>(
                flowMethods.length);
        for (Method flowMethod : flowMethods) {
            String flowMethodName = flowMethod.getName();

            // Not include object methods
            if (Object.class.equals(flowMethod.getDeclaringClass())) {
                continue;
            }

            // Determine if default method
            if (flowMethod.isDefault()) {
                continue;
            }

            // Ensure not duplicate flow names
            if (flowMethodMetaDatas.containsKey(flowMethodName)) {
                throw new Exception("May not have duplicate flow method names (flow="
                        + flowInterfaceType.getSimpleName() + "." + flowMethodName + ")");
            }

            // Build the flow method meta-data
            ClassFlowMethodMetaData flowMethodMetaData = this.buildFlow(flowMethodName, flowInterfaceType, flowMethod, flowRegistry, "(flow "
                    + flowInterfaceType.getSimpleName() + "." + flowMethodName + ")");

            // Register
            flowMethodMetaDatas.put(flowMethodName, flowMethodMetaData);
        }

        // Create and return the flow interface parameter factory
        return new ClassFlowInterfaceFactory(sourceContext, flowInterfaceType, flowMethodMetaDatas);
    }

    /**
     * Builds the {@link ClassFlowMethodMetaData}.
     *
     * @param flowName          Name of the {@link Flow}.
     * @param flowInterfaceType Interface type of dependency to invoke for the {@link Flow}.
     * @param flowMethod        {@link Method} to invoke for the {@link Flow}.
     * @param flowRegistry      {@link ClassFlowRegistry}.
     * @param errorSuffix       Error suffix.
     * @return {@link ClassFlowMethodMetaData}.
     * @throws Exception If fails to build the {@link ClassFlowMethodMetaData}.
     */
    protected ClassFlowMethodMetaData buildFlow(String flowName, Class<?> flowInterfaceType, Method flowMethod, ClassFlowRegistry flowRegistry, String errorSuffix) throws Exception {

        // Obtain the method
        String flowMethodName = flowMethod.getName();

        // Ensure appropriate parameters
        Class<?> flowParameterType = null;
        boolean isFlowCallback = false;
        Class<?>[] flowMethodParams = flowMethod.getParameterTypes();
        switch (flowMethodParams.length) {
            case 2:
                // Two parameters, first parameter, second flow callback
                flowParameterType = flowMethodParams[0];
                if (!FlowCallback.class.isAssignableFrom(flowMethodParams[1])) {
                    throw new Exception("Second parameter must be " + FlowCallback.class.getSimpleName() + " " + errorSuffix);
                }
                isFlowCallback = true;
                break;

            case 1:
                // Single parameter, either parameter or flow callback
                if (FlowCallback.class.isAssignableFrom(flowMethodParams[0])) {
                    isFlowCallback = true;
                } else {
                    flowParameterType = flowMethodParams[0];
                }
                break;

            case 0:
                // No parameters
                break;

            default:
                // Invalid to have more than two parameter
                throw new Exception(
                        "Flow methods may only have at most two parameters [<parameter>, <flow callback>] " + errorSuffix);
        }

        // Ensure void return type
        Class<?> flowReturnType = flowMethod.getReturnType();
        if ((flowReturnType != null) && (!Void.TYPE.equals(flowReturnType))) {
            // Invalid return type
            throw new Exception("Flow method " + flowInterfaceType.getSimpleName() + "." + flowMethodName
                    + " return type is invalid (return type=" + flowReturnType.getName()
                    + ").  Must not have return type. " + errorSuffix);
        }

        // Register the flow
        int flowIndex = flowRegistry.registerFlow(
                new ClassFlowContextImpl(flowName, flowInterfaceType, flowMethod, flowParameterType, isFlowCallback));

        // Create and return the flow method meta-data
        return new ClassFlowMethodMetaData(flowMethod, flowIndex,
                flowParameterType != null, isFlowCallback);
    }

    /**
     * {@link ClassFlowContext} implementation.
     */
    private static class ClassFlowContextImpl implements ClassFlowContext {

        /**
         * Name of the {@link Flow}.
         */
        private final String flowName;

        /**
         * {@link FlowInterface} type.
         */
        private final Class<?> flowInterfaceType;

        /**
         * {@link Method} on interface invoking the {@link Flow}.
         */
        private final Method method;

        /**
         * Possible parameter type for {@link Flow}. <code>null</code> if no parameter.
         */
        private final Class<?> parameterType;

        /**
         * Indicates if {@link FlowCallback} on {@link Flow}.
         */
        private final boolean isFlowCallback;

        /**
         * Initiate.
         *
         * @param flowName          Name of the {@link Flow}.
         * @param flowInterfaceType {@link FlowInterface} type.
         * @param method            {@link Method} on interface invoking the
         *                          {@link Flow}.
         * @param parameterType     Possible parameter type for {@link Flow}.
         *                          <code>null</code> if no parameter.
         * @param isFlowCallback    Indicates if {@link FlowCallback} on {@link Flow}.
         */
        private ClassFlowContextImpl(String flowName, Class<?> flowInterfaceType, Method method, Class<?> parameterType,
                                     boolean isFlowCallback) {
            this.flowName = flowName;
            this.flowInterfaceType = flowInterfaceType;
            this.method = method;
            this.parameterType = parameterType;
            this.isFlowCallback = isFlowCallback;
        }

        /*
         * ======================= ClassFlowContext ===========================
         */

        @Override
        public String getFlowName() {
            return this.flowName;
        }

        @Override
        public Class<?> getFlowInterfaceType() {
            return this.flowInterfaceType;
        }

        @Override
        public Method getMethod() {
            return this.method;
        }

        @Override
        public Class<?> getParameterType() {
            return this.parameterType;
        }

        @Override
        public boolean isFlowCallback() {
            return this.isFlowCallback;
        }
    }

}
