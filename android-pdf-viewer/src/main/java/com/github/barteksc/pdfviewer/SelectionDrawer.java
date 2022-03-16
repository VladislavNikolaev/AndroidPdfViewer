package com.github.barteksc.pdfviewer;

import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;
import android.util.SparseArray;
import android.view.View;

import androidx.annotation.NonNull;

import java.util.ArrayList;

public class SelectionDrawer {

    private final Matrix matrix = new Matrix();

    private SparseArray<SearchRecord> searchRecords = new SparseArray<>();
    private final RectF tempRect = new RectF(0, 0, 256, 256);
    private final RectF tempPosRect = new RectF();
    private final View view;

    private float scale;
    private final Paint rectHighlightPaint = new Paint();
    final float[] srcArray = new float[8];
    final float[] dstArray = new float[8];
    private int currentPage;

    private float currentXOffset = 0;
    private float currentYOffset = 0;

    public SelectionDrawer(View view) {
        this.view = view;
        matrix.mapRect(tempRect);
        rectHighlightPaint.setColor(0x80ffff00);
        rectHighlightPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DARKEN));
    }

    public void onDraw(Canvas canvas, PdfFile pdfFile) {
        for (SearchRecord searchRecord : getSearchRecords()) {
            for (SearchRecordItem searchRecordItem : searchRecord.getSearchRecordItems()) {
                RectF[] rects = searchRecordItem.rects;
                for (RectF rect : rects) {
                    drawRect(canvas, rect, pdfFile, searchRecord.pageIdx);
                }
            }
        }
    }

    private void drawRect(Canvas canvas, RectF rect, PdfFile pdfFile, int page) {
        sourceToViewRectFF(rect, tempPosRect, pdfFile, page);
        matrix.reset();
        int bmWidth = (int) rect.width();
        int bmHeight = (int) rect.height();
        setMatrixArray(srcArray, 0, 0, bmWidth, 0, bmWidth, bmHeight, 0, bmHeight);
        setMatrixArray(dstArray,
                tempPosRect.left,
                tempPosRect.top,
                tempPosRect.right,
                tempPosRect.top,
                tempPosRect.right,
                tempPosRect.bottom,
                tempPosRect.left,
                tempPosRect.bottom);

        matrix.setPolyToPoly(srcArray, 0, dstArray, 0, 4);
        matrix.postRotate(0, view.getWidth(), view.getHeight());

        canvas.save();
        canvas.concat(matrix);
        tempPosRect.set(0, 0, bmWidth, bmHeight);
        canvas.drawRect(tempPosRect, rectHighlightPaint);
        canvas.restore();
    }

    private void sourceToViewRectFF(@NonNull RectF sourceRect, @NonNull RectF targetRect, PdfFile pdfFile, int page) {
        int pageX = (int) pdfFile.getSecondaryPageOffset(page, scale);
        int pageY = (int) pdfFile.getPageOffset(page, scale);
        targetRect.set(
                sourceRect.left * scale + ((pageX)) + currentXOffset,
                sourceRect.top * scale + ((pageY)) + currentYOffset,
                sourceRect.right * scale + ((pageX)) + currentXOffset,
                sourceRect.bottom * scale + ((pageY)) + currentYOffset
        );
    }

    public void setSearchRecords(SparseArray<SearchRecord> searchRecords) {
        this.searchRecords = searchRecords;
    }

    public void setScale(float scale) {
        this.scale = scale;
    }

    /**
     * Helper method for setting the values of a tile matrix array.
     */
    void setMatrixArray(float[] array, float f0, float f1, float f2, float f3,
                        float f4, float f5, float f6, float f7) {
        array[0] = f0;
        array[1] = f1;
        array[2] = f2;
        array[3] = f3;
        array[4] = f4;
        array[5] = f5;
        array[6] = f6;
        array[7] = f7;
    }

    public void setCurrentXOffset(float currentXOffset) {
        this.currentXOffset = currentXOffset;
    }

    public void setCurrentYOffset(float currentYOffset) {
        this.currentYOffset = currentYOffset;
    }

    public void setCurrentPage(int currentPage) {
        this.currentPage = currentPage;
    }

    /**
     * To draw search result after and before current page
     **/
    private ArrayList<SearchRecord> getSearchRecords() {
        ArrayList<SearchRecord> list = new ArrayList<>();

        for (int index = currentPage - 1; index <= currentPage + 1; index++) {
            SearchRecord searchRecord = searchRecords.get(index);
            if (searchRecord != null) {
                list.add(searchRecord);
            }
        }

        return list;
    }
}
