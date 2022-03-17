package com.github.barteksc.pdfviewer;

import android.graphics.RectF;

import com.ahmer.afzal.pdfium.PdfDocument;
import com.ahmer.afzal.pdfium.PdfiumCore;
import com.ahmer.afzal.pdfium.util.Size;
import com.ahmer.afzal.pdfium.util.SizeF;

import java.util.ArrayList;
import java.util.Arrays;
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
    private final long offsetAlongScrollAxis;
    private final boolean isVertical;

    public PdfPage(PdfiumCore pdfiumCore, PdfDocument pdfDocument,
                   int pageIdx, Size originalSize, boolean isVertical, long offsetAlongScrollAxis) {
        this.pdfiumCore = pdfiumCore;
        this.pdfDocument = pdfDocument;
        this.pageIdx = pageIdx;
        this.originalSize = originalSize;
        this.isVertical = isVertical;
        this.offsetAlongScrollAxis = offsetAlongScrollAxis;
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

    public void getAllMatchOnPage(SearchRecord record) {
        prepareText();
        if (record.data == null) {
            long keyStr = record.getKeyStr();
            if (keyStr != 0) {
                long searchHandle = pdfiumCore.nativeFindTextPageStart(tid, keyStr, 0, record.findStart);
                if (searchHandle != 0) {
                    while (pdfiumCore.nativeFindTextPageNext(searchHandle)) {
                        int startIndex = pdfiumCore.nativeGetFindIdx(searchHandle);
                        int endIndex = pdfiumCore.nativeGetFindLength(searchHandle);
                        getRectsForRecordItem(record.getSearchRecordItems(), startIndex, endIndex, record.pageIdx);
                    }
                    pdfiumCore.nativeFindTextPageEnd(searchHandle);
                }
            }
        }
    }

    private void getRectsForRecordItem(ArrayList<SearchRecordItem> data, int start, int end, int pageIdx) {
        if (start >= 0 && end > 0) {
            int rectCount = pdfiumCore.nativeCountRects(tid, start, end);
            if (rectCount > 0) {
                RectF[] rects = new RectF[rectCount];
                for (int i = 0; i < rectCount; i++) {
                    RectF rect = new RectF();
                    pdfiumCore.nativeGetRect(
                            pid.get(), 0, 0, (int) currentSize.getWidth(),
                            (int) currentSize.getHeight(), tid, rect, i);
                    rects[i] = rect;
                }
                rects = Arrays.asList(rects).toArray(new RectF[0]);
                data.add(new SearchRecordItem(start, end, rects, pageIdx));
            }
        }
    }
}
