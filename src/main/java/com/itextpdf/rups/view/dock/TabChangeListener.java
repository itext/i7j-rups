package com.itextpdf.rups.view.dock;

import com.itextpdf.rups.controller.RupsInstanceController;
import com.itextpdf.rups.view.RupsPanel;
import org.noos.xing.mydoggy.ToolWindow;
import org.noos.xing.mydoggy.ToolWindowTab;
import org.noos.xing.mydoggy.plaf.MyDoggyToolWindowManager;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.util.Observable;

public class TabChangeListener extends Observable implements ChangeListener {
    private MyDoggyToolWindowManager doggy;

    @Override
    public void stateChanged(ChangeEvent e) {
        JTabbedPane tabbedPane = (JTabbedPane) e.getSource();
        if ( tabbedPane.getSelectedComponent() instanceof RupsPanel) {
            RupsPanel rupsPanel = ((RupsPanel) (tabbedPane.getSelectedComponent()));
            RupsInstanceController controller = rupsPanel.getRupsInstanceController();

            ToolWindow info = doggy.getToolWindow("Info");
            info.getToolWindowTabs()[0].setComponent(controller.getDockedComponent(InfoDockPanel.class));

            ToolWindow xref = doggy.getToolWindow("XREF");
            ToolWindowTab[] toolWindowTabs = xref.getToolWindowTabs();
            Component dockedComponent = controller.getDockedComponent(XRefTable.class);
            toolWindowTabs[0].setComponent(dockedComponent);

            ToolWindow pages = doggy.getToolWindow("Pages");
            pages.getToolWindowTabs()[0].setComponent(controller.getDockedComponent(PagesTable.class));

            doggy.repaint();
        }
    }

    public void setDoggy(MyDoggyToolWindowManager toolWindowManager) {
        this.doggy = toolWindowManager;
    }
}
