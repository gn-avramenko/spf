/*****************************************************************
 * Gridnine AB http://www.gridnine.com
 * Project: SPF
 *****************************************************************/
package com.gridnine.spf.test;

import com.gridnine.spf.meta.SpfExtension;
import com.gridnine.spf.meta.SpfExtensionParameter;
import com.gridnine.spf.meta.SpfPlugin;
import com.gridnine.spf.meta.SpfPluginsRegistry;
import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

public class ParserTest {

    @Test
    public void tesParser(){
        SpfPluginsRegistry registry = new SpfPluginsRegistry();
        registry.initRegistry(Arrays.asList(getClass().getClassLoader().getResource("testdata/plugins/com.gridnine.spf.test.plugin1/plugin.xml"),
                getClass().getClassLoader().getResource("testdata/plugins/com.gridnine.spf.test.plugin2/plugin.xml") ));
        Assert.assertEquals(2, registry.getPlugins().size());
        Assert.assertEquals("com.gridnine.spf.test.plugin1", registry.getPlugins().get(0).getId());
        Assert.assertEquals("com.gridnine.spf.test.plugin2", registry.getPlugins().get(1).getId());
        List<SpfExtension> extensions = registry.getExtensions("extension-point-1");
        Assert.assertEquals(2, extensions.size());
        {
            SpfExtension extension = extensions.get(0);
            Assert.assertEquals("extension-point-1", extension.getPointId());
            Assert.assertEquals(2, extension.getParameters().size());

            {
                SpfExtensionParameter param = extension.getParameters().get(0);
                Assert.assertEquals("class", param.getId());
                Assert.assertEquals("cls1", param.getValue());
            }
            {
                SpfExtensionParameter param = extension.getParameters().get(1);
                Assert.assertEquals("location", param.getId());
                Assert.assertEquals("loc1", param.getValue());
            }

        }
        {
            SpfExtension extension = extensions.get(1);
            Assert.assertEquals(2, extension.getParameters().size());
            {
                SpfExtensionParameter param = extension.getParameters().get(0);
                Assert.assertEquals("class", param.getId());
                Assert.assertEquals("cls2", param.getValue());
            }
            {
                SpfExtensionParameter param = extension.getParameters().get(1);
                Assert.assertEquals("location", param.getId());
                Assert.assertEquals("loc2", param.getValue());
            }

        }
        SpfPlugin spfPlugin = registry.getPlugins().get(0);
        Assert.assertEquals(1, spfPlugin.getParameters().size());
        Assert.assertEquals("type", spfPlugin.getParameters().get(0).getId());
        Assert.assertEquals("SERVER", spfPlugin.getParameters().get(0).getValue());
    }
}

