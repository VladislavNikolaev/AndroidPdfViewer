package com.github.barteksc.pdfviewer;

import com.shockwave.pdfium.PdfiumCore;

import java.util.ArrayList;

public class SearchRecord {
    private final String key;
    public final int pageIdx;
    public final int findStart;
    public Object data;
    private final ArrayList<SearchRecordItem> searchRecordItems = new ArrayList<>();
    private long keyStr;

    public SearchRecord(String key, int pageIdx, int findStart) {
        this.key = key;
        this.pageIdx = pageIdx;
        this.findStart = findStart;
    }

    public ArrayList<SearchRecordItem> getSearchRecordItems() {
        return searchRecordItems;
    }

    public long getKeyStr() {
        if (keyStr == 0) {
            keyStr = PdfiumCore.nativeGetStringChars(key);
        }
        return keyStr;
    }
}
