package org.sls.commons.dynamicvalue.core;

import com.google.common.eventbus.Subscribe;
import org.sls.commons.dynamicvalue.core.DynamicValueEventManager.RefreshEvent;
import org.sls.commons.dynamicvalue.utils.ValueParseUtils;
import java.io.IOException;
import java.util.Map;

/**
 * @author shanlingshi
 */
public class DynamicValueChangeListener {

    @Subscribe
    private void refresh(RefreshEvent refreshEvent) throws IOException {
        String dataId = refreshEvent.getDataId();

        triggerFileListener(refreshEvent.getGroup(), dataId, refreshEvent.getConfigInfo());
        triggerValueListener(ValueParseUtils.parseFileData(dataId, refreshEvent.getConfigInfo()));
    }

    private void triggerFileListener(String group, String dataId, String content) {
        for (DynamicFileRefreshListener listener : DynamicValueListenerHolder.getInstance()
                .getFileTarget(group, dataId)) {
            listener.refresh(content);
        }
    }

    private void triggerValueListener(Map<String, Object> dataMap) {
        dataMap.forEach((k, v) -> {
            for (DynamicValueRefreshListener listener : DynamicValueListenerHolder.getInstance().getValueTarget(k)) {
                listener.refresh(v);
            }
        });
    }
}
