package com.itextpdf.rups.event;

import com.itextpdf.kernel.utils.CompareTool;

public class PostCompareEvent extends RupsEvent{

    private CompareTool.CompareResult result;

    public PostCompareEvent(CompareTool.CompareResult result) {
        this.result = result;
    }

    @Override
    public int getType() {
        return COMPARE_POST_EVENT;
    }

    @Override
    public Object getContent() {
        return result;
    }
}
