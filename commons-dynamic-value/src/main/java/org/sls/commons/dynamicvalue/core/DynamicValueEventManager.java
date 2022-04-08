package org.sls.commons.dynamicvalue.core;

import com.google.common.eventbus.AsyncEventBus;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class DynamicValueEventManager {

    public final ExecutorService executorService = Executors.newCachedThreadPool();

    private final AsyncEventBus eventBus = new AsyncEventBus(executorService);

    public void publish(RefreshEvent event) {
        eventBus.post(event);
    }

    public void register(DynamicValueChangeListener changeListener) {
        this.eventBus.register(changeListener);
    }

    public static class RefreshEvent {

        private final String group;
        private final String dataId;
        private final String configInfo;

        public RefreshEvent(String group, String dataId, String configInfo) {
            this.group = group;
            this.dataId = dataId;
            this.configInfo = configInfo;
        }

        public String getGroup() {
            return group;
        }

        public String getDataId() {
            return dataId;
        }

        public String getConfigInfo() {
            return configInfo;
        }
    }
}
