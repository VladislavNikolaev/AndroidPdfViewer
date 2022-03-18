package com.github.barteksc.pdfviewer;

public class Word {
    private final int pageIndex;
    private final int start;
    private final int end;

    public Word(int pageIndex, int start, int end) {

        this.pageIndex = pageIndex;
        this.start = start;
        this.end = end;
    }

    public int getPageIndex() {
        return pageIndex;
    }

    public int getStart() {
        return start;
    }

    public int getEnd() {
        return end;
    }
}
