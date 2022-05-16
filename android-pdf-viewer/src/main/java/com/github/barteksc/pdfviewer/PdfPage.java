package com.github.barteksc.pdfviewer;

import android.graphics.RectF;
import android.util.Log;

import com.ahmer.afzal.pdfium.PdfDocument;
import com.ahmer.afzal.pdfium.PdfiumCore;
import com.ahmer.afzal.pdfium.util.Size;
import com.ahmer.afzal.pdfium.util.SizeF;

import java.util.Locale;
import java.util.concurrent.atomic.AtomicLong;

public class PdfPage {

    private long tid;
    private final PdfiumCore pdfiumCore;
    private final PdfDocument pdfDocument;
    private final int pageIdx;
    private final AtomicLong pid = new AtomicLong();
    private final Size originalSize;
    private SizeF currentSize;
    private String allText;
    private BreakIteratorHelper pageBreakIterator;

    public PdfPage(PdfiumCore pdfiumCore, PdfDocument pdfDocument, int pageIdx, Size originalSize) {
        this.pdfiumCore = pdfiumCore;
        this.pdfDocument = pdfDocument;
        this.pageIdx = pageIdx;
        this.originalSize = originalSize;
    }

    public void setCurrentSize(SizeF currentSize) {
        this.currentSize = currentSize;
    }

    public int getPageIdx() {
        return pageIdx;
    }

    public AtomicLong getPid() {
        return pid;
    }

    public long getTid() {
        return tid;
    }

    public boolean loadText() {
        synchronized (pid) {
            boolean shouldClose = open();
            if (tid == 0) {
                tid = pdfiumCore.openText(pid.get());
            }
            return shouldClose;
        }
    }

    public boolean open() {
        if (pid.get() == 0) {
            synchronized (pid) {
                if (pid.get() == 0) {
                    pid.set(pdfiumCore.openPage(pdfDocument, pageIdx));
                    return true;
                }
            }
        }
        return false;
    }

    public void prepareText() {
        loadText();
        if (allText == null) {
            allText = pdfiumCore.nativeGetText(tid);
            if (pageBreakIterator == null) {
                pageBreakIterator = new BreakIteratorHelper();
            }
            pageBreakIterator.setText(allText);
        }
    }

    public void close() {
        if (pid.get() != 0) {
            synchronized (pid) {
                pdfiumCore.closePage(pdfDocument, pageIdx);
                pid.set(0);
                tid = 0;
            }
        }
    }

    public Size getOriginalSize() {
        return originalSize;
    }

    public SearchRecord getAllMatchOnPage(String key) {
        SearchRecord record = new SearchRecord(key, pageIdx);
        prepareText();
        String pageTextLowercase = allText.toLowerCase(Locale.ROOT);
        if (record.data == null) {
            for (String searchQuery : record.getSearchQueries()) {
                int startIndex = pageTextLowercase.indexOf(searchQuery);
                while (startIndex >= 0) {
                    int endIndex = searchQuery.length();
                    RectF[] rectsForRecordItem = getRectsForRecordItem(startIndex, endIndex);
                    if (rectsForRecordItem != null) {
                        record.addSearchRecordItem(new SearchRecordItem(startIndex, endIndex, rectsForRecordItem, pageIdx));
                    }
                    int nextSearchStartIndex = startIndex + 1;
                    startIndex = pageTextLowercase.indexOf(searchQuery, nextSearchStartIndex);
                }
            }
            record.sortSearchRecordItems();
        }
        return record;
    }

    private RectF[] getRectsForRecordItem(int start, int end) {
        if (start >= 0 && end > 0) {
            int rectCount = pdfiumCore.nativeCountRects(tid, start, end);
            Log.d("olol", "getAllMatchOnPage getKeyStr page=" + getPageIdx() + " nativeCountRects = " + rectCount);

            if (rectCount > 0) {
                RectF[] rects = new RectF[rectCount];
                for (int i = 0; i < rectCount; i++) {
                    RectF rect = new RectF();
                    pdfiumCore.nativeGetRect(
                            pid.get(), 0, 0, (int) currentSize.getWidth(),
                            (int) currentSize.getHeight(), tid, rect, i);
                    Log.d("olol", "getAllMatchOnPage getKeyStr page=" + getPageIdx() + " nativeGetRect = " + rect);
                    rects[i] = rect;
                }
                return rects;
            }
        }
        return null;
    }
}
