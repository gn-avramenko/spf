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
        for (int i = 0; i < plugins.size(); i++) {
            for (int j = i + 1; j < plugins.size(); j++) {
                if (isDependent(plugins.get(i),
                        plugins.get(j))) {
                    Collections.swap(plugins, i, j);
                    i = -1;
                    break;
                }
            }
        }
        extensions.clear();
        plugins.forEach(pl ->
            pl.getExtensions().forEach(ext ->{
                String pointId = ext.getPointId();
                extensions.computeIfAbsent(pointId, k -> new ArrayList<>()).add(ext);
            })
        );
    }

    private boolean isDependent(SpfPlugin plug1, SpfPlugin plug2) {
        Set<String> plug1Depths = new HashSet<>();
        collectDependencies(plug1Depths, plug1);
        return plug1Depths.contains(plug2.getId());
    }

    private void collectDependencies(Set<String> depth, SpfPlugin plug) {
        plug.getPluginsDependencies().forEach(it ->{
            if(depth.add(it.getPluginId())){
                collectDependencies(depth, plugins.stream().filter(p -> p.getId().equals(it.getPluginId())).findFirst().get());
            }
        });
    }

}
