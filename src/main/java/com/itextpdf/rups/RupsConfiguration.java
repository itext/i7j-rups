/*
    This file is part of the iText (R) project.
    Copyright (c) 1998-2023 Apryse Group NV
    Authors: Apryse Software.

    This program is free software; you can redistribute it and/or modify
    it under the terms of the GNU Affero General Public License version 3
    as published by the Free Software Foundation with the addition of the
    following permission added to Section 15 as permitted in Section 7(a):
    FOR ANY PART OF THE COVERED WORK IN WHICH THE COPYRIGHT IS OWNED BY
    APRYSE GROUP. APRYSE GROUP DISCLAIMS THE WARRANTY OF NON INFRINGEMENT
    OF THIRD PARTY RIGHTS

    This program is distributed in the hope that it will be useful, but
    WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
    or FITNESS FOR A PARTICULAR PURPOSE.
    See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License
    along with this program; if not, see http://www.gnu.org/licenses or write to
    the Free Software Foundation, Inc., 51 Franklin Street, Fifth Floor,
    Boston, MA, 02110-1301 USA, or download the license from the following URL:
    http://itextpdf.com/terms-of-use/

    The interactive user interfaces in modified source and object code versions
    of this program must display Appropriate Legal Notices, as required under
    Section 5 of the GNU Affero General Public License.

    In accordance with Section 7(b) of the GNU Affero General Public License,
    a covered work must retain the producer line in every PDF that is created
    or manipulated using iText.

    You can be released from the requirements of the license by purchasing
    a commercial license. Buying such a license is mandatory as soon as you
    develop commercial activities involving the iText software without
    disclosing the source code of your own applications.
    These activities include: offering paid services to customers as an ASP,
    serving PDFs on the fly in a web application, shipping iText with a closed
    source product.

    For more information, please contact iText Software Corp. at this
    address: sales@itextpdf.com
 */
package com.itextpdf.rups;

import com.itextpdf.rups.model.LoggerHelper;
import com.itextpdf.rups.view.Language;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Locale;
import java.util.Properties;
import java.util.Set;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;
import javax.swing.UIManager;
import javax.swing.WindowConstants;

/**
 * The access point for all the configuration that RUPS allows.
 *
 * <p>
 * On start up everything is loaded from the default.properties file included with this project. RupsConfiguration will
 * then check if these properties are available in the {@link java.util.prefs.Preferences Preferences}. If the
 * properties in default.properties are not available in the Preferences, RupsConfiguration will insert them.
 *
 * <p>
 * Whenever changes are made to the settings in RUPS, they are saved to a Properties instance. Callers of this code
 * need to make a call to {@link RupsConfiguration#saveConfiguration()} to persist the changes to the Preferences.
 */
public enum RupsConfiguration {
    INSTANCE;

    private static final String DEFAULT_CONFIG_PATH = "/config/default.properties";
    private static final String ICON_CONFIG_PATH = "/config/icon.properties";
    private static final String DEFAULT_HOME_VALUE = "home";
    private static final String CLOSE_OPERATION_KEY = "ui.closeoperation";
    private static final String DUPLICATE_OPEN_FILES_KEY = "rups.duplicatefiles";
    private static final String HOME_FOLDER_KEY = "user.home";
    private static final String LOCALE_KEY = "user.locale";
    private static final String LOOK_AND_FEEL_KEY = "ui.lookandfeel";
    private static final String ICON_KEY = "ui.lookandfeel";

    private final Preferences systemPreferences;
    private final Properties defaultProperties;
    private final Properties temporaryProperties;

    private final Properties iconProperties;


    RupsConfiguration() {
        this.defaultProperties = loadDefaultProperties();
        this.temporaryProperties = new Properties();
        this.systemPreferences = Preferences.userNodeForPackage(RupsConfiguration.class);
        this.iconProperties = loadIconProperties();

        initializeSystemDefaults(this.defaultProperties, this.systemPreferences);
    }

