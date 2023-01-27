package com.itextpdf.rups.controller.search;

import com.itextpdf.search.model.ESearchOptions;
import com.itextpdf.search.model.ISearchFilter;

import java.util.Arrays;
import java.util.Collection;

public class PdfStreamSearchFilter implements ISearchFilter {
    String query = "";
    ESearchOptions[] options = {};
    @Override
    public boolean setQuery(String inputQuery) {
        //TODO: Properly Validate input...
        if(null != inputQuery)
            this.query = inputQuery;
        return (null !=query) && (query == inputQuery);
    }

    @Override
    public boolean setOptions(ESearchOptions[] searchOptions) {
        options = searchOptions;
        return (null != options) && (options == searchOptions);
    }

    @Override
    public String getQuery() {
        return query;
    }

    @Override
    public Collection<ESearchOptions> getOptions() {
        return Arrays.asList(options);
    }
}
