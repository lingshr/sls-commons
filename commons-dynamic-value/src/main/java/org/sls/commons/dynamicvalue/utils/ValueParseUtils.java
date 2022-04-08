package org.sls.commons.dynamicvalue.utils;

import com.alibaba.cloud.nacos.parser.NacosDataParserHandler;
import com.google.common.collect.Maps;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.springframework.core.env.CompositePropertySource;
import org.springframework.core.env.EnumerablePropertySource;
import org.springframework.core.env.PropertySource;

/**
 * @author shanlingshi
 */
public class ValueParseUtils {

    private static final NacosDataParserHandler DATA_PARSER_HANDLER = NacosDataParserHandler.getInstance();

    public static Map<String, Object> parseFileData(String file, String data) throws IOException {
        return parseData(file, data, parseExtension(file));
    }

    public static Map<String, Object> parseData(String file, String data, String extension) throws IOException {
        List<PropertySource<?>> propertySources = DATA_PARSER_HANDLER.parseNacosData(file, data, extension);
        Map<String, Object> result = Maps.newHashMap();

        for (PropertySource<?> propertySource : propertySources) {
            extract(propertySource, result);
        }
        return result;
    }

    public static String parseExtension(String file) {
        int splitAt = file.indexOf(".");

        if (splitAt < 0) {
            return "properties";
        }

        return file.substring(splitAt + 1);
    }

    private static void extract(PropertySource<?> parent, Map<String, Object> result) {
        if (parent instanceof CompositePropertySource) {
            try {
                List<PropertySource<?>> sources = new ArrayList<PropertySource<?>>();
                for (PropertySource<?> source : ((CompositePropertySource) parent)
                        .getPropertySources()) {
                    sources.add(0, source);
                }
                for (PropertySource<?> source : sources) {
                    extract(source, result);
                }
            } catch (Exception e) {
                return;
            }
        } else if (parent instanceof EnumerablePropertySource) {
            for (String key : ((EnumerablePropertySource<?>) parent).getPropertyNames()) {
                result.put(key, parent.getProperty(key));
            }
        }
    }

}
