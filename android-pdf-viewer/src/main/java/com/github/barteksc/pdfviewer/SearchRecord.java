package com.github.barteksc.pdfviewer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class SearchRecord {
    public final int pageIdx;
    public Object data;
    private final Set<SearchRecordItem> searchRecordItems = new LinkedHashSet<>();

    private final ArrayList<String> searchQueries = new ArrayList<>();

    public SearchRecord(String key, int pageIdx) {
        key = key.toLowerCase(Locale.ROOT);
        this.pageIdx = pageIdx;
        prepareSearchQueries(key);
    }

    private void prepareSearchQueries(String key) {
        searchQueries.add(key);
        int spaceIndex = key.indexOf(' ');
        while (spaceIndex >= 0) {
            StringBuilder keyStringBuilder = new StringBuilder(key);
            StringBuilder result = keyStringBuilder.replace(spaceIndex, spaceIndex + 1, "\r\n");
            searchQueries.add(result.toString());
            spaceIndex = key.indexOf(' ', spaceIndex + 1);
        }
        if (searchQueries.size() > 1)
            searchQueries.add(key.replaceAll(" ", "\r\n"));
    }

    public Set<SearchRecordItem> getSearchRecordItems() {
        return searchRecordItems;
    }

    public List<String> getSearchQueries() {
        return searchQueries;
    }

    public void sortSearchRecordItems() {
        List<SearchRecordItem> items = new ArrayList<>(searchRecordItems);
        Collections.sort(items);
        searchRecordItems.clear();
        searchRecordItems.addAll(items);
    }

    public void addSearchRecordItem(SearchRecordItem searchRecordItem) {
        searchRecordItems.add(searchRecordItem);
    }
}
