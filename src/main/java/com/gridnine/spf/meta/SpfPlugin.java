package com.gridnine.spf.meta;

import java.util.ArrayList;
import java.util.List;

public class SpfPlugin {
    private String id;

    private final List<SpfLibDependency> libsDependencies = new ArrayList<>();

    private final List<SpfPluginDependency> pluginsDependencies = new ArrayList<>();

    private final List<SpfExtension> extensions = new ArrayList<>();

    private final List<SpfExtensionPoint> extensionPoints = new ArrayList<>();

    public String getId() {
        return id;
    }

    void setId(String id) {
        this.id = id;
    }

    public List<SpfExtension> getExtensions() {
        return extensions;
    }

    public List<SpfExtensionPoint> getExtensionPoints() {
        return extensionPoints;
    }

    public List<SpfLibDependency> getLibsDependencies() {
        return libsDependencies;
    }

    public List<SpfPluginDependency> getPluginsDependencies() {
        return pluginsDependencies;
    }
}
