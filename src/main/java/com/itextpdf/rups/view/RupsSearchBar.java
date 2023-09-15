package com.itextpdf.rups.view;

import com.itextpdf.kernel.pdf.PdfIndirectReference;
import com.itextpdf.rups.RupsConfiguration;
import com.itextpdf.search.model.ESearchOptions;
import com.itextpdf.search.model.ESearchScope;
import com.itextpdf.search.model.ISearchFilter;
import com.itextpdf.search.ISearchHandler;
import com.itextpdf.search.model.SearchContext;
import com.itextpdf.rups.view.icons.IconDropdownRenderer;
import com.itextpdf.rups.view.icons.IconFetcher;

import javax.swing.*;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.ArrayList;
import java.util.List;

public class RupsSearchBar extends JDialog /*implements TreeSelectionListener */{

    //TODO:
    //  Move tree listener to search bar.
    //  Track SearchInstance instances rather than search bar instances
    //  -> re-fill in the search bar details from instance when a node is re-selected
    //  Make search handler more atomic

    static private RupsSearchBar INSTANCE;
    private JComboBox<ESearchScope> scopeDropdown = new JComboBox<>();
    private AncestorListener ancestorListener;
    private FocusListener focusListener;
    private KeyListener keyListener;

    JComponent targetComponent;
    PdfIndirectReference targetObject;
    JTextField searchBox;
    JButton caseSensitivity;
    JRadioButton wordSearch;
    JRadioButton regexSearch;
    ISearchHandler searchHandler;

    private int width;
    private int height;