    /**
     * Are there any unsaved and non-persisted changes in the settings? To persist these, {@link
     * RupsConfiguration#saveConfiguration()}, to clear the unsaved changes, {@link
     * RupsConfiguration#cancelTemporaryChanges()}.
     *
     * @return boolean
     */
    public boolean hasUnsavedChanges() {
        return !this.temporaryProperties.keySet().isEmpty();
    }

    /**
     * Returns whether or not RUPS can open the same file twice.
     *
     * @return boolean indicating if we can open duplicate files
     */
    public boolean canOpenDuplicateFiles() {
        final String value = getValueFromSystemPreferences(DUPLICATE_OPEN_FILES_KEY);
        return Boolean.parseBoolean(value);
    }

    /**
     * Returns the closing operation for the RUPS instance. Default it is returning EXIT_ON_CLOSE, but
     * another value could be useful when embedding RUPS or calling it from a Java process.
     *
     * <p>
     * {@link WindowConstants} for possible values.
     *
     * @return int
     */
    public int getCloseOperation() {
        final String value = getValueFromSystemPreferences(CLOSE_OPERATION_KEY);
        final int closeOperation;

        switch (value) {
            default:
            case "exit":
                closeOperation = WindowConstants.EXIT_ON_CLOSE;
                break;
            case "dispose":
                closeOperation = WindowConstants.DISPOSE_ON_CLOSE;
                break;
            case "hide":
                closeOperation = WindowConstants.HIDE_ON_CLOSE;
                break;
            case "nothing":
                closeOperation = WindowConstants.DO_NOTHING_ON_CLOSE;
                break;
        }

        return closeOperation;
    }

    /**
     * Gets the folder to which we will open the file explorers. Default value is the value associated
     * with "user.home".
     *
     * @return String default folder to open
     */
    public File getHomeFolder() {
        final String value = getValueFromSystemPreferences(HOME_FOLDER_KEY, System.getProperty(HOME_FOLDER_KEY));
        return new File(value);
    }

    /**
     * Gets the Locale specified by the user.
     *
     * @return User Locale
     */
    public Locale getUserLocale() {
        final String value = getValueFromSystemPreferences(LOCALE_KEY);
        Locale locale = Locale.getDefault();

        if (value != null) {
            locale = Locale.forLanguageTag(value);

            if (locale == null) {
                locale = Locale.getDefault();
            }
        }

        return locale;
    }

    /**
     * Gets the set Look and Feel for the application.
     *
     * @return String
     */
    public String getLookAndFeel() {
        final String lookAndFeelName;
        final String value = getValueFromSystemPreferences(LOOK_AND_FEEL_KEY);

        switch (value) {
            default:
            case "system":
                lookAndFeelName = UIManager.getSystemLookAndFeelClassName();
                break;
            case "crossplatform":
                lookAndFeelName = UIManager.getCrossPlatformLookAndFeelClassName();
                break;
        }

        return lookAndFeelName;
    }

    public void setLookAndFeel(String lookAndFeel) {
        this.temporaryProperties.setProperty(LOOK_AND_FEEL_KEY, lookAndFeel);
    }

    public void setOpenDuplicateFiles(boolean value) {
        this.temporaryProperties.setProperty(DUPLICATE_OPEN_FILES_KEY, Boolean.toString(value));
    }

    /**
     * Sets the default folder to use in JFileChoosers.
     *
     * @param newDefaultFolder folder to use
     */
    public void setHomeFolder(String newDefaultFolder) {
        final String defaultHome = System.getProperty(HOME_FOLDER_KEY);
        if (newDefaultFolder == null || DEFAULT_HOME_VALUE.equals(newDefaultFolder)) {
            newDefaultFolder = defaultHome;
        } else {
            File valueDirectory = new File(newDefaultFolder);
            if (!valueDirectory.exists() || !valueDirectory.isDirectory()) {
                newDefaultFolder = defaultHome;
            }
        }

        this.temporaryProperties.setProperty(HOME_FOLDER_KEY, newDefaultFolder);
    }

