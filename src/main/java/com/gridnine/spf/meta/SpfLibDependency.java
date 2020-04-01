/*****************************************************************
 * Gridnine AB http://www.gridnine.com
 * Project: xTrip-3
 *****************************************************************/
package com.gridnine.spf.meta;

@SuppressWarnings("unused")
public class SpfLibDependency {
    private String group;

    private String name;

    private String version;

    public String getGroup() {
        return group;
    }

    void setGroup(String group) {
        this.group = group;
    }

    public String getName() {
        return name;
    }

    void setName(String name) {
        this.name = name;
    }

    public String getVersion() {
        return version;
    }

    void setVersion(String version) {
        this.version = version;
    }
}
