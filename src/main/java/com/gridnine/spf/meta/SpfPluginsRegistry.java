/*****************************************************************
 * Gridnine AB http://www.gridnine.com
 * Project: SPF
 *****************************************************************/
package com.gridnine.spf.meta;

import java.net.URL;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class SpfPluginsRegistry {

    private final List<SpfPlugin> plugins = new ArrayList<>();

    private final Map<String, List<SpfExtension>> extensions = new HashMap<>();

    public List<SpfPlugin> getPlugins() {
        return plugins;
    }

    public List<SpfExtension> getExtensions(String pointId) {
        return extensions.get(pointId);
    }

    public void initRegistry(Collection<URL> descriptions) {
        initRegistry(descriptions, it -> true);
    }

    public void initRegistry(Collection<URL> descriptions, Predicate<SpfPlugin> filter) {
        plugins.clear();
        plugins.addAll(descriptions.stream().map(SpfPluginFileParser::parse).filter(filter).collect(Collectors.toList()));
        plugins.sort((plug1, plug2) -> {
                if(isDependent(plug1, plug2)){
                    return 1;
                } else if (isDependent(plug2, plug1)){
                    return -1;
                }
                return 0;
             }
        );
        extensions.clear();
        plugins.forEach(pl ->
            pl.getExtensions().forEach(ext ->{
                String pointId = ext.getPointId();
                extensions.computeIfAbsent(pointId, k -> new ArrayList<>()).add(ext);
            })
        );
    }

    private boolean isDependent(SpfPlugin plug1, SpfPlugin plug2) {
        return plug1.getPluginsDependencies().stream().anyMatch(it -> plug2.getId().equals(it.getPluginId()));
    }

}
