package org.sls.commons.dynamicvalue.core;

import com.alibaba.cloud.nacos.NacosConfigManager;
import java.util.concurrent.atomic.AtomicBoolean;
import org.springframework.beans.BeansException;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.boot.convert.ApplicationConversionService;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ApplicationListener;

/**
 * @author shanlingshi
 */
public class DynamicValueCoreContext implements ApplicationListener<ApplicationReadyEvent>, ApplicationContextAware {

    private DynamicValueEventManager eventManager;
    private DynamicValueNacosRegistry nacosRegistry;
    private DynamicValueChangeListener changeListener;

    private AtomicBoolean ready = new AtomicBoolean(false);

    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {
        if (this.ready.compareAndSet(false, true)) {
            this.init();
            this.registerEventListener();
            this.registerNacosListener();
        }
    }

    private void registerEventListener() {
        this.eventManager.register(changeListener);
    }

    private void init() {
        DynamicValueListenerHolder.getInstance().init(ApplicationConversionService.getSharedInstance());
        eventManager = new DynamicValueEventManager();
        nacosRegistry = new DynamicValueNacosRegistry(eventManager,
                applicationContext.getBean(NacosConfigManager.class));
        changeListener = new DynamicValueChangeListener();
    }

    private void registerNacosListener() {
        nacosRegistry.registerListener();
    }

    private ApplicationContext applicationContext;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    public DynamicValueEventManager getEventManager() {
        return eventManager;
    }

    public DynamicValueNacosRegistry getNacosRegistry() {
        return nacosRegistry;
    }

    public DynamicValueChangeListener getChangeListener() {
        return changeListener;
    }
}
