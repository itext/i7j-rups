package com.itextpdf.rups.view;

import com.itextpdf.rups.RupsConfiguration;
import com.itextpdf.rups.view.icons.FrameIconUtil;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.util.Locale;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.WindowConstants;

/**
 * The window responsible for holding the UI to set the preferences.
 */
public class PreferencesWindow {

    private JDialog jDialog;

    private GridBagLayout gridBagLayout;
    private GridBagConstraints left;
    private GridBagConstraints right;

    private JPanel visualPanel;
    private JScrollPane generalSettingsScrollPane;

    // Fields to reset
    private JCheckBox openDuplicateFiles;
    private JTextField pathField;
    private JLabel restartLabel;
    private JComboBox<String> localeBox;

    public PreferencesWindow() {
        initializeJDialog();
        initializeLayout();

        createGeneralSettingsTab();
        createVisualSettingsTab();
        createTabbedPane();
        createSaveCancelResetSection();

        completeJDialogCreation();
    }

    private void initializeJDialog() {
        this.jDialog = new JDialog();

        this.jDialog.setTitle(Language.PREFERENCES.getString());
        this.jDialog.setIconImages(FrameIconUtil.loadFrameIcons());
        this.jDialog.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        this.jDialog.setModal(true);
        this.jDialog.setLayout(new BorderLayout());
        this.jDialog.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                RupsConfiguration.INSTANCE.cancelTemporaryChanges();
                resetView();
            }
        });
    }

    private void initializeLayout() {
        this.gridBagLayout = new GridBagLayout();

        this.left = new GridBagConstraints();
        this.left.anchor = GridBagConstraints.EAST;

        this.right = new GridBagConstraints();
        this.right.weightx = 2.0;
        this.right.fill = GridBagConstraints.HORIZONTAL;
        this.right.gridwidth = GridBagConstraints.REMAINDER;
    }

    private void createGeneralSettingsTab() {
        this.pathField = new JTextField(RupsConfiguration.INSTANCE.getHomeFolder().getPath(), 30);
        JLabel pathLabel = new JLabel(Language.PREFERENCES_OPEN_FOLDER.getString());
        pathLabel.setLabelFor(this.pathField);

        JButton pathChooser = new JButton(Language.PREFERENCES_SELECT_NEW_DEFAULT_FOLDER.getString());
        pathChooser.addActionListener(e -> {
            JFileChooser fileChooser = new JFileChooser(RupsConfiguration.INSTANCE.getHomeFolder());
            fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            int choice = fileChooser.showOpenDialog(jDialog);

            if (choice == JFileChooser.APPROVE_OPTION) {
                String path = fileChooser.getSelectedFile().getPath();
                this.pathField.setText(path);
                RupsConfiguration.INSTANCE.setHomeFolder(path);
            }
        });

        JPanel fieldsPanel = new JPanel();
        fieldsPanel.add(this.pathField);
        fieldsPanel.add(pathChooser);

        this.openDuplicateFiles = new JCheckBox("", RupsConfiguration.INSTANCE.canOpenDuplicateFiles());
        this.openDuplicateFiles.addActionListener(
                e -> RupsConfiguration.INSTANCE.setOpenDuplicateFiles(((JCheckBox) e.getSource()).isSelected())
        );
        JLabel openDuplicateFilesLabel = new JLabel(Language.PREFERENCES_ALLOW_DUPLICATE_FILES.getString());
        openDuplicateFilesLabel.setLabelFor(this.openDuplicateFiles);

        JPanel generalSettingsPanel = new JPanel();
        generalSettingsPanel.setLayout(this.gridBagLayout);

        generalSettingsPanel.add(pathLabel, this.left);
        generalSettingsPanel.add(fieldsPanel, this.right);

        generalSettingsPanel.add(openDuplicateFilesLabel, this.left);
        generalSettingsPanel.add(this.openDuplicateFiles, this.right);

        this.generalSettingsScrollPane = new JScrollPane(generalSettingsPanel);
    }

    private void createVisualSettingsTab() {
        this.localeBox = new JComboBox<>();
        this.localeBox.addItem("nl-NL");
        this.localeBox.addItem("en-US");
        this.localeBox.setSelectedItem(RupsConfiguration.INSTANCE.getUserLocale().toLanguageTag());
        final JLabel localeLabel = new JLabel(Language.LOCALE.getString());
        localeLabel.setLabelFor(localeBox);

        this.restartLabel = new JLabel(Language.PREFERENCES_NEED_RESTART.getString());
        this.restartLabel.setVisible(false);
        this.restartLabel.setLabelFor(localeBox);

        this.localeBox.addActionListener(e -> {
            Object selectedItem = localeBox.getSelectedItem();
            String selectedString = (String) selectedItem;
            RupsConfiguration.INSTANCE.setUserLocale(Locale.forLanguageTag(selectedString));
            this.restartLabel.setVisible(true);
        });

        this.visualPanel = new JPanel();
        this.visualPanel.setLayout(this.gridBagLayout);

        this.visualPanel.add(localeLabel, this.left);
        this.visualPanel.add(this.localeBox, this.right);
        this.visualPanel.add(this.restartLabel, this.right);
    }

    private void createTabbedPane() {
        final JTabbedPane tabbedPane = new JTabbedPane();

        tabbedPane.add(Language.PREFERENCES_RUPS_SETTINGS.getString(), this.generalSettingsScrollPane);
        tabbedPane.add(Language.PREFERENCES_VISUAL_SETTINGS.getString(), this.visualPanel);

        this.jDialog.add(tabbedPane, BorderLayout.CENTER);
    }

    private void createSaveCancelResetSection() {
        JPanel buttons = new JPanel();

        JButton save = new JButton(Language.SAVE.getString());
        save.addActionListener(e -> {
            RupsConfiguration.INSTANCE.saveConfiguration();
            resetView();
            this.jDialog.dispose();
        });
        buttons.add(save);

        JButton cancel = new JButton(Language.DIALOG_CANCEL.getString());
        cancel.addActionListener(e -> {
            if (RupsConfiguration.INSTANCE.hasUnsavedChanges()) {
                int choice = JOptionPane.showConfirmDialog(jDialog,
                        Language.SAVE_UNSAVED_CHANGES.getString());
                if (choice == JOptionPane.OK_OPTION) {
                    RupsConfiguration.INSTANCE.cancelTemporaryChanges();
                    this.jDialog.dispose();
                }
            } else {
                this.jDialog.dispose();
            }
        });
        buttons.add(cancel);

        JButton reset = new JButton(Language.PREFERENCES_RESET_TO_DEFAULTS.getString());

        reset.addActionListener(e -> {
            int choice = JOptionPane.showConfirmDialog(jDialog,
                    Language.PREFERENCES_RESET_TO_DEFAULTS_CONFIRM.getString());
            if (choice == JOptionPane.OK_OPTION) {
                RupsConfiguration.INSTANCE.resetToDefaultProperties();
                resetView();
            }
        });
        buttons.add(reset);

        jDialog.add(buttons, BorderLayout.SOUTH);
    }

    private void completeJDialogCreation() {
        this.jDialog.pack();
        this.jDialog.setResizable(false);
    }

    private void resetView() {
        this.pathField.setText(RupsConfiguration.INSTANCE.getHomeFolder().getPath());
        this.openDuplicateFiles.setSelected(RupsConfiguration.INSTANCE.canOpenDuplicateFiles());
        this.localeBox.setSelectedItem(RupsConfiguration.INSTANCE.getUserLocale().toLanguageTag());
        this.restartLabel.setVisible(false);
    }

    public void show(Component component) {
        jDialog.setLocationRelativeTo(component);
        jDialog.setVisible(true);
    }
}
