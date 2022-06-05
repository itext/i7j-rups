package com.itextpdf.rups.plugins;

import com.itextpdf.rups.api.RupsPluginContext;

import java.io.File;
import java.lang.reflect.InvocationTargetException;

public interface IPluginLoader {

    File[] fetchPlugins(File pluginDirectory);

    boolean loadPlugins(File[] plugins, RupsPluginContext pluginContext)
            throws ClassNotFoundException, InvocationTargetException, InstantiationException, IllegalAccessException;
}