    public RupsSearchBar(){
//        super(SwingUtilities.getWindowAncestor(component));
        super();

//        this.targetComponent = component;
        //TODO: Move <<Target <-> Instance>> pairing to either an internal Map or to the controller
        this.setUndecorated(true);
        this.setupListeners();
        this.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
        this.setLayout(new BorderLayout());
        this.setAlwaysOnTop(true);
        JPanel searchControls = new JPanel();
        searchControls.setLayout(new BorderLayout());

        JPanel searchOptions = new JPanel();
        searchOptions.setLayout(new FlowLayout());

        JScrollPane searchResults = new JScrollPane();
        JTextField search = new JTextField("");
        search.setName("SearchInput");
        this.searchBox = search;

        searchControls.add(searchBox, BorderLayout.WEST);

        JButton caseSwitch = new JButton(IconFetcher.getIcon(RupsConfiguration.INSTANCE.getIconFor("rups.search.options.case_sensitivity")));
        caseSwitch.setName("CaseSensitive");
        caseSwitch.setToolTipText(ESearchOptions.CASE_SENSITIVE.getTooltip());
        caseSensitivity = caseSwitch;

        searchOptions.add(caseSensitivity);

        JRadioButtonMenuItem wordOrRegex = new JRadioButtonMenuItem();

        JRadioButton word = new JRadioButton(IconFetcher.getIcon(RupsConfiguration.INSTANCE.getIconFor("rups.search.options.word")));
        word.setName("Word");
        word.setToolTipText(ESearchOptions.WORD.getTooltip());
        wordSearch = word;

        JRadioButton regex = new JRadioButton(IconFetcher.getIcon(RupsConfiguration.INSTANCE.getIconFor("rups.search.options.regex")));
        regex.setName("Regex");
        regex.setToolTipText(ESearchOptions.REGEX.getTooltip());
        regexSearch = regex;

        searchOptions.add(wordSearch);
        searchOptions.add(regexSearch);

//                scopeDropdown.setIcon(IconFetcher.getIcon("magnifier.png"));

        JLabel localScope = new JLabel();
        localScope.setIcon(IconFetcher.getIcon(RupsConfiguration.INSTANCE.getIconFor("rups.search.scope.selection")));
        localScope.setToolTipText(ESearchScope.SELECTION.getTooltip());
        localScope.setVisible(true);
        scopeDropdown.addItem(ESearchScope.SELECTION);

        JLabel documentScope = new JLabel();
        documentScope.setIcon(IconFetcher.getIcon(RupsConfiguration.INSTANCE.getIconFor("rups.search.scope.document")));
        documentScope.setToolTipText(ESearchScope.DOCUMENT.getTooltip());
        documentScope.setVisible(true);
        scopeDropdown.addItem(ESearchScope.DOCUMENT);

        JLabel allDocumentsScope = new JLabel();
        allDocumentsScope.setIcon(IconFetcher.getIcon(RupsConfiguration.INSTANCE.getIconFor("rups.search.scope.all_documents")));
        allDocumentsScope.setToolTipText(ESearchScope.ALL_DOCUMENTS.getTooltip());
        allDocumentsScope.setVisible(true);
        scopeDropdown.addItem(ESearchScope.ALL_DOCUMENTS);

        scopeDropdown.setSelectedIndex(0);

        scopeDropdown.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                scopeDropdown.setToolTipText(((ESearchScope)scopeDropdown.getSelectedItem()).getTooltip());
            }
        });

        scopeDropdown.setToolTipText(((ESearchScope) scopeDropdown.getSelectedItem()).getTooltip());

        scopeDropdown.setRenderer(new IconDropdownRenderer());

        searchOptions.add(scopeDropdown);

        searchControls.add(searchOptions, BorderLayout.EAST);
        this.add(searchControls,BorderLayout.NORTH);
        this.add(searchResults,BorderLayout.CENTER);


        this.searchBox.addKeyListener(keyListener);

    }

    private void setupListeners() {
        // Key Listener
        this.keyListener = new KeyListener() {
            @Override
            public void keyTyped(KeyEvent e) {
                if (e.getKeyChar() != KeyEvent.VK_ENTER)
                    return;
//                searchHandler.setSearchTarget(,(ESearchScope) INSTANCES.get(target).scopeDropdown.getSelectedItem());
                SearchContext currentContext = searchHandler.getContext(searchHandler.getCurrentTarget(), (ESearchScope) scopeDropdown.getSelectedItem());
                ISearchFilter query = currentContext.getNewFilter();
                query.setQuery(searchBox.getText());

                List<ESearchOptions> searchOptions = new ArrayList<ESearchOptions>();
                if (caseSensitivity.isSelected())
                    searchOptions.add(ESearchOptions.CASE_SENSITIVE);
                if (wordSearch.getModel().isSelected())
                    searchOptions.add(ESearchOptions.WORD);
                if (regexSearch.getModel().isSelected())
                    searchOptions.add(ESearchOptions.REGEX);
                ESearchOptions[] resultingOptions = new ESearchOptions[searchOptions.size()];
                searchOptions.toArray(resultingOptions);
                query.setOptions(resultingOptions);
//                (ESearchScope) scopeDropdown.getSelectedItem())

                //TODO - Fix this being Null.
                targetObject = searchHandler.getCurrentTarget().getIndirectReference();

                searchHandler.find(currentContext, query);
                System.out.println("Rups SearchInstance Handler");
                System.out.println(query.toString());
            }

            @Override
            public void keyPressed(KeyEvent e) {}

            @Override
            public void keyReleased(KeyEvent e) {}
        };

        // Focus Listener
        this.focusListener = new FocusListener() {

            @Override
            public void focusGained(FocusEvent e) {
                System.out.println("Target Focus Gained.");
            }

            @Override
            public void focusLost(FocusEvent e) {
                System.out.println("Target Focus Lost.");
//                INSTANCES.get(target).hideSearchBar(true);
            }
        };

        // Ancestor Listener
        this.ancestorListener = new AncestorListener() {
            @Override
            public void ancestorAdded(AncestorEvent event) {}

            @Override
            public void ancestorRemoved(AncestorEvent event) {
                setTarget(null); // clean up old listeners but don't assign new ones
            }

            @Override
            public void ancestorMoved(AncestorEvent event) {
                setWindowPosition();
            }
        };
    }

    public void setTarget(JComponent newTarget) {
        if (newTarget != this.targetComponent) {
            if (this.targetComponent != null) {
                this.targetComponent.removeAncestorListener(this.ancestorListener);
                this.targetComponent.removeFocusListener(this.focusListener);
            }
            //TODO: remove listeners from old target
            // Update the listeners to the new target
            // Add the listeners to the new target
            this.targetComponent = newTarget;
//            newTarget.getTopLevelAncestor();
        }

        if (this.targetComponent == null)
            return;

        this.targetComponent.addAncestorListener(ancestorListener);

        this.targetComponent.addFocusListener(this.focusListener);

//        setWindowPosition();

        this.width = /*500;//*/this.targetComponent.getSize().width;
        this.height = 24;/*500;//*///searchBox.getHeight();
        int fontWidth = searchBox.getFont().getSize();
//        int fontWidth = 18;
        int buttonWidth = caseSensitivity.getWidth();
        int colNum = (width - (3 * buttonWidth))/ fontWidth;
        System.out.print("Column #: ");
        System.out.println(colNum);
        searchBox.setColumns(colNum);
        searchBox.setLocation(1,0);
        setWindowPosition();
        INSTANCE.hideSearchBar(false);
    }

    private void setWindowPosition() {
        Point targetLocation = targetComponent.getLocation();
        SwingUtilities.convertPointToScreen(targetLocation, targetComponent);
        setLocation(targetLocation);
        setBounds(new Rectangle(targetLocation.x,targetLocation.y, width, height));
    }

    private void hideSearchBar(boolean hide){
        setVisible(!hide);
        setEnabled(!hide);
    }

    public static RupsSearchBar getSearchBar(JComponent component){
        if(INSTANCE == null)
            INSTANCE = new RupsSearchBar();
        INSTANCE.setTarget(component);
        return INSTANCE;
        //TODO: Bring to Foreground.
        //TODO: Hide on losing focus.
    }

//    @Override
//    public void valueChanged(TreeSelectionEvent e) {
//        this.targetObject = ((PdfObjectTreeNode) e.getNewLeadSelectionPath().getLastPathComponent()).getPdfObject().getIndirectReference();
//    }
    //TODO let SearchHandler determine focused object.

    public void setSearchHandler(ISearchHandler searchHandler) {
        this.searchHandler = searchHandler;
    }
}
