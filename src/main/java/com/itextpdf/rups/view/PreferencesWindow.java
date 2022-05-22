package com.itextpdf.rups.view;

import com.itextpdf.rups.RupsConfiguration;
import com.itextpdf.rups.view.icons.FrameIconUtil;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
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

    private JFrame jFrame;

    public PreferencesWindow() {
        jFrame = new JFrame();
        jFrame.setTitle(Language.PREFERENCES.getString());
        jFrame.setIconImages(FrameIconUtil.loadFrameIcons());
        jFrame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        jFrame.setLayout(new BorderLayout());

        JTabbedPane tabbedPane = new JTabbedPane();

        JTextField pathField = new JTextField(RupsConfiguration.INSTANCE.getHomeFolder().getPath(), 30);
        JLabel pathLabel = new JLabel(Language.PREFERENCES_OPEN_FOLDER.getString());
        pathLabel.setLabelFor(pathField);

        JButton pathChooser = new JButton(Language.PREFERENCES_SELECT_NEW_DEFAULT_FOLDER.getString());
        pathChooser.addActionListener(e -> {
            JFileChooser fileChooser = new JFileChooser(RupsConfiguration.INSTANCE.getHomeFolder());
            fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            int choice = fileChooser.showOpenDialog(jFrame);

            if (choice == JFileChooser.APPROVE_OPTION) {
                String path = fileChooser.getSelectedFile().getPath();
                pathField.setText(path);
                RupsConfiguration.INSTANCE.setHomeFolder(path);
            }
        });

        JPanel fieldsPanel = new JPanel();
        fieldsPanel.add(pathField);
        fieldsPanel.add(pathChooser);

        JCheckBox openDuplicateFiles = new JCheckBox("", RupsConfiguration.INSTANCE.canOpenDuplicateFiles());
        openDuplicateFiles.addActionListener(
                e -> RupsConfiguration.INSTANCE.setOpenDuplicateFiles(((JCheckBox) e.getSource()).isSelected())
        );
        JLabel openDuplicateFilesLabel = new JLabel(Language.PREFERENCES_ALLOW_DUPLICATE_FILES.getString());
        openDuplicateFilesLabel.setLabelFor(openDuplicateFiles);

        JPanel outerPanel = new JPanel();
        GridBagLayout gridBagLayout = new GridBagLayout();
        outerPanel.setLayout(gridBagLayout);

        GridBagConstraints left = new GridBagConstraints();
        left.anchor = GridBagConstraints.EAST;
        GridBagConstraints right = new GridBagConstraints();
        right.weightx = 2.0;
        right.fill = GridBagConstraints.HORIZONTAL;
        right.gridwidth = GridBagConstraints.REMAINDER;

        outerPanel.add(pathLabel, left);
        outerPanel.add(fieldsPanel, right);

        outerPanel.add(openDuplicateFilesLabel, left);
        outerPanel.add(openDuplicateFiles, right);

        JScrollPane scrollPane = new JScrollPane(outerPanel);

        tabbedPane.add(Language.PREFERENCES_RUPS_SETTINGS.getString(), scrollPane);

        jFrame.add(tabbedPane, BorderLayout.CENTER);

        JPanel buttons = new JPanel();

        JButton save = new JButton(Language.SAVE.getString());
        save.addActionListener(e -> RupsConfiguration.INSTANCE.saveConfiguration());
        buttons.add(save);

        JButton cancel = new JButton(Language.DIALOG_CANCEL.getString());
        cancel.addActionListener(e -> {
            if (RupsConfiguration.INSTANCE.hasUnsavedChanges()) {
                int choice = JOptionPane.showConfirmDialog(jFrame,
                        Language.SAVE_UNSAVED_CHANGES.getString());
                if (choice == JOptionPane.OK_OPTION) {
                    RupsConfiguration.INSTANCE.cancelTemporaryChanges();
                    jFrame.dispose();
                }
            } else {
                jFrame.dispose();
            }
        });
        buttons.add(cancel);

        JButton reset = new JButton(Language.PREFERENCES_RESET_TO_DEFAULTS.getString());

        reset.addActionListener(e -> {
            int choice = JOptionPane.showConfirmDialog(jFrame,
                    Language.PREFERENCES_RESET_TO_DEFAULTS_CONFIRM.getString());
            if (choice == JOptionPane.OK_OPTION) {
                RupsConfiguration.INSTANCE.resetToDefaultProperties();

                resetView(pathField, openDuplicateFiles);
            }
        });
        buttons.add(reset);

        jFrame.add(buttons, BorderLayout.SOUTH);
        jFrame.pack();
        jFrame.setResizable(false);
    }

    private void resetView(JTextField pathField, JCheckBox openDuplicateFiles) {
        pathField.setText(RupsConfiguration.INSTANCE.getHomeFolder().getPath());
        openDuplicateFiles.setSelected(RupsConfiguration.INSTANCE.canOpenDuplicateFiles());
    }

    public void show() {
        jFrame.setLocationRelativeTo(null);
        jFrame.setVisible(true);
    }
}
