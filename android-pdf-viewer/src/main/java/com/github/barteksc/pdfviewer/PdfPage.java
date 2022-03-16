package com.github.barteksc.pdfviewer;

import android.graphics.RectF;

import com.ahmer.afzal.pdfium.PdfDocument;
import com.ahmer.afzal.pdfium.PdfiumCore;
import com.ahmer.afzal.pdfium.util.Size;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

public class PdfPage {

    private long tid;
    private final PdfiumCore pdfiumCore;
    private final PdfDocument pdfDocument;
    private final int pageIdx;
    private final AtomicLong pid = new AtomicLong();
    private final Size size;
    private String allText;
    private BreakIteratorHelper pageBreakIterator;
    private final long offsetAlongScrollAxis;
    private final boolean isVertical;

    public PdfPage(PdfiumCore pdfiumCore, PdfDocument pdfDocument,
                   int pageIdx, Size size, boolean isVertical, long offsetAlongScrollAxis) {
        this.pdfiumCore = pdfiumCore;
        this.pdfDocument = pdfDocument;
        this.pageIdx = pageIdx;
        this.size = size;
        this.isVertical = isVertical;
        this.offsetAlongScrollAxis = offsetAlongScrollAxis;
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

    public Size getSize() {
        return size;
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
                        getRectsForRecordItem(record.getSearchRecordItems(), startIndex, endIndex);
                    }
                    pdfiumCore.nativeFindTextPageEnd(searchHandle);
                }
            }
        }
    }

    private void getRectsForRecordItem(ArrayList<SearchRecordItem> data, int start, int end) {
        if (start >= 0 && end > 0) {
            int rectCount = pdfiumCore.nativeCountRects(tid, start, end);
            if (rectCount > 0) {
                RectF[] rects = new RectF[rectCount];
                for (int i = 0; i < rectCount; i++) {
                    RectF rect = new RectF();
                    int offsetY = isVertical ? (int) offsetAlongScrollAxis : getLateralOffset();
                    int offsetX = isVertical ? getLateralOffset() : (int) offsetAlongScrollAxis;
                    pdfiumCore.nativeGetRect(
                            pid.get(), offsetY, offsetX, size.getWidth(), size.getHeight(), tid, rect, i);
                    rects[i] = rect;
                }
                rects = mergeLineRects(Arrays.asList(rects), null).toArray(new RectF[0]);
                data.add(new SearchRecordItem(start, end, rects));
            }
        }
    }

    public int getLateralOffset() {
        // TODO possibly we should remove it, check rotation of document
        //if(size.getWidth()!=maxPageWidth) {
        //	return (maxPageWidth-size.getWidth())/2;
        //}
        return 0;
    }

    public ArrayList<RectF> mergeLineRects(List<RectF> selRects, RectF box) {
        RectF tmp = new RectF();
        ArrayList<RectF> selLineRects = new ArrayList<>(selRects.size());
        RectF currentLineRect = null;
        for (RectF rI : selRects) {
            //CMN.Log("RectF rI:selRects", rI);
            if (currentLineRect != null && Math.abs((currentLineRect.top + currentLineRect.bottom) - (rI.top + rI.bottom)) < currentLineRect.bottom - currentLineRect.top) {
                currentLineRect.left = Math.min(currentLineRect.left, rI.left);
                currentLineRect.right = Math.max(currentLineRect.right, rI.right);
                currentLineRect.top = Math.min(currentLineRect.top, rI.top);
                currentLineRect.bottom = Math.max(currentLineRect.bottom, rI.bottom);
            } else {
                currentLineRect = new RectF();
                currentLineRect.set(rI);
                selLineRects.add(currentLineRect);
                int cid = getCharIdxAtPos(rI.left + 1, rI.top + rI.height() / 2);
                if (cid > 0) {
                    getCharLoosePos(tmp, cid);
                    currentLineRect.left = Math.min(currentLineRect.left, tmp.left);
                    currentLineRect.right = Math.max(currentLineRect.right, tmp.right);
                    currentLineRect.top = Math.min(currentLineRect.top, tmp.top);
                    currentLineRect.bottom = Math.max(currentLineRect.bottom, tmp.bottom);
                }
            }
            if (box != null) {
                box.left = Math.min(box.left, currentLineRect.left);
                box.right = Math.max(box.right, currentLineRect.right);
                box.top = Math.min(box.top, currentLineRect.top);
                box.bottom = Math.max(box.bottom, currentLineRect.bottom);
            }
        }
        return selLineRects;
    }

    /**
     * Get the char index at a page position
     *
     * @param posX position X in the page coordinate<br/>
     * @param posY position Y in the page coordinate<br/>
     */
    public int getCharIdxAtPos(float posX, float posY) {
        prepareText();
        if (tid != 0) {
            return pdfiumCore.nativeGetCharIndexAtCoord(
                    pid.get(), size.getWidth(), size.getHeight(), tid,
                    posX, posY, 100.0, 100.0);
        }
        return -1;
    }

    public void getCharLoosePos(RectF pos, int index) {
        int offsetY = isVertical ? (int) offsetAlongScrollAxis : getLateralOffset();
        int offsetX = isVertical ? getLateralOffset() : (int) offsetAlongScrollAxis;
        pdfiumCore.nativeGetMixedLooseCharPos(pid.get(), offsetY, offsetX,
                size.getWidth(), size.getHeight(), pos, tid, index, true);
    }
}
