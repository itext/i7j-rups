package com.itextpdf.rups.controller.search;

import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfIndirectReference;
import com.itextpdf.kernel.pdf.PdfObject;
import com.itextpdf.search.model.ESearchScope;
import com.itextpdf.search.model.ISearchFilter;
import com.itextpdf.search.model.ISearchResult;
import com.itextpdf.search.model.SearchContext;

import javax.swing.event.TreeSelectionEvent;
import java.util.HashMap;
import java.util.Map;

public class PdfSearchHandler extends RupsSearchHandler<PdfIndirectReference> {

    Map<PdfIndirectReference,Map<ESearchScope, SearchContext>> contexts;

    PdfIndirectReference currentTarget;

    public PdfSearchHandler(){
        super();
        contexts = new HashMap<>();
    }


    @Override
    public ISearchResult find(SearchContext<PdfIndirectReference> searchContext, ISearchFilter filter) {
        if((null == filter || (null == searchContext.getSearchScope())))
            return null; //TODO: Replace this.

        switch(searchContext.getSearchScope()) {
            case ALL_DOCUMENTS:
                break;
            case DOCUMENT:
                PdfDocument testdoc = searchContext.getTarget().getIndirectReference().getDocument();
                break;
            default:
            case SELECTION:
                PdfObject testobj = searchContext.getTarget().getIndirectReference().getRefersTo(true);
        }
        return null;
    }

    public ISearchFilter getNewFilter() {
        return new PdfStreamSearchFilter();
    }

    @Override
    public SearchContext<PdfIndirectReference> getNewContext(ESearchScope itemScope){
        SearchContext<PdfIndirectReference> newContext = super.getNewContext();
        newContext.setTarget(getCurrentTarget());
        Map<ESearchScope, SearchContext> scopeContexts = contexts.getOrDefault(currentTarget, new HashMap<ESearchScope, SearchContext>());
        scopeContexts.put(itemScope, newContext);
        contexts.put(getCurrentTarget(), scopeContexts);
        return getContext(currentTarget, ESearchScope.SELECTION);

    }

    @Override
    public SearchContext<PdfIndirectReference> getContext(PdfIndirectReference currentTarget, ESearchScope selectedItem) {
        Map<ESearchScope, SearchContext> scopeContexts = contexts.getOrDefault(currentTarget, new HashMap<ESearchScope, SearchContext>());
        return scopeContexts.getOrDefault(currentTarget,getNewContext(selectedItem));
    }

    @Override
    public PdfIndirectReference getCurrentTarget() {
        return currentTarget;
    }

    @Override
    public void valueChanged(TreeSelectionEvent e) {

    }
}
