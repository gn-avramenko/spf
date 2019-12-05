/*****************************************************************
 * Gridnine AB http://www.gridnine.com
 * Project: SPF
 *****************************************************************/
package com.gridnine.spf.meta;

import java.util.ArrayList;
import java.util.List;

public class SpfExtension {
    private String pointId;

    private final List<SpfExtensionParameter> parameters = new ArrayList<>();

    public String getPointId() {
        return pointId;
    }

    void setPointId(String pointId) {
        this.pointId = pointId;
    }

    public List<SpfExtensionParameter> getParameters() {
        return parameters;
    }
}
