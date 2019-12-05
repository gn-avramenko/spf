/*****************************************************************
 * Gridnine AB http://www.gridnine.com
 * Project: SPF
 *****************************************************************/
package com.gridnine.spf.meta;

public class SpfExtensionPointParameterDef {
    private String id;
    private SpfParameterMultiplicity multiplicity;

    public String getId() {
        return id;
    }

    void setId(String id) {
        this.id = id;
    }

    public SpfParameterMultiplicity getMultiplicity() {
        return multiplicity;
    }

    void setMultiplicity(SpfParameterMultiplicity multiplicity) {
        this.multiplicity = multiplicity;
    }
}
