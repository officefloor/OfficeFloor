package net.officefloor.spring.starter.rest.argument;

import net.officefloor.compile.spi.supplier.source.SupplierSourceContext;
import net.officefloor.compile.spi.supplier.source.impl.AbstractSupplierSource;
import org.springframework.beans.factory.annotation.AnnotatedBeanDefinition;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.core.type.AnnotationMetadata;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;

public class SpringBeanSupplierSource extends AbstractSupplierSource {

    private ConfigurableListableBeanFactory beanFactory;

    /**
     * Instantiate.
     *
     * @param beanFactory {@link ConfigurableListableBeanFactory}.
     */
    public SpringBeanSupplierSource(ConfigurableListableBeanFactory beanFactory) {
        this.beanFactory = beanFactory;
    }

    /*
     * ================= SupplierSource ================
     */

    @Override
    protected void loadSpecification(SpecificationContext context) {
        // No specification
    }

    @Override
    public void supply(SupplierSourceContext context) throws Exception {

        // Group bean names by (qualifier, type) to detect duplicates
        BiFunction<String, Class<?>, String> groupKey = (qualifier, type) -> ((qualifier != null) ? qualifier : "") + ":" + type.getName();
        Map<String, List<String>> groupedTypes = new LinkedHashMap<>();

        // Load all beans
        Map<String, SpringBeanReference> beanReferences = new LinkedHashMap<>();
        for (String beanName : this.beanFactory.getBeanDefinitionNames()) {

            // Obtain the bean type (ensuring available)
            Class<?> beanType = this.beanFactory.getType(beanName);
            if (beanType == null) {
                continue;
            }

            // Obtain the possible qualifier
            String qualifier = null;
            BeanDefinition beanDefinition = this.beanFactory.getBeanDefinition(beanName);
            if (beanDefinition instanceof AnnotatedBeanDefinition) {
                AnnotationMetadata metadata = ((AnnotatedBeanDefinition) beanDefinition).getMetadata();
                Map<String, Object> attributes = metadata.getAnnotationAttributes(Qualifier.class.getName());
                qualifier = (attributes != null) ? (String) attributes.get("value") : null;
            }

            // Load the bean reference
            beanReferences.put(beanName, new SpringBeanReference(qualifier, beanType));

            // Look for duplicate types
            String typeKey = groupKey.apply(qualifier, beanType);
            groupedTypes.computeIfAbsent(typeKey, (key) -> new ArrayList<>()).add(beanName);
        }

        // Register beans
        for (String beanName : beanReferences.keySet()) {
            SpringBeanReference bean = beanReferences.get(beanName);

            // Determine the qualifier (use bean name if same type)
            String typeKey = groupKey.apply(bean.qualifier, bean.type);
            boolean isDuplicate = groupedTypes.get(typeKey).size() > 1;
            String effectiveQualifier = isDuplicate ? beanName : bean.qualifier;

            // Register the bean
            context.addManagedObjectSource(effectiveQualifier, bean.type,
                    new SpringBeanManagedObjectSource(beanName, bean.type, this.beanFactory));
        }
    }

    @Override
    public void terminate() {
        // Spring will terminate itself
    }

    private static class SpringBeanReference {
        private final String qualifier;
        private final Class<?> type;
        private SpringBeanReference(String qualifier, Class<?> type) {
            this.qualifier = qualifier;
            this.type = type;
        }
    }

}
