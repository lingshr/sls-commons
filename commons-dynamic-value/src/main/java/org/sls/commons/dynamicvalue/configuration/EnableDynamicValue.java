package org.sls.commons.dynamicvalue.configuration;

import org.sls.commons.dynamicvalue.core.DynamicValueCoreContext;
import org.sls.commons.dynamicvalue.core.DynamicValueScanner;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.springframework.context.annotation.Import;

/**
 * @author shanlingshi
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
@Import({DynamicValueCoreContext.class, DynamicValueScanner.class})
public @interface EnableDynamicValue {

}
