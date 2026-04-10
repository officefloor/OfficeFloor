package net.officefloor.spring.starter.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import net.officefloor.activity.compose.build.ComposeArchitect;
import net.officefloor.activity.compose.build.ComposeEmployer;
import net.officefloor.compile.managedfunction.ManagedFunctionObjectType;
import net.officefloor.compile.managedfunction.ManagedFunctionType;
import net.officefloor.compile.properties.PropertyList;
import net.officefloor.compile.spi.office.OfficeArchitect;
import net.officefloor.compile.spi.office.OfficeManagedObject;
import net.officefloor.compile.spi.office.source.OfficeSourceContext;
import net.officefloor.compile.spi.office.source.impl.AbstractOfficeSource;
import net.officefloor.compile.spi.officefloor.ExternalServiceInput;
import net.officefloor.frame.api.managedobject.source.ManagedObjectSource;
import net.officefloor.frame.internal.structure.ManagedObjectScope;
import net.officefloor.plugin.section.clazz.MethodParameterAnnotation;
import net.officefloor.server.http.HttpMethod;
import net.officefloor.server.http.ServerHttpConnection;
import net.officefloor.server.http.impl.ProcessAwareServerHttpConnectionManagedObject;
import net.officefloor.spring.starter.rest.argument.SpringArgumentManagedObjectSource;
import net.officefloor.spring.starter.rest.argument.SpringBeanManagedObjectSource;
import net.officefloor.spring.starter.rest.argument.SpringMvcArguments;
import net.officefloor.spring.starter.rest.argument.SpringTypeQualifierInterrogator;
import net.officefloor.spring.starter.rest.response.RequestEntityHttpObjectResponderFactory;
import net.officefloor.spring.starter.rest.response.SpringExceptionHandler;
import net.officefloor.spring.starter.rest.response.SpringExceptionHandlerServiceFactory;
import net.officefloor.spring.starter.rest.response.SpringExceptionHandlerResponderFactory;
import net.officefloor.spring.starter.rest.servlet.HttpServletRequestManagedObjectSource;
import net.officefloor.spring.starter.rest.servlet.HttpServletResponseManagedObjectSource;
import net.officefloor.spring.starter.rest.view.ViewResponse;
import net.officefloor.spring.starter.rest.view.ViewResponseManagedObjectSource;
import net.officefloor.web.WebArchitectEmployer;
import net.officefloor.web.build.WebArchitect;
import net.officefloor.web.json.JacksonHttpObjectParserFactory;
import net.officefloor.web.json.JacksonHttpObjectResponderFactory;
import net.officefloor.web.rest.build.RestArchitect;
import net.officefloor.web.rest.build.RestEmployer;
import net.officefloor.web.rest.build.RestEndpoint;
import net.officefloor.web.rest.build.RestEndpointContext;
import net.officefloor.web.rest.build.RestEndpointListener;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.AnnotatedBeanDefinition;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.type.AnnotationMetadata;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class SpringBootOfficeSource extends AbstractOfficeSource {

    private final Logger logger;

    private final ObjectMapper objectMapper;

    private final List<OfficeFloorRestEndpoint> restEndpoints;

    private final ConfigurableApplicationContext applicationContext;

    private final Map<String, OfficeManagedObject> springArguments = new HashMap<>();

    public SpringBootOfficeSource(Logger logger,
                                  ObjectMapper objectMapper,
                                  List<OfficeFloorRestEndpoint> restEndpoints,
                                  ConfigurableApplicationContext applicationContext) {
        this.logger = logger;
        this.objectMapper = objectMapper;
        this.restEndpoints = restEndpoints;
        this.applicationContext = applicationContext;
    }

    /*
     * ======================= OfficeSource ========================
     */

    @Override
    protected void loadSpecification(SpecificationContext specificationContext) {
        // No specification
    }

    @Override
    public void sourceOffice(OfficeArchitect officeArchitect, OfficeSourceContext officeSourceContext) throws Exception {

        // Employ the architects
        WebArchitect webArchitect = WebArchitectEmployer.employWebArchitect(officeArchitect, officeSourceContext);
        ComposeArchitect composeArchitect = ComposeEmployer.employComposeArchitect(officeArchitect, officeSourceContext);
        RestArchitect restArchitect = RestEmployer.employRestArchitect(officeArchitect, webArchitect, composeArchitect, officeSourceContext);

        // Undertake Spring Boot extensions
        for (OfficeFloorSpringBootExtension springBootExtension : officeSourceContext.loadOptionalServices(OfficeFloorSpringBootExtensionServiceFactory.class)) {
            springBootExtension.extendSpringBootSupport(new OfficeFloorSpringBootExtensionContext() {
                @Override
                public OfficeArchitect getOfficeArchitect() {
                    return officeArchitect;
                }

                @Override
                public OfficeSourceContext getOfficeSourceContext() {
                    return officeSourceContext;
                }

                @Override
                public ComposeArchitect getComposeArchitect() {
                    return composeArchitect;
                }

                @Override
                public RestArchitect getRestArchitect() {
                    return restArchitect;
                }

                @Override
                public ConfigurableApplicationContext getApplicationContext() {
                    return SpringBootOfficeSource.this.applicationContext;
                }
            });
        }

        // Configure object requests
        webArchitect.addHttpObjectParser(new JacksonHttpObjectParserFactory(this.objectMapper));

        // Handle RequestEntity before generic JSON
        webArchitect.addHttpObjectResponder(new RequestEntityHttpObjectResponderFactory(this.objectMapper));

        // Load the Spring Exception Handlers
        List<SpringExceptionHandler> springExceptionHandlersList = new LinkedList<>();
        for (SpringExceptionHandler springExceptionHandler : officeSourceContext.loadOptionalServices(SpringExceptionHandlerServiceFactory.class)) {
            if (springExceptionHandler != null) {
                springExceptionHandlersList.add(springExceptionHandler);
            }
        }
        SpringExceptionHandler[] springExceptionHandlers = springExceptionHandlersList.toArray(SpringExceptionHandler[]::new);

        // Handle JSON with Spring handling errors first
        webArchitect.addHttpObjectResponder(new SpringExceptionHandlerResponderFactory("application/json", springExceptionHandlers));
        webArchitect.addHttpObjectResponder(new JacksonHttpObjectResponderFactory(this.objectMapper));

        // All generic Spring error handling
        webArchitect.addHttpObjectResponder(new SpringExceptionHandlerResponderFactory("*/*", springExceptionHandlers));

        // Add the rest servicing
        this.logger.info("Loading REST endpoints:");
        PropertyList propertyList = officeSourceContext.createPropertyList();
        for (String propertyName : officeSourceContext.getPropertyNames()) {
            propertyList.addProperty(propertyName).setValue(officeSourceContext.getProperty(propertyName));
        }
        restArchitect.addRestServices(false, "officefloor/rest", propertyList, new RestEndpointListener() {
            @Override
            public void initialise(RestEndpointContext restEndpointContext) {
                logger.info("  " + restEndpointContext.getHttpMethod().getName() + " /" + restEndpointContext.getPath());
            }

            @Override
            public void endpoint(RestEndpoint restEndpoint) {

                // Register the end point
                HttpMethod httpMethod = restEndpoint.getHttpMethod();
                String path =  restEndpoint.getPath();
                ExternalServiceInput externalServiceInput = restEndpoint.getHttpInput().getDirect().addExternalServiceInput(ServerHttpConnection.class, ProcessAwareServerHttpConnectionManagedObject.class);
                SpringBootOfficeSource.this.restEndpoints.add(new OfficeFloorRestEndpoint(httpMethod, path, externalServiceInput));
            }
        });

        // Register all the Spring components for use
        ConfigurableListableBeanFactory beanFactory = this.applicationContext.getBeanFactory();
        for (String beanName : beanFactory.getBeanDefinitionNames()) {
            BeanDefinition beanDefinition = beanFactory.getBeanDefinition(beanName);

            // Obtain the bean type
            Class<?> beanType = beanFactory.getType(beanName);

            // Ensure type can be loaded (avoid lambdas)
            String beanTypeName = beanType.getName();
            if (officeSourceContext.loadOptionalClass(beanTypeName) != null) {

                // Obtain the bean qualifier
                String qualifier = null;
                if (beanDefinition instanceof AnnotatedBeanDefinition) {
                    AnnotatedBeanDefinition annotatedBeanDefinition = (AnnotatedBeanDefinition) beanDefinition;
                    AnnotationMetadata metadata = annotatedBeanDefinition.getMetadata();

                    // Obtain the qualifier
                    Map<String, Object> attributes = metadata.getAnnotationAttributes(Qualifier.class.getName());
                    qualifier = (attributes != null) ? (String) attributes.get("value") : null;
                }

                // Register bean
                String objectName = "SPRING_" + beanName;
                OfficeManagedObject beanMo = officeArchitect.addOfficeManagedObjectSource(
                                objectName,
                                new SpringBeanManagedObjectSource(beanName, beanType, beanFactory))
                        .addOfficeManagedObject(objectName, ManagedObjectScope.THREAD);
                if (qualifier != null) {
                    beanMo.addTypeQualification(qualifier, beanTypeName);
                }

            }
        }

        // Add access to servlet request/response
        this.addOfficeManagedObjectSource(HttpServletRequest.class, new HttpServletRequestManagedObjectSource(), officeArchitect);
        this.addOfficeManagedObjectSource(HttpServletResponse.class, new HttpServletResponseManagedObjectSource(), officeArchitect);
        webArchitect.enableHttpExternalResponse();

        // Allow rendering of views
        this.addOfficeManagedObjectSource(ViewResponse.class, new ViewResponseManagedObjectSource(), officeArchitect);

        // Obtain the Spring argument checker
        SpringMvcArguments.SpringArgumentChecker springArgumentChecker = SpringMvcArguments.getSpringArgumentChecker(officeSourceContext);

        // Load in-line configured spring dependencies
        officeArchitect.addManagedFunctionAugmentor((context) -> {
                    ManagedFunctionType<?, ?> functionType = context.getManagedFunctionType();
                    for (ManagedFunctionObjectType<?> functionParameterType : functionType.getObjectTypes()) {
                        Class<?> objectType = functionParameterType.getObjectType();

                        // Obtain the method parameter annotation
                        MethodParameterAnnotation parameterAnnotation = functionParameterType.getAnnotation(MethodParameterAnnotation.class);
                        if (parameterAnnotation != null) {

                            // Obtain the method details for the parameter
                            Method method = parameterAnnotation.getMethod();
                            int parameterIndex = parameterAnnotation.getParameterIndex();

                            // Determine if in-line configuration of dependency
                            if (springArgumentChecker.isSpringArgument(objectType, functionParameterType.getAnnotations())) {
                                this.handleSpringArgument(objectType, method, parameterIndex, officeArchitect);
                            }
                        }
                    }
                });

        // Configure Office
        webArchitect.informOfficeArchitect();
    }

    private <S> void addOfficeManagedObjectSource(Class<S> objectType, ManagedObjectSource<?, ?> managedObjectSource, OfficeArchitect officeArchitect) {
        officeArchitect.addOfficeManagedObjectSource(objectType.getSimpleName(), managedObjectSource)
                .addOfficeManagedObject(objectType.getSimpleName(), ManagedObjectScope.THREAD);
    }

    private void handleSpringArgument(Class<?> objectType, Method method, int parameterIndex, OfficeArchitect officeArchitect) {

        // Determine the binding name and qualifier
        String bindAndQualifier = SpringTypeQualifierInterrogator.getSpringTypeQualifier(method, parameterIndex);

        // Set up the spring argument managed object
        this.springArguments.computeIfAbsent(bindAndQualifier, (key) -> {

            // Add the argument object
            OfficeManagedObject argumentObject = officeArchitect.addOfficeManagedObjectSource(bindAndQualifier, new SpringArgumentManagedObjectSource(objectType, method, parameterIndex))
                    .addOfficeManagedObject(bindAndQualifier, ManagedObjectScope.THREAD);

            // Add qualifier
            argumentObject.addTypeQualification(bindAndQualifier, objectType.getName());

            // Return the argument object
            return argumentObject;
        });
    }

}
