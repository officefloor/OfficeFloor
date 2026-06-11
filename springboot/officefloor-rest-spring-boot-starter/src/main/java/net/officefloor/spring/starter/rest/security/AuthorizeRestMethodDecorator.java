/*-
 * #%L
 * OfficeFloor REST Spring Boot Starter
 * %%
 * Copyright (C) 2005 - 2026 Daniel Sagenschneider
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

package net.officefloor.spring.starter.rest.security;

import net.officefloor.compile.spi.managedfunction.source.FunctionNamespaceBuilder;
import net.officefloor.compile.spi.managedfunction.source.ManagedFunctionSourceContext;
import net.officefloor.compile.spi.managedfunction.source.ManagedFunctionTypeBuilder;
import net.officefloor.compile.spi.managedfunction.source.impl.AbstractManagedFunctionSource;
import net.officefloor.compile.spi.office.OfficeSection;
import net.officefloor.compile.spi.section.SectionDesigner;
import net.officefloor.compile.spi.section.SectionFunction;
import net.officefloor.compile.spi.section.source.SectionSourceContext;
import net.officefloor.compile.spi.section.source.impl.AbstractSectionSource;
import net.officefloor.frame.api.build.Indexed;
import net.officefloor.frame.api.function.ManagedFunction;
import net.officefloor.frame.api.function.ManagedFunctionContext;
import net.officefloor.frame.api.function.ManagedFunctionFactory;
import net.officefloor.server.http.ServerHttpConnection;
import net.officefloor.spring.starter.rest.SpringServerHttpConnection;
import net.officefloor.web.rest.build.RestMethodDecorator;
import net.officefloor.web.rest.build.RestMethodDecoratorContext;
import net.officefloor.web.rest.build.RestPathContext;
import org.aopalliance.intercept.MethodInvocation;
import org.springframework.context.ApplicationContext;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.Expression;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.security.access.expression.ExpressionUtils;
import org.springframework.security.access.expression.method.MethodSecurityExpressionHandler;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * {@link RestMethodDecorator} applying YAML-configured SpEL authorization before endpoint execution.
 * <p>
 * The {@code authorize} key is read from the {@code composition:} block of an endpoint YAML file,
 * or from the top-level of a path-config YAML file (inherited by child endpoints).
 * The most-specific (deepest) configured expression wins.
 * <p>
 * The SpEL expression is parsed once at startup for syntax validation and compiled for runtime speed.
 */
public class AuthorizeRestMethodDecorator implements RestMethodDecorator<Void> {

    private static final SpelExpressionParser EXPRESSION_PARSER = new SpelExpressionParser();

    /*
     * =================== RestMethodDecorator ===================
     */

    @Override
    public void decorateRestMethod(RestMethodDecoratorContext<Void> context) {

        // Resolve authorize expression: endpoint composition: block first, then parent path chain
        String expressionText = context.getConfiguration("authorize", String.class);
        if (expressionText == null) {
            RestPathContext path = context.getPath();
            while (path != null) {
                expressionText = path.getConfiguration("authorize", String.class);
                if (expressionText != null) break;
                path = path.getParentPath();
            }
        }

        if (expressionText == null) {
            return; // no authorization configured
        }

        // Parse once at startup: catches syntax errors immediately and gives a compiled Expression
        Expression expression = EXPRESSION_PARSER.parseExpression(expressionText);

        String sectionName = "YAML_AUTHORIZE_" + context.getHttpMethod().getName() + "_" + context.getPath().getPath();
        context.addHttpInputInterceptor(interceptorContext -> {
            OfficeSection section = interceptorContext.getOfficeArchitect()
                    .addOfficeSection(sectionName, new AuthorizeSectionSource(expression), null);
            interceptorContext.link(
                    section.getOfficeSectionInput("input"),
                    section.getOfficeSectionOutput("output"));
        });
    }

    private enum AuthorizeFlow { OUTPUT }

    private static class AuthorizeSectionSource extends AbstractSectionSource {

        private final Expression expression;

        AuthorizeSectionSource(Expression expression) {
            this.expression = expression;
        }

        @Override
        protected void loadSpecification(SpecificationContext context) {}

