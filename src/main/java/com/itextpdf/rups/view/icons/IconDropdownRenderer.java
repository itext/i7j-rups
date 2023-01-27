package com.itextpdf.rups.view.icons;

import com.itextpdf.rups.RupsConfiguration;
import com.itextpdf.search.model.PropertyEnum;

import javax.swing.*;
import java.awt.*;

public class IconDropdownRenderer extends JLabel implements ListCellRenderer<PropertyEnum> {
    @Override
    public Component getListCellRendererComponent(JList<? extends PropertyEnum> list, PropertyEnum object, int index, boolean isSelected, boolean cellHasFocus) {
        String type = object.getType();
        String value = object.toString().toLowerCase();
        String iconString = RupsConfiguration.INSTANCE.getIconFor(String.format("rups.%s.%1s",type,value));
        Icon icon = IconFetcher.getIcon(iconString);
        this.setIcon(icon);
        this.setToolTipText(object.getTooltip());// TODO: Implement Locales.
        this.setSize(icon.getIconWidth(),icon.getIconHeight());
        this.setVisible(true);
        return this;
    }
}
