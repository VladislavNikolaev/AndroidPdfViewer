package com.github.barteksc.pdfviewer;

import android.graphics.RectF;

/** Stores the highlight rects and start-end index
 *  	of one matching item on a page */
public class SearchRecordItem {
	public final int start;
	public final int end;
	public final RectF[] rects;
	public SearchRecordItem(int start, int end, RectF[] rects) {
		this.start = start;
		this.end = end;
		this.rects = rects;
	}
}