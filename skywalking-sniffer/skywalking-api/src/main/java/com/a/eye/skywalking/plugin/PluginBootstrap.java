package com.a.eye.skywalking.plugin;

import com.a.eye.skywalking.logging.LogManager;
import com.a.eye.skywalking.logging.EasyLogger;
import net.bytebuddy.pool.TypePool;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class PluginBootstrap {
    private static EasyLogger easyLogger = LogManager.getLogger(PluginBootstrap.class);

    public static TypePool CLASS_TYPE_POOL = null;

    public List<AbstractClassEnhancePluginDefine> loadPlugins() {
        CLASS_TYPE_POOL = TypePool.Default.ofClassPath();

        PluginResourcesResolver resolver = new PluginResourcesResolver();
        List<URL> resources = resolver.getResources();

        if (resources == null || resources.size() == 0) {
            easyLogger.info("no plugin files (skywalking-plugin.properties) found, continue to start application.");
            return new ArrayList<AbstractClassEnhancePluginDefine>();
        }

        for (URL pluginUrl : resources) {
            try {
                PluginCfg.CFG.load(pluginUrl.openStream());
            } catch (Throwable t) {
                easyLogger.error("plugin [{}] init failure.", new Object[] {pluginUrl}, t);
            }
        }

        List<String> pluginClassList = PluginCfg.CFG.getPluginClassList();

        List<AbstractClassEnhancePluginDefine> plugins = new ArrayList<AbstractClassEnhancePluginDefine>();
        for (String pluginClassName : pluginClassList) {
            try {
                easyLogger.debug("loading plugin class {}.", pluginClassName);
                AbstractClassEnhancePluginDefine plugin =
                        (AbstractClassEnhancePluginDefine) Class.forName(pluginClassName).newInstance();
                plugins.add(plugin);
            } catch (Throwable t) {
                easyLogger.error("loade plugin [{}] failure.", new Object[] {pluginClassName}, t);
            }
        }

        return plugins;

    }


}
