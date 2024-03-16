package com.itextpdf.rups.view.diff;

import be.ysebie.diff.lib.Delta;
import be.ysebie.diff.lib.Diff;
import be.ysebie.diff.lib.deltaimpl.ChangeDelta;
import be.ysebie.diff.lib.deltaimpl.DeleteDelta;
import be.ysebie.diff.lib.deltaimpl.EqualDelta;
import be.ysebie.diff.lib.deltaimpl.InsertDelta;

import javax.swing.*;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;
import java.awt.*;
import java.io.File;
import java.nio.ByteBuffer;
import java.util.List;

public class DiffViewer extends JPanel {

    private final ViewerOptions options;
    private final JFileChooser filePicker = new JFileChooser();
    private final JCheckBox cbDecompress = new JCheckBox("Decompress");
    private final JPanel optionsBox = new JPanel();
    JPanel mainPanel = new JPanel();

    public DiffViewer(File a, File b) {
        options = new ViewerOptions();
        options.fileA = a;
        options.fileB = b;
        BorderLayout layout = new BorderLayout();
        layout.setHgap(1);
        layout.setVgap(1);
        setLayout(layout);
        GridLayout layout2 = new GridLayout(1, 2);
        mainPanel.setLayout(layout2);

        add(mainPanel, BorderLayout.CENTER);
        setupOptionSelector();
        setupMainView();
    }

    private static String convertByteBufferToString(ByteBuffer byteBuffer) {
        return new String(byteBuffer.array());
    }

    private void setupOptionSelector() {
        cbDecompress.setSelected(options.decompress);
        cbDecompress.addActionListener((l) -> {
            options.decompress = cbDecompress.isSelected();
            rerenderDiff();
        });
        optionsBox.add(cbDecompress);
        add(optionsBox, BorderLayout.NORTH);
    }

    private void rerenderDiff() {
        SwingUtilities.invokeLater(this::setupMainView);

    }

