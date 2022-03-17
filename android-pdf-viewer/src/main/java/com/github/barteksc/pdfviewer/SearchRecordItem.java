package com.github.barteksc.pdfviewer;

import android.graphics.RectF;

/**
 * Stores the highlight rects and start-end index
 * of one matching item on a page
 */
public class SearchRecordItem {
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
}