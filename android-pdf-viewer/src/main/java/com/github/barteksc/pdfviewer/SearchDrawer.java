package com.github.barteksc.pdfviewer;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;
import android.util.SparseArray;
import android.view.View;

import androidx.annotation.NonNull;

import java.util.ArrayList;

public class SearchDrawer {

    private final Matrix matrix = new Matrix();

    private SparseArray<SearchRecord> searchRecords = new SparseArray<>();
    private ArrayList<SearchRecordItem> searchRecordItems = new ArrayList<>();
    private final RectF tempRect = new RectF(0, 0, 256, 256);
    private final RectF tempPosRect = new RectF();
    private final View view;

    private float scale;
    private final Paint rectHighlightPaint = new Paint();
    private final Paint currentRectHighlightPaint = new Paint();
    final float[] srcArray = new float[8];
    final float[] dstArray = new float[8];
    private int currentPage;
    private int currentSearchRecordItemIndex = -1;

    private float currentXOffset = 0;
    private float currentYOffset = 0;

    public SearchDrawer(View view) {
        this.view = view;
        matrix.mapRect(tempRect);
        rectHighlightPaint.setColor(0x80ffff00);
        rectHighlightPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DARKEN));

        currentRectHighlightPaint.setColor(Color.RED);
        currentRectHighlightPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DARKEN));
    }

    public void onDraw(Canvas canvas, PdfFile pdfFile) {
        for (SearchRecord searchRecord : getSearchRecords()) {
            for (SearchRecordItem searchRecordItem : searchRecord.getSearchRecordItems()) {
                drawSearchRecordItem(canvas, rectHighlightPaint, pdfFile, searchRecordItem);
            }
        }

        if (currentSearchRecordItemIndex >= 0) {
            SearchRecordItem searchRecordItem = searchRecordItems.get(currentSearchRecordItemIndex);
            drawSearchRecordItem(canvas, currentRectHighlightPaint, pdfFile, searchRecordItem);
        }
    }

    private void drawSearchRecordItem(Canvas canvas, Paint paint, PdfFile pdfFile, SearchRecordItem searchRecordItem) {
        RectF[] rects = searchRecordItem.rects;
        for (RectF rect : rects) {
            drawRect(canvas, paint, rect, pdfFile, searchRecordItem.getPageIdx());
        }
    }

    private void drawRect(Canvas canvas, Paint paint, RectF rect, PdfFile pdfFile, int page) {
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
        canvas.drawRect(tempPosRect, paint);
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
        searchRecordItems.clear();
        for (int i = 0; i < searchRecords.size(); i++) {
            SearchRecord searchRecord = searchRecords.valueAt(i);
            searchRecordItems.addAll(searchRecord.getSearchRecordItems());
        }
        setInitialValueForCurrentSearchRecordItemIndex();
    }

    public void clear() {
        currentSearchRecordItemIndex = -1;
        searchRecordItems.clear();
        searchRecords.clear();
    }

    public int showNextSearchRecordItem() {
        currentSearchRecordItemIndex++;
        if (currentSearchRecordItemIndex >= searchRecordItems.size()) {
            setInitialValueForCurrentSearchRecordItemIndex();
        }
        if (currentSearchRecordItemIndex < searchRecordItems.size()) {
            return searchRecordItems.get(currentSearchRecordItemIndex).getPageIdx();
        }

        return -1;
    }

    public int showPreviousSearchRecordItem() {
        currentSearchRecordItemIndex--;
        if (currentSearchRecordItemIndex < 0) {
            if (searchRecordItems.isEmpty())
                currentSearchRecordItemIndex = -1;
            else
                currentSearchRecordItemIndex = searchRecordItems.size() - 1;
        }

        if (currentSearchRecordItemIndex < searchRecordItems.size()) {
            return searchRecordItems.get(currentSearchRecordItemIndex).getPageIdx();
        }

        return -1;
    }

    private void setInitialValueForCurrentSearchRecordItemIndex() {
        if (searchRecordItems.isEmpty())
            currentSearchRecordItemIndex = -1;
        else
            currentSearchRecordItemIndex = 0;
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