    public void setUserLocale(Locale locale) {
        if (locale == null) {
            locale = Locale.getDefault();
        }

        this.temporaryProperties.setProperty(LOCALE_KEY, locale.toLanguageTag());
    }

    /**
     * Saves any unsaved changes. To cancel changes, see {@link RupsConfiguration#cancelTemporaryChanges()}.
     */
    public void saveConfiguration() {
        this.temporaryProperties.forEach((key, value) -> {
            this.systemPreferences.put((String) key, (String) value);
        });

        this.temporaryProperties.clear();
    }

    /**
     * Clears any unsaved changes. To save changes, see {@link RupsConfiguration#saveConfiguration()}.
     */
    public void cancelTemporaryChanges() {
        this.temporaryProperties.clear();
    }

    /**
     * Resets the settings in Preferences, not the local Properties object to the default.
     */
    public void resetToDefaultProperties() {
        this.defaultProperties.forEach((Object key, Object value) -> {
            this.systemPreferences.put((String) key, (String) value);
        });

        this.temporaryProperties.clear();
    }

    /**
     * Returns a Properties object containing the settings.
     *
     * @return Properties containing all the Preferences associated with RUPS.
     * @throws BackingStoreException in case of error accessing the Preferences
     */
    public Properties getCurrentState() throws BackingStoreException {
        String[] keys = this.systemPreferences.keys();
        Properties properties = new Properties();

        for (String key : keys) {
            properties.put(key, this.systemPreferences.get(key, ""));
        }

        return properties;
    }

    /**
     * Sets the Preferences to the values provided in the Properties object.
     *
     * @param properties Properties holding new values for Preferences
     */
    public void restore(Properties properties) {
        properties.forEach((key, value) -> {
            this.systemPreferences.put((String) key, (String) value);
        });
    }

    private Properties loadDefaultProperties() {
        final InputStream resourceAsStream = RupsConfiguration.class.getResourceAsStream(DEFAULT_CONFIG_PATH);
        final Properties properties = new Properties();

        if (resourceAsStream != null) {
            try {
                properties.load(resourceAsStream);
                resourceAsStream.close();
            } catch (IOException e) {
                LoggerHelper.error(Language.ERROR_LOADING_DEFAULT_SETTINGS.getString(), e, RupsConfiguration.class);
            }
        }

        return properties;
    }

    private Properties loadIconProperties() {
        final InputStream resourceAsStream = RupsConfiguration.class.getResourceAsStream(ICON_CONFIG_PATH);
        final Properties properties = new Properties();

        if (resourceAsStream != null) {
            try {
                properties.load(resourceAsStream);
                resourceAsStream.close();
            } catch (IOException e) {
                LoggerHelper.error(Language.ERROR_LOADING_ICON_DEFINITIONS.getString(), e, RupsConfiguration.class);
            }
        }

        return properties;
    }

    private void initializeSystemDefaults(Properties defaultProperties, Preferences systemPreferences) {
        try {
            final String[] keys = systemPreferences.keys();
            final Set<String> preferenceKeys = new HashSet<>(Arrays.asList(keys));

            for (final Object key : defaultProperties.keySet()) {
                final String property = (String) key;

                if (!preferenceKeys.contains(property)) {
                    systemPreferences.put(property, defaultProperties.getProperty(property));
                }
            }
        } catch (BackingStoreException e) {
            LoggerHelper.error(Language.ERROR_INITIALIZING_SETTINGS.getString(), RupsConfiguration.class);
        }
    }

    private String getValueFromSystemPreferences(String key, String defaultValue) {
        String value = getValueFromSystemPreferences(key);

        if (value == null || value.isEmpty()) {
            value = defaultValue;
        }

        return value;
    }

    private String getValueFromSystemPreferences(String key) {
        return this.systemPreferences.get(key, this.defaultProperties.getProperty(key));
    }

    public String getIconFor(String key){
        return this.iconProperties.getProperty(key, "default");
    }
}