package com.itextpdf.rups.view;

import com.itextpdf.research.xfdfmerge.XfdfMerge;
import com.itextpdf.rups.controller.RupsController;
import com.itextpdf.rups.io.filters.PdfFilter;
import com.itextpdf.rups.io.filters.XfdfFilter;
import com.itextpdf.rups.io.listeners.SelectFileActionListener;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class XfdfMergeDialog {

    private JPanel mainPanel;
    private JTextField xfdfInputTextField;
    private JTextField pdfOutputField;
    private JFrame jFrame;

    public XfdfMergeDialog(RupsController controller) {
        this.jFrame = new JFrame("Apply XFDF to PDF File");

        this.mainPanel = new JPanel();
        GridLayout gridLayout = new GridLayout(0, 3);
        gridLayout.setHgap(3);
        gridLayout.setVgap(3);
        this.mainPanel.setLayout(gridLayout);

        JLabel xfdfLabel = new JLabel("XFDF File");
        this.xfdfInputTextField = new JTextField();
        this.mainPanel.add(xfdfLabel);
        this.mainPanel.add(this.xfdfInputTextField);
        JButton selectXFDFButton = new JButton("Select File");
        selectXFDFButton.addActionListener(new SelectFileActionListener(this.xfdfInputTextField, XfdfFilter.INSTANCE));
        this.mainPanel.add(selectXFDFButton);

        JLabel pdfLabel = new JLabel("PDF File");
        this.pdfOutputField = new JTextField();
        this.mainPanel.add(pdfLabel);
        this.mainPanel.add(this.pdfOutputField);
        JButton selectPDFButton = new JButton("Select Output");
        selectPDFButton.addActionListener(new SelectFileActionListener(this.pdfOutputField, PdfFilter.INSTANCE));
        this.mainPanel.add(selectPDFButton);

        this.mainPanel.add(new JLabel());
        this.mainPanel.add(new JLabel());
        this.mainPanel.add(new JLabel());

        this.mainPanel.add(new JLabel());
        this.mainPanel.add(new JLabel());
        JButton applyButton = new JButton("Apply XFDF");

        applyButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String pdfFile = pdfOutputField.getText();
                String xfdfFile = xfdfInputTextField.getText();
                String inputPath = controller.getPdfFile().getDirectory().getPath();
                String inputName = controller.getPdfFile().getFilename();
                String input = inputPath + "/" + inputName;

                String[] args = new String[] {input, xfdfFile, pdfFile};

                try {
                    XfdfMerge.main(args);
                    JOptionPane.showMessageDialog(null, "XFDF applied successfully!");
                    jFrame.dispose();
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(null, ex.getMessage());
                }
            }
        });

        this.mainPanel.add(applyButton);

        this.jFrame.setContentPane(this.mainPanel);
        this.jFrame.pack();
    }

    public void show() {
        Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
        this.jFrame.setLocation((int) (screen.getWidth() * .05), (int) (screen.getHeight() * .05));
        this.jFrame.setResizable(true);
        this.jFrame.setVisible(true);
    }
}
