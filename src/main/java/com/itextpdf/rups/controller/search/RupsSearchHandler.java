package com.itextpdf.rups.controller.search;

import com.itextpdf.kernel.pdf.PdfIndirectReference;
import com.itextpdf.kernel.pdf.PdfObject;
import com.itextpdf.rups.view.RupsPanel;
import com.itextpdf.search.model.ESearchOptions;
import com.itextpdf.search.model.ESearchScope;
import com.itextpdf.search.model.ISearchFilter;
import com.itextpdf.search.ISearchHandler;
import com.itextpdf.search.model.ISearchResult;
import com.itextpdf.search.model.SearchContext;
import com.itextpdf.search.SearchHandler;

import javax.swing.event.TreeSelectionListener;
import java.util.concurrent.atomic.AtomicBoolean;


public abstract class RupsSearchHandler<T extends PdfObject> extends SearchHandler<T> implements TreeSelectionListener {
    private static ISearchHandler INSTANCE;

    public RupsSearchHandler() {
        if (null == INSTANCE) {
            INSTANCE = this;
        }

    }

    @Override
    public ISearchResult find(SearchContext<T> context, ISearchFilter filter) {
        AtomicBoolean case_sensitive = new AtomicBoolean(false);
        AtomicBoolean regex = new AtomicBoolean(false);
        AtomicBoolean word = new AtomicBoolean(false);

        filter.getOptions().forEach((ESearchOptions option) -> {
            case_sensitive.set(case_sensitive.get() || option == ESearchOptions.CASE_SENSITIVE);
            regex.set((!word.get()) && (regex.get() || option == ESearchOptions.REGEX));
            word.set((!regex.get()) && (word.get() || option == ESearchOptions.REGEX));
        });

        if(regex.get()){
            // Regex Evaluation
        } else if (word.get()) {
            // Whole Word Matches only
        } else {
            // Default Search
        }
        // new ThreadedSearch

        return null;
    }

    public static ISearchHandler getInstance(RupsPanel rupsPanel) {
        return INSTANCE;
    }

    public abstract SearchContext<PdfIndirectReference> getNewContext(ESearchScope itemScope);
}