    private void setupMainView() {
        mainPanel.removeAll();

        JPanel panelA = new JPanel();
        JPanel panelB = new JPanel();
        Box vboxA = Box.createVerticalBox();
        Box vboxB = Box.createVerticalBox();


        vboxA.setSize(100, 100);
        vboxB.setSize(100, 100);

        panelA.setLayout(new BoxLayout(panelA, BoxLayout.Y_AXIS));
        panelB.setLayout(new BoxLayout(panelB, BoxLayout.Y_AXIS));

        vboxA.setVisible(true);
        vboxB.setVisible(true);

        JButton btnFileA = new JButton("File A");
        btnFileA.addActionListener((l) -> {
            int returnVal = filePicker.showOpenDialog(this);
            if (returnVal == JFileChooser.APPROVE_OPTION) {
                options.fileA = filePicker.getSelectedFile();
                rerenderDiff();
            }
        });

        JButton btnFileB = new JButton("File B");
        btnFileB.addActionListener((l) -> {
            int returnVal = filePicker.showOpenDialog(this);
            if (returnVal == JFileChooser.APPROVE_OPTION) {
                options.fileB = filePicker.getSelectedFile();
                rerenderDiff();
            }
        });

        JLabel labelA = new JLabel("File A: " + (options.fileA != null ? options.fileA.getName() : "None"));
        JLabel labelB = new JLabel("File B: " + (options.fileB != null ? options.fileB.getName() : "None"));

        labelA.setHorizontalAlignment(SwingConstants.CENTER);
        labelB.setHorizontalAlignment(SwingConstants.CENTER);

        labelA.setSize(100, 100);
        labelB.setSize(100, 100);

        vboxA.add(labelA);
        vboxA.add(btnFileA);

        vboxB.add(labelB);
        vboxB.add(btnFileB);

        JTextPane textPaneA = new JTextPane();
        textPaneA.setEditable(false);
        JTextPane textPaneB = new JTextPane();
        textPaneB.setEditable(false);


        JScrollPane scrollPaneA = new JScrollPane(textPaneA);
        JScrollPane scrollPaneB = new JScrollPane(textPaneB);

        scrollPaneA.getVerticalScrollBar().addAdjustmentListener((l) -> {
            scrollPaneB.getVerticalScrollBar().setValue(scrollPaneA.getVerticalScrollBar().getValue());
        });
        scrollPaneA.getHorizontalScrollBar().addAdjustmentListener((l) -> {
            scrollPaneB.getHorizontalScrollBar().setValue(scrollPaneA.getHorizontalScrollBar().getValue());
        });

        scrollPaneB.getVerticalScrollBar().addAdjustmentListener((l) -> {
            scrollPaneA.getVerticalScrollBar().setValue(scrollPaneB.getVerticalScrollBar().getValue());
        });

        scrollPaneB.getHorizontalScrollBar().addAdjustmentListener((l) -> {
            scrollPaneA.getHorizontalScrollBar().setValue(scrollPaneB.getHorizontalScrollBar().getValue());
        });

        StyledDocument docA = textPaneA.getStyledDocument();
        StyledDocument docB = textPaneB.getStyledDocument();

        vboxA.add(scrollPaneA);
        vboxB.add(scrollPaneB);

        if (options.fileA != null && options.fileB != null) {
            System.out.println("Generating diff");
            try {
                DiffGenerationStrategy diffGenerationStrategy = new DiffGenerationStrategy(options);
                Diff<ByteBuffer> diff = new Diff<ByteBuffer>(diffGenerationStrategy);
                List<Delta<ByteBuffer>> deltas = diff.generateDeltas();
                for (Delta<ByteBuffer> delta : deltas) {
                    switch (delta.getType()) {
                        case Change:
                            System.out.println("Change");
                            ChangeDelta<ByteBuffer> changeDelta = (ChangeDelta<ByteBuffer>) delta;
                            StringBuilder sbChangeA = new StringBuilder();
                            StringBuilder sbChangeB = new StringBuilder();
                            for (ByteBuffer data : changeDelta.getDeletedData()) {
                                sbChangeA.append(convertByteBufferToString(data)).append("\n");
                            }
                            for (ByteBuffer data : changeDelta.getInsertedData()) {
                                sbChangeB.append(convertByteBufferToString(data)).append("\n");
                            }
                            SimpleAttributeSet keyWord = new SimpleAttributeSet();
                            StyleConstants.setForeground(keyWord, Color.BLACK);
                            StyleConstants.setBackground(keyWord, Color.YELLOW);
                            StyleConstants.setBold(keyWord, true);

                            docA.insertString(docA.getLength(), sbChangeA.toString(), keyWord);
                            docB.insertString(docB.getLength(), sbChangeB.toString(), keyWord);
                            break;
                        case Delete:
                            DeleteDelta<ByteBuffer> deleteDelta = (DeleteDelta<ByteBuffer>) delta;
                            StringBuilder sbADelete = new StringBuilder();
                            StringBuilder sbBDelete = new StringBuilder();
                            for (ByteBuffer data : deleteDelta.getDeletedData()) {
                                sbADelete.append(convertByteBufferToString(data)).append("\n");
                                sbBDelete.append("\n");
                            }
                            SimpleAttributeSet keyWordDelete = new SimpleAttributeSet();
                            StyleConstants.setForeground(keyWordDelete, Color.BLACK);
                            StyleConstants.setBackground(keyWordDelete, Color.RED);

                            docA.insertString(docA.getLength(), sbADelete.toString(), keyWordDelete);
                            docB.insertString(docB.getLength(), sbBDelete.toString(), keyWordDelete);

                            break;
                        case Equal:
                            EqualDelta<ByteBuffer> equalDelta = (EqualDelta<ByteBuffer>) delta;
                            StringBuilder sb = new StringBuilder();
                            for (ByteBuffer data : equalDelta.getData()) {
                                sb.append(convertByteBufferToString(data)).append("\n");
                            }
                            docA.insertString(docA.getLength(), sb.toString(), null);
                            docB.insertString(docB.getLength(), sb.toString(), null);
                            break;
                        case Insert:
                            System.out.println("inserted");
                            InsertDelta<ByteBuffer> insertDelta = (InsertDelta<ByteBuffer>) delta;
                            StringBuilder sbA = new StringBuilder();
                            StringBuilder sbB = new StringBuilder();
                            for (ByteBuffer data : insertDelta.getInsertedData()) {
                                sbA.append("\n");
                                sbB.append(convertByteBufferToString(data)).append("\n");
                            }
                            SimpleAttributeSet keyWordInsert = new SimpleAttributeSet();
                            StyleConstants.setForeground(keyWordInsert, Color.BLACK);
                            StyleConstants.setBackground(keyWordInsert, Color.GREEN);

                            docA.insertString(docA.getLength(), sbA.toString(), keyWordInsert);
                            docB.insertString(docB.getLength(), sbB.toString(), keyWordInsert);

                            break;
                    }
                }
            } catch (Exception e) {
                System.out.println("Error generating diff: " + e.getMessage());
                e.printStackTrace();
            }

        }



        panelA.add(vboxA);
        panelB.add(vboxB);

        mainPanel.add(panelA);
        mainPanel.add(panelB);
        revalidate();
    }

}

