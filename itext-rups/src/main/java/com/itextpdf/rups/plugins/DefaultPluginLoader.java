package com.itextpdf.rups.plugins;

import com.itextpdf.rups.api.RupsPluginContext;
import com.itextpdf.rups.api.IRupsPlugin;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;
import java.util.jar.Attributes;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

public class DefaultPluginLoader implements IPluginLoader {
    @Override
    public File[] fetchPlugins(File pluginDirectory) {
        FileFilter pluginFilter = new RupsPluginFileFilter();

        if (!pluginDirectory.isDirectory()) {
            if (pluginFilter.accept(pluginDirectory)) {
                return new File[] {pluginDirectory};
            } else {
                return new File[0];
            }
        }

        return pluginDirectory.listFiles(pluginFilter);
    }

    @Override
    public boolean loadPlugins(File[] plugins, RupsPluginContext pluginContext)
            throws ClassNotFoundException, InvocationTargetException, InstantiationException, IllegalAccessException {

        List<Pair<URL, String>> values = convertFileArrayToValuePair(plugins);

        List<URL> urls = new ArrayList<>();
        List<String> strings = new ArrayList<>();

        for (Pair<URL, String> pair : values) {
            urls.add(pair.getT());
            strings.add(pair.getK());
        }

        URLClassLoader classLoader = new URLClassLoader(urls.toArray(new URL[urls.size()]),
                this.getClass().getClassLoader());

        Class<?> pluginClass = classLoader.loadClass(strings.get(0));
        Constructor<?>[] declaredConstructors = pluginClass.getDeclaredConstructors();
        IRupsPlugin o = (IRupsPlugin) declaredConstructors[0].newInstance();

        o.initialize(pluginContext);

        return false;
    }

    private List<Pair<URL, String>> convertFileArrayToValuePair(File[] files) {
        List<Pair<URL, String>> valuePairs = new ArrayList<>();

        for (File plugin : files) {
            URL url = null;

            try {
                url = plugin.toURL();
            } catch (MalformedURLException e) {
                continue;
            }

            String className = null;
            try {
                className = extractClassNameFromJarManifest(plugin);
            } catch (IOException e) {
                continue;
            }

            valuePairs.add(new Pair(url, className));
        }

        return valuePairs;
    }

    private String extractClassNameFromJarManifest(File plugin) throws IOException {
        Manifest manifest = new JarFile(plugin).getManifest();

        String string = (String) manifest.getMainAttributes().get(new Attributes.Name("rups-implementation"));

        return string;
    }

    private class Pair<T, K> {
        private T t;
        private K k;

        public Pair(T t, K k) {
            this.t = t;
            this.k = k;
        }

        public T getT() {
            return t;
        }

        public K getK() {
            return k;
        }
    }
}
