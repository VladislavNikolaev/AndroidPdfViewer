package com.github.barteksc.pdfviewer;

import android.graphics.RectF;

import java.util.Objects;

/**
 * Stores the highlight rects and start-end index
 * of one matching item on a page
 */
public class SearchRecordItem implements Comparable<SearchRecordItem> {
    public final int start;
    public final int end;
    public final RectF[] rects;
    private final int pageIdx;

    public SearchRecordItem(int start, int end, RectF[] rects, int pageIdx) {
        this.start = start;
        this.end = end;
        this.rects = rects;
        this.pageIdx = pageIdx;
    }

    public int getPageIdx() {
        return pageIdx;
    }

    @Override
    public int compareTo(SearchRecordItem item) {
        return Integer.compare(start, item.start);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SearchRecordItem that = (SearchRecordItem) o;
        return start == that.start && end == that.end;
    }

    @Override
    public int hashCode() {
        return Objects.hash(start, end);
    }
}