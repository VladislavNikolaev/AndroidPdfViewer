package com.github.barteksc.pdfviewer;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;

public class SelectionDrawer {

    private int pageIndexStart;
    private int pageIndexEnd;
    private int positionStart;
    private int positionEnd;
    private Drawable handleLeft;
    private Drawable handleRight;
    final RectF handleLeftPos = new RectF();
    final RectF handleRightPos = new RectF();
    private boolean hasSelection;

    public SelectionDrawer(Context context) {
        handleLeft = context.getResources().getDrawable(R.drawable.abc_text_select_handle_left_mtrl_dark);
        handleRight = context.getResources().getDrawable(R.drawable.abc_text_select_handle_right_mtrl_dark);
        ColorFilter colorFilter = new PorterDuffColorFilter(0xaa309afe, PorterDuff.Mode.SRC_IN);
        handleLeft.setColorFilter(colorFilter);
        handleRight.setColorFilter(colorFilter);
        handleLeft.setAlpha(255);
        handleRight.setAlpha(255);
    }

    public boolean hasSelection() {
        return hasSelection;
    }

    public void setHasSelection(boolean hasSelection) {
        this.hasSelection = hasSelection;
    }

    public void onDraw(Canvas canvas) {
//        if (pdfView.hasSelection) {
//            pdfView.sourceToViewRectFF(pdfView.handleLeftPos, VR);
//            float left = VR.left + drawableDeltaW;
//            pdfView.handleLeft.setBounds((int) (left - drawableWidth), (int) VR.bottom, (int) left, (int) (VR.bottom + drawableHeight));
//            pdfView.handleLeft.draw(canvas);
//            //canvas.drawRect(pDocView.handleLeft.getBounds(), rectPaint);
//
//            pdfView.sourceToViewRectFF(pdfView.handleRightPos, VR);
//            left = VR.right - drawableDeltaW;
//            pdfView.handleRight.setBounds((int) left, (int) VR.bottom, (int) (left + drawableWidth), (int) (VR.bottom + drawableHeight));
//            pdfView.handleRight.draw(canvas);
//
//            // canvas.drawRect(pDocView.handleRight.getBounds(), rectPaint);
//            pdfView.sourceToViewCoord(pdfView.sCursorPos, vCursorPos);
//
//            for (int i = 0; i < rectPoolSize; i++) {
//
//                ArrayList<RectF> rectPage = rectPool.get(i);
//                for (RectF rI : rectPage) {
//                    pdfView.sourceToViewRectFF(rI, VR);
//                    matrix.reset();
//                    int bmWidth = (int) rI.width();
//                    int bmHeight = (int) rI.height();
//                    pdfView.setMatrixArray(pdfView.srcArray, 0, 0, bmWidth, 0, bmWidth, bmHeight, 0, bmHeight);
//                    pdfView.setMatrixArray(pdfView.dstArray, VR.left, VR.top, VR.right, VR.top, VR.right, VR.bottom, VR.left, VR.bottom);
//
//                    matrix.setPolyToPoly(pdfView.srcArray, 0, pdfView.dstArray, 0, 4);
//                    matrix.postRotate(0, pdfView.getScreenWidth(), pdfView.getScreenHeight());
//
//                    canvas.save();
//                    canvas.concat(matrix);
//                    VR.set(0, 0, bmWidth, bmHeight);
//                    canvas.drawRect(VR, rectPaint);
//                    canvas.restore();
//                }
//            }
//        }
    }

    public void setSelectionAtPage(int pageIdx, int start, int end) {
        pageIndexStart = pageIdx;
        pageIndexEnd = pageIdx;
        positionStart = start;
        positionEnd = end;
        hasSelection = true;
    }

    public int getCurrentSelectedPage() {
        return pageIndexStart;
    }
}