        @Override
        public void sourceSection(SectionDesigner designer, SectionSourceContext context) throws Exception {
            SectionFunction function = designer
                    .addSectionFunctionNamespace("Authorize", new AuthorizeFunctionSource(this.expression))
                    .addSectionFunction("Authorize", "Authorize");

            designer.link(designer.addSectionInput("input", null), function);
            designer.link(
                    function.getFunctionFlow(AuthorizeFlow.OUTPUT.name()),
                    designer.addSectionOutput("output", null, false),
                    false);
            designer.link(
                    function.getFunctionObject("0"),
                    designer.addSectionObject(ServerHttpConnection.class.getSimpleName(), ServerHttpConnection.class.getName()));
        }
    }

    private static class AuthorizeFunctionSource extends AbstractManagedFunctionSource
            implements ManagedFunctionFactory<Indexed, AuthorizeFlow>, ManagedFunction<Indexed, AuthorizeFlow>, MethodInvocation {

        private static final Method DUMMY_METHOD;

        static {
            try {
                DUMMY_METHOD = Object.class.getMethod("hashCode");
            } catch (NoSuchMethodException ex) {
                throw new IllegalStateException("Unable to obtain dummy method", ex);
            }
        }

        private final Expression expression;

        AuthorizeFunctionSource(Expression expression) {
            this.expression = expression;
        }

        /*
         * =============== AbstractManagedFunctionSource ===============
         */

        @Override
        protected void loadSpecification(SpecificationContext context) {}

        @Override
        public void sourceManagedFunctions(FunctionNamespaceBuilder builder, ManagedFunctionSourceContext context) throws Exception {
            ManagedFunctionTypeBuilder<Indexed, AuthorizeFlow> function =
                    builder.addManagedFunctionType("Authorize", Indexed.class, AuthorizeFlow.class);
            function.setFunctionFactory(this);
            function.addFlow().setKey(AuthorizeFlow.OUTPUT);
            function.addObject(ServerHttpConnection.class);
        }

        /*
         * ================ ManagedFunctionFactory =================
         */

        @Override
        public ManagedFunction<Indexed, AuthorizeFlow> createManagedFunction() throws Throwable {
            return this;
        }

        /*
         * =================== ManagedFunction ====================
         */

        @Override
        public void execute(ManagedFunctionContext<Indexed, AuthorizeFlow> context) throws Throwable {

            // Obtain ApplicationContext via the Spring-aware HTTP connection
            ServerHttpConnection connection = (ServerHttpConnection) context.getObject(0);
            SpringServerHttpConnection springConnection = (SpringServerHttpConnection) connection;
            ApplicationContext applicationContext = springConnection.getApplicationContext();

            // Skip evaluation if Spring method-level security is not active
            final String CONFIG_BEAN = "_prePostMethodSecurityConfiguration";
            if (!applicationContext.containsBean(CONFIG_BEAN)) {
                context.doFlow(AuthorizeFlow.OUTPUT, null, null);
                return;
            }

            // Obtain the expression handler (same reflection approach as AuthorizeAdministrationSource)
            Object configuration = applicationContext.getBean(CONFIG_BEAN);
            Field expressionHandlerField = configuration.getClass().getDeclaredField("expressionHandler");
            expressionHandlerField.setAccessible(true);
            MethodSecurityExpressionHandler expressionHandler =
                    (MethodSecurityExpressionHandler) expressionHandlerField.get(configuration);

            // Evaluate the pre-compiled expression with the current authentication
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            EvaluationContext evalContext = expressionHandler.createEvaluationContext(() -> auth, this);
            if (!ExpressionUtils.evaluateAsBoolean(this.expression, evalContext)) {
                throw new AuthorizationDeniedException("Access denied");
            }

            context.doFlow(AuthorizeFlow.OUTPUT, null, null);
        }

        /*
         * =================== MethodInvocation ===================
         * Required by MethodSecurityExpressionHandler.createEvaluationContext
         */

        @Override
        public Method getMethod() { return DUMMY_METHOD; }

        @Override
        public Object[] getArguments() { return new Object[0]; }

        @Override
        public Object proceed() throws Throwable { return null; }

        @Override
        public Object getThis() { return this; }

        @Override
        public AccessibleObject getStaticPart() { return null; }
    }
}
