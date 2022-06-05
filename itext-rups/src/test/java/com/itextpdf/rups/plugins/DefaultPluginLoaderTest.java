package com.itextpdf.rups.plugins;

import com.itextpdf.rups.api.RupsPluginContext;
import com.itextpdf.test.annotations.type.UnitTest;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import org.junit.Test;
import org.junit.experimental.categories.Category;

@Category(UnitTest.class)
public class DefaultPluginLoaderTest {

    @Test
    public void test()
            throws ClassNotFoundException, InvocationTargetException, InstantiationException, IllegalAccessException {
        /**IPluginLoader pluginLoader = new DefaultPluginLoader();

        File[] files = pluginLoader.fetchPlugins(
                new File(
                        "C:\\Users\\michael.demey\\.m2\\repository\\com\\itextpdf\\rups-pdfrender\\1.0-SNAPSHOT"
                )
        );

        pluginLoader.loadPlugins(files, new RupsPluginContext());*/
    }
}
