package org.sls.commons.dynamicvalue.core;

import com.alibaba.cloud.nacos.NacosConfigManager;
import com.alibaba.cloud.nacos.NacosPropertySourceRepository;
import com.alibaba.cloud.nacos.client.NacosPropertySource;
import com.alibaba.nacos.api.config.listener.Listener;
import com.alibaba.nacos.api.exception.NacosException;
import com.google.common.util.concurrent.MoreExecutors;
import org.sls.commons.dynamicvalue.core.DynamicValueEventManager.RefreshEvent;
import java.util.concurrent.Executor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author shanlingshi
 */
public class DynamicValueNacosRegistry {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    private DynamicValueEventManager eventManager;
    private NacosConfigManager nacosConfigManager;

    public DynamicValueNacosRegistry(DynamicValueEventManager eventManager,
            NacosConfigManager nacosConfigManager) {
        this.eventManager = eventManager;
        this.nacosConfigManager = nacosConfigManager;
    }

    public void registerListener() {
        for (NacosPropertySource nacosPropertySource : NacosPropertySourceRepository.getAll()) {
            String group = nacosPropertySource.getGroup();
            String dataId = nacosPropertySource.getDataId();

            try {
                nacosConfigManager.getConfigService().addListener(dataId,
                        group, new Listener() {
                            @Override
                            public Executor getExecutor() {
                                return MoreExecutors.directExecutor();
                            }

                            @Override
                            public void receiveConfigInfo(String configInfo) {
                                eventManager.publish(new RefreshEvent(group, dataId, configInfo));
                            }
                        });
            } catch (NacosException e) {
                logger.error("Nacos dynamic value listener register error, group:{}, dataid:{}", group, dataId, e);
            }
        }

        init();
    }

    private void init() {
        for (NacosPropertySource nacosPropertySource : NacosPropertySourceRepository.getAll()) {
            String group = nacosPropertySource.getGroup();
            String dataId = nacosPropertySource.getDataId();

            try {
                String config = nacosConfigManager.getConfigService().getConfig(dataId, group, 500);
                eventManager.publish(new RefreshEvent(group, dataId, config));
            } catch (NacosException e) {
                logger.error("Nacos init value listener error, group:{}, dataid:{}", group, dataId, e);
            }
        }
    }
}
