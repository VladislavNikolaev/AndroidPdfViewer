package com.github.barteksc.pdfviewer;

import android.graphics.RectF;

import androidx.annotation.Nullable;

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

    public PdfPage(PdfiumCore pdfiumCore, PdfDocument pdfDocument,
                   int pageIdx, Size originalSize) {
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

    public String getAllText() {
        return allText;
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

    @Nullable
    public Word getWordAtPosition(int pageX, int pageY, SizeF pageSize,
                                  float mappedX, float mappedY, float tolFactor) {
        long pagePtr = pdfDocument.mNativePagesPtr.get(pageIdx);
        int charIdx = pdfiumCore.nativeGetCharIndexAtCoord(pagePtr, pageSize.getWidth(), pageSize.getHeight(),
                tid, Math.abs(mappedX - pageX), Math.abs(mappedY - pageY), 10.0 * tolFactor, 10.0 * tolFactor);

        if (charIdx >= 0) {

            int start = pageBreakIterator.previous();
            int end = pageBreakIterator.following(charIdx);
            return new Word(pageIdx, start, end);

        }

        return null;
    }
}
