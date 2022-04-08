package org.sls.commons.dynamicvalue.core;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sls.commons.dynamicvalue.utils.ValueParseUtils;
import org.springframework.beans.SimpleTypeConverter;
import org.springframework.beans.factory.config.DependencyDescriptor;
import org.springframework.core.convert.ConversionService;

/**
 * @author shanlingshi
 */
public class DynamicValueListenerHolder {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    private static final DynamicValueListenerHolder INSTANCE = new DynamicValueListenerHolder();

    private SimpleTypeConverter simpleTypeConverter = new SimpleTypeConverter();

    private DynamicValueListenerHolder() {
    }

    public static DynamicValueListenerHolder getInstance() {
        return INSTANCE;
    }

    public void init(ConversionService conversionService) {
        simpleTypeConverter.setConversionService(conversionService);
    }

    private final Multimap<String, DynamicValueRefreshListener> valueListenerMap = ArrayListMultimap.create();
    private final Multimap<String, DynamicFileRefreshListener> fileListenerMap = ArrayListMultimap.create();

    public void register(String group, String file, Object bean, Method method) {
        this.fileListenerMap.put(fileKey(group, file), value -> {
            try {
                logger.info("receive new file content, file:{}, content:{}", file, value);
                Class<?>[] parameterTypes = method.getParameterTypes();
                if (parameterTypes.length != 1) {
                    throw new IllegalArgumentException("param count error, must 1");
                }

                Class<?> parameterType = parameterTypes[0];
                if (!String.class.isAssignableFrom(parameterType)
                        && !Map.class.isAssignableFrom(parameterType)) {
                    throw new IllegalArgumentException("param type error, must String or Map");
                }

                if (String.class.isAssignableFrom(parameterType)) {
                    method.invoke(bean, value);
                } else {
                    method.invoke(bean, ValueParseUtils.parseFileData(file, value));
                }
            } catch (Exception e) {
                logger.error("refresh new file content error, key:{}, value:{}", value, e);
            }
        });
    }

    private String fileKey(String group, String file) {
        return group + "@" + file;
    }

    public void register(String key, Object bean, Field field) {
        this.valueListenerMap.put(key, value -> {
            try {
                logger.info("receive new value, key:{}, value:{}", key, value);
                DependencyDescriptor descriptor = new DependencyDescriptor(field, true);
//                Object obj = DynamicValueTypeParser.get().convertIfNecessary(value, field.getType());
                Object obj = simpleTypeConverter
                        .convertIfNecessary(value, field.getType(), descriptor.getTypeDescriptor());
                boolean accessibleBefore = field.isAccessible();
                field.setAccessible(true);
                field.set(bean, obj);
                field.setAccessible(accessibleBefore);
            } catch (Exception e) {
                logger.error("value refresh error, key:{}, value:{}", value, e);
            }
        });
    }

    public Collection<DynamicValueRefreshListener> getValueTarget(String key) {
        return valueListenerMap.get(key);
    }

    public Collection<DynamicFileRefreshListener> getFileTarget(String group, String file) {
        return fileListenerMap.get(fileKey(group, file));
    }
}
