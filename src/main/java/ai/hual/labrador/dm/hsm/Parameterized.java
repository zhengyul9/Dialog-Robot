package ai.hual.labrador.dm.hsm;

import ai.hual.labrador.dialog.AccessorRepository;
import ai.hual.labrador.dm.ContextedString;
import ai.hual.labrador.exceptions.DMException;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public class Parameterized {

    private AccessorRepository accessorRepository;

    public final void setUp(String slotName,
                            Map<String, ContextedString> params, AccessorRepository accessorRepository) {
        // find all fields in class and subclasses with type ContextedString and annotation Param.
        Class<?> clazz = getClass();
        while (!clazz.equals(Object.class)) {
            for (Field field : clazz.getDeclaredFields()) {
                if (field.getType().isAssignableFrom(ContextedString.class)) {
                    Param paramAnnotation = field.getAnnotation(Param.class);
                    if (paramAnnotation != null) {
                        setFieldWithContextedString(field, paramAnnotation, params, slotName);
                    }
                }
            }
            clazz = clazz.getSuperclass();
        }

        // set accessor repository
        this.accessorRepository = accessorRepository;
    }

    protected AccessorRepository getAccessorRepository() {
        return accessorRepository;
    }

    public final void setUp(Map<String, ContextedString> params, AccessorRepository accessorRepository) {
        setUp(null, params, accessorRepository);
    }

    private void setFieldWithContextedString(Field field, Param paramAnnotation, Map<String, ContextedString> params,
                                             String slotName) {
        String key = Optional.of(paramAnnotation.key()).filter(k -> !k.isEmpty()).orElse(field.getName());
        ContextedString param = params.get(key);

        if (param == null) {
            String defaultValue = paramAnnotation.defaultValue();
            param = defaultValue.isEmpty() ? null : new ContextedString(defaultValue);
        }

        // if null but required, throw exception
        if (param == null && paramAnnotation.required()) {
            throw new DMException(String.format("Missing required param %s for %s",
                    key, getClass().getSimpleName()));
        }

        // if not null and not in range, throw exception
        if (param != null && paramAnnotation.range().length > 0) {
            String paramStr = param.getStr();
            if (Arrays.stream(paramAnnotation.range()).noneMatch(x -> Objects.equals(x, paramStr))) {
                throw new DMException(String.format("Param %s for %s can only be one of %s",
                        key, getClass().getSimpleName(), Arrays.toString(paramAnnotation.range())));
            }
        }
        try {
            field.setAccessible(true);
            field.set(this, param);
        } catch (IllegalAccessException e) {
            throw new DMException(String.format("Unable to set field %s with contexted string %s for %s",
                    field.getName(), param == null ? "null" : param.getStr(), getClass().getSimpleName()));
        }
    }

}
