package com.itextpdf.rups.io.listeners;

import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

public class SelectFileActionListener implements ActionListener {

    private JTextField textField;
    private JFileChooser fileChooser;
    private FileFilter fileFilter;

    public SelectFileActionListener(JTextField textField, FileFilter fileFilter) {
        this.textField = textField;
        this.fileFilter = fileFilter;

        this.fileChooser = new JFileChooser();
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        JFileChooser fc = new JFileChooser();
        int returnVal = fc.showOpenDialog(this.textField);

        if (returnVal == JFileChooser.APPROVE_OPTION) {
            File file = fc.getSelectedFile();
            this.textField.setText(file.getAbsolutePath());
        }
    }
}
