package com.github.barteksc.pdfviewer;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;
import android.os.Build;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

import com.github.barteksc.pdfviewer.util.PdfUtils;

import java.util.ArrayList;

/**
 * A View to paint PDF selections, [magnifier] and search highlights
 */
public class PDFSelectionView extends View {

    private final PointF vCursorPos = new PointF();
    private final RectF tmpPosRct = new RectF();
    final RectF handleLeftPos = new RectF();
    final RectF handleRightPos = new RectF();
    public boolean shouldIgnoreInvalidate;
    PDFView pdfView;
    float drawableWidth = 60;
    float drawableHeight = 30;
    float drawableDeltaW = drawableWidth / 4;
    Paint rectPaint;
    Paint rectFramePaint;
    Paint rectHighlightPaint;

    int rectPoolSize = 0;

    ArrayList<ArrayList<RectF>> rectPool = new ArrayList<>();

    public PDFSelectionView(Context context) {
        super(context);
    }

    public PDFSelectionView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public PDFSelectionView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public PDFSelectionView(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }


    public void init(PDFView pdfView) {
        rectPaint = new Paint();
        rectPaint.setColor(0x66109afe);
        rectHighlightPaint = new Paint();
        rectHighlightPaint.setColor(getResources().getColor(R.color.colorHighlight));
        rectPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DARKEN));
        rectHighlightPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DARKEN));
        rectFramePaint = new Paint();
        rectFramePaint.setColor(0xccc7ab21);
        rectFramePaint.setStyle(Paint.Style.STROKE);
        rectFramePaint.setStrokeWidth(0.5f);
        this.pdfView = pdfView;
        drawableWidth = PdfUtils.getDP(getContext(), (int) drawableWidth);
        drawableHeight = PdfUtils.getDP(getContext(), (int) drawableHeight);
        drawableDeltaW = drawableWidth / 4;
    }

    public ArrayList<ArrayList<RectF>> getPagePool(int pageCount) {
        int poolSize = rectPool.size();
        ArrayList<RectF> rectPagePool;
        for (int i = 0; i <= pageCount; i++) {
            if (i >= poolSize) {
                rectPagePool = new ArrayList<>();
                rectPool.add(rectPagePool);
            }
        }

        if (pdfView.hasSelection) {
            rectPoolSize = pageCount + 1;
        } else {
            rectPoolSize = 0;
        }
        return rectPool;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (pdfView == null) {
            return;
        }
        RectF VR = tmpPosRct;
        Matrix matrix = pdfView.matrix;


        if (pdfView.hasSelection) {
            pdfView.sourceToViewRectFF(handleLeftPos, VR);
            float left = VR.left + drawableDeltaW;
            pdfView.handleLeft.setBounds((int) (left - drawableWidth), (int) VR.bottom, (int) left, (int) (VR.bottom + drawableHeight));
            pdfView.handleLeft.draw(canvas);
            //canvas.drawRect(pDocView.handleLeft.getBounds(), rectPaint);

            pdfView.sourceToViewRectFF(handleRightPos, VR);
            left = VR.right - drawableDeltaW;
            pdfView.handleRight.setBounds((int) left, (int) VR.bottom, (int) (left + drawableWidth), (int) (VR.bottom + drawableHeight));
            pdfView.handleRight.draw(canvas);

            // canvas.drawRect(pDocView.handleRight.getBounds(), rectPaint);
            pdfView.sourceToViewCoord(pdfView.sCursorPos, vCursorPos);

            for (int i = 0; i < rectPoolSize; i++) {

                ArrayList<RectF> rectPage = rectPool.get(i);
                for (RectF rI : rectPage) {
                    pdfView.sourceToViewRectFF(rI, VR);
                    matrix.reset();
                    int bmWidth = (int) rI.width();
                    int bmHeight = (int) rI.height();
                    pdfView.setMatrixArray(pdfView.srcArray, 0, 0, bmWidth, 0, bmWidth, bmHeight, 0, bmHeight);
                    pdfView.setMatrixArray(pdfView.dstArray, VR.left, VR.top, VR.right, VR.top, VR.right, VR.bottom, VR.left, VR.bottom);

                    matrix.setPolyToPoly(pdfView.srcArray, 0, pdfView.dstArray, 0, 4);
                    matrix.postRotate(0, pdfView.getScreenWidth(), pdfView.getScreenHeight());

                    canvas.save();
                    canvas.concat(matrix);
                    VR.set(0, 0, bmWidth, bmHeight);
                    canvas.drawRect(VR, rectPaint);
                    canvas.restore();
                }
            }

        }
    }

}
