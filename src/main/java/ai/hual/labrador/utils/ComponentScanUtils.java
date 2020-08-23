package ai.hual.labrador.utils;

import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import javax.annotation.Nullable;
import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ComponentScanUtils {

    public static final String COMPONENT_DELIMITER = ",";

    public static <T> List<T> scan(String packageName, Class<T> clazz) {
        return scan((Stream<String>) null, new String[]{packageName}, clazz);
    }

    public static <T> List<T> scan(String[] packageNames, Class<T> clazz) {
        return scan((Stream<String>) null, packageNames, clazz);
    }

    public static <T> List<T> scan(String components, String packageName, Class<T> clazz) {
        return scan(components, new String[]{packageName}, clazz);
    }

    public static <T> List<T> scan(String components, String[] packageNames, Class<T> clazz) {
        return scan(Arrays.stream(components.split(COMPONENT_DELIMITER)), packageNames, clazz);
    }

    public static <T> List<T> scan(List<String> components, String packageName, Class<T> clazz) {
        return scan(components, new String[]{packageName}, clazz);
    }

    public static <T> List<T> scan(List<String> components, String[] packageNames, Class<T> clazz) {
        return scan(components.stream(), packageNames, clazz);
    }

    public static <T> List<T> scan(Stream<String> components, String packageName, Class<T> clazz) {
        return scan(components, new String[]{packageName}, clazz);
    }

    public static <T> List<T> scan(Stream<String> components, String[] packageNames, Class<T> clazz) {
        AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext();
        context.refresh();
        return scan(context, components, packageNames, clazz);
    }

    private static <T> List<T> scan(AnnotationConfigApplicationContext context,
                                    Stream<String> components, String[] packageNames, Class<T> clazz) {
        return scan(context, null, null, components, packageNames, clazz);
    }


    public static ComponentScanHelper withBean(String beanName, Object bean) {
        return new ComponentScanHelper().withBean(beanName, bean);
    }

    public static ComponentScanHelper filterAnnotation(Class<? extends Annotation> annotation) {
        return new ComponentScanHelper().filterAnnotation(annotation);
    }

    public static ComponentScanHelper filterExtends(Class<?> superClass) {
        return new ComponentScanHelper().filterExtends(superClass);
    }

    public static class ComponentScanHelper {

        private AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext();
        private Class<? extends Annotation> filterAnnotation;
        private Class<?> filterExtends;

        ComponentScanHelper() {
            context.refresh();
        }

        public ComponentScanHelper withBean(String beanName, Object bean) {
            context.getBeanFactory().registerSingleton(beanName, bean);
            return this;
        }

        public ComponentScanHelper filterAnnotation(Class<? extends Annotation> annotation) {
            this.filterAnnotation = annotation;
            return this;
        }

        public ComponentScanHelper filterExtends(Class<?> superClass) {
            this.filterExtends = superClass;
            return this;
        }

        public <T> List<T> scan(String packageName, Class<T> clazz) {
            return scan((Stream<String>) null, new String[]{packageName}, clazz);
        }

        public <T> List<T> scan(String[] packageNames, Class<T> clazz) {
            return scan((Stream<String>) null, packageNames, clazz);
        }

        public <T> List<T> scan(List<String> components, String[] packageNames, Class<T> clazz) {
            return scan(components.stream(), packageNames, clazz);
        }

        public <T> List<T> scan(String components, String[] packageNames, Class<T> clazz) {
            return scan(Arrays.stream(components.split(COMPONENT_DELIMITER)), packageNames, clazz);
        }

        public <T> List<T> scan(String components, String packageName, Class<T> clazz) {
            return scan(components, new String[]{packageName}, clazz);
        }

        public <T> List<T> scan(Stream<String> components, String[] packageNames, Class<T> clazz) {
            return ComponentScanUtils.scan(context, filterAnnotation, filterExtends, components, packageNames, clazz);
        }

    }

    private static <T> List<T> scan(AnnotationConfigApplicationContext context,
                                    @Nullable Class<? extends Annotation> filterAnnotation,
                                    @Nullable Class<?> filterSubClassOf,
                                    @Nullable Stream<String> components, String[] packageNames, Class<T> clazz) {
        for (String packageName : packageNames) {
            context.scan(packageName);
        }
        Stream<T> beanStream = (components == null) ? context.getBeansOfType(clazz).values().stream() :
                components.map(String::trim).filter(x -> !x.isEmpty()).map(name -> context.getBean(name, clazz));
        return beanStream
                .filter(bean -> filterAnnotation == null || bean.getClass().isAnnotationPresent(filterAnnotation))
                .filter(bean -> filterSubClassOf == null || filterSubClassOf.isAssignableFrom(bean.getClass()))
                .collect(Collectors.toList());
    }

}
