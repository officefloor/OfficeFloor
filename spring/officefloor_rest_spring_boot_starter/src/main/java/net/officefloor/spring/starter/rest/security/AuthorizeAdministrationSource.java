package net.officefloor.spring.starter.rest.security;

import net.officefloor.compile.spi.administration.source.impl.AbstractAdministrationSource;
import net.officefloor.frame.api.administration.Administration;
import net.officefloor.frame.api.administration.AdministrationContext;
import net.officefloor.frame.api.administration.AdministrationFactory;
import net.officefloor.frame.api.build.None;
import net.officefloor.server.http.ServerHttpConnection;
import net.officefloor.spring.starter.rest.SpringServerHttpConnection;
import org.aopalliance.intercept.MethodInvocation;
import org.springframework.context.ApplicationContext;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.Expression;
import org.springframework.security.access.expression.ExpressionUtils;
import org.springframework.security.access.expression.method.MethodSecurityExpressionHandler;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.lang.annotation.Annotation;
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.function.Function;

public class AuthorizeAdministrationSource<A extends Annotation> extends AbstractAdministrationSource<ServerHttpConnection, None, None>
        implements  AdministrationFactory<ServerHttpConnection, None, None>, Administration<ServerHttpConnection, None, None>, MethodInvocation {

    private static final Method DUMMY_METHOD;

    static {
        try {
            DUMMY_METHOD = Object.class.getMethod("hashCode");
        } catch (NoSuchMethodException ex) {
            throw new IllegalStateException("Unable to obtain dummy method", ex);
        }
    }

    private final Class<A> annotationType;

    private final Function<A, String> extractExpression;

    public AuthorizeAdministrationSource(Class<A> annotationType, Function<A, String> extractExpression) {
        this.annotationType = annotationType;
        this.extractExpression = extractExpression;
    }

    /*
     * ===================== AdministrationSource ======================
     */

    @Override
    protected void loadSpecification(SpecificationContext context) {
        // No specification
    }

    @Override
    protected void loadMetaData(MetaDataContext<ServerHttpConnection, None, None> context) throws Exception {
        context.setExtensionInterface(ServerHttpConnection.class);
        context.setAdministrationFactory(this);
    }

    /*
     * ===================== AdministrationFactory ======================
     */

    @Override
    public Administration<ServerHttpConnection, None, None> createAdministration() throws Throwable {
        return this;
    }

    /*
     * ========================= Administration ==========================
     */

    @Override
    public void administer(AdministrationContext<ServerHttpConnection, None, None> context) throws Throwable {

        // Obtain the application context
        ApplicationContext applicationContext = null;
        for (ServerHttpConnection connection : context.getExtensions()) {
            if (connection instanceof SpringServerHttpConnection) {
                SpringServerHttpConnection springConnection = (SpringServerHttpConnection) connection;
                applicationContext = springConnection.getApplicationContext();
            }
        }
        if (applicationContext == null) {
            throw new IllegalStateException("OfficeFloor REST spring boot starter invalid state as can't obtain " + SpringServerHttpConnection.class.getName());
        }

        // Obtain the configuration (or return if method level security not active in Spring)
        final String CONFIGURATION_BEAN_NAME = "_prePostMethodSecurityConfiguration";
        if (!applicationContext.containsBean(CONFIGURATION_BEAN_NAME)) {
            return; // Method level security in Spring not active
        }
        Object configuration = applicationContext.getBean(CONFIGURATION_BEAN_NAME);

        // Obtain the expression handler
        Field expressionHandlerField = configuration.getClass().getDeclaredField("expressionHandler");
        expressionHandlerField.setAccessible(true);
        MethodSecurityExpressionHandler expressionHandler = (MethodSecurityExpressionHandler) expressionHandlerField.get(configuration);

        // Create the expression context
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        EvaluationContext ctx = expressionHandler.createEvaluationContext(() -> auth, this);

        // Obtain the appropriate annotation
        Object[] annotationObjects = context.getManagedFunctionAnnotations();
        for (Object annotationObject : annotationObjects) {
            if (this.annotationType.isInstance(annotationObject)) {
                A annotation = (A) annotationObject;

                // Found annotation, so extract expression
                String expressionText = this.extractExpression.apply(annotation);

                // Undertake expression
                Expression expression = expressionHandler.getExpressionParser().parseExpression(expressionText);
                boolean isPermitted = ExpressionUtils.evaluateAsBoolean(expression, ctx);
                if (!isPermitted) {
                    throw new AuthorizationDeniedException("Access denied");
                }

            }
        }
    }

    /*
     * ======================= MethodInvocation =========================
     */

    @Override
    public Method getMethod() {
        return DUMMY_METHOD;
    }

    @Override
    public Object[] getArguments() {
        return new Object[0];
    }

    @Override
    public Object proceed() throws Throwable {
        return null;
    }

    @Override
    public Object getThis() {
        return this;
    }

    @Override
    public AccessibleObject getStaticPart() {
        return null;
    }
}
