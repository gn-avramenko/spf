/*****************************************************************
 * Gridnine AB http://www.gridnine.com
 * Project: SPF
 *****************************************************************/
package com.gridnine.spf.meta;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("unused")
public class SpfExtensionPoint {
    private String id;

    private final List<SpfExtensionPointParameterDef> parameters = new ArrayList<>();


    public String getId() {
        return id;
    }

    void setId(String id) {
        this.id = id;
    }

    public List<SpfExtensionPointParameterDef> getParameters() {
        return parameters;
    }
}
