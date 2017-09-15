package cn.imeina.screeencaptureshare;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.RectF;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * 屏幕截图编辑
 * Created by AKid on 2017/6/6.
 */

public class ScreenCaptureEditerView extends View {

    private static final String TAG = ScreenCaptureEditerView.class.getSimpleName();

    private Context mContext;
    /**
     * 原始画布
     */
    private Canvas canvas;
    /**
     * 画布
     */
    private Canvas mCanvas;
    /**
     * 画笔调色盘
     */
    private String COLORS[];
    /**
     * 箭头直线画笔
     */
    private Paint mBightPaint;
    /**
     * 画笔颜色
     */
    private int mColor;
    /**
     * 曲线画笔宽度
     */
    private int mBightStrokeWidth = PaintStrokeWidth.SIZE_ONE.value();
    /**
     * 箭头直线画笔
     */
    private Paint mArrowsLinePaint;
    /**
     * 箭头直线画笔宽度
     */
    private int mALStrokeWidth = 4;
    /**
     * 是否画箭头线
     */
    private boolean isArrowsLine = true;
    /**
     * 截图图片Uri
     */
    private Uri scUri;
    /**
     * 分享图片路径
     */
    private String sharePath;
    /**
     * 屏幕状态栏高度
     */
    private int statusHeight;
    /**
     * 指标高度作为偏移量
     */
    private int offset;
    /**
     * 截图图片
     */
    private Bitmap mBitmap;
    /**
     * 手指按下位置X
     */
    int startX = 0;
    /**
     * 手指按下位置X
     */
    int startY = 0;
    /**
     * 手指离开|移动位置X
     */
    int endX = 0;
    /**
     * 手指离开|移动位置Y
     */
    int endY = 0;
    /**
     * 曲线路径
     */
    private Path mPath;
    /**
     * 手指是否处于屏幕中
     */
    private boolean isTouch;
    /**
     * 画笔路径（用于撤销）
     */
    private List<DrawPath> drawPaths;
    /**
     * 画线路径
     */
    private DrawPath drawPath;
    private Rect rect;
    private RectF dstRectF;

    /**
     * 画笔宽度
     */
    enum PaintStrokeWidth {

        SIZE_ONE(10),
        SIZE_TWO(20),
        SIZE_THREE(30),
        SIZE_FOUR(40),
        SIZE_FIVE(45);

        private int value = 0;

        private PaintStrokeWidth(int value) {
            this.value = value;
        }

        public int value() {
            return this.value;
        }
    }

    /**
     * 绘制路径
     */
    private class DrawPath {

        public Paint paint;
        public Path path;
    }

    /**
     * 回调接口
     */
    private ScreenCaptureCallback callback;

    public void setCallback(ScreenCaptureCallback callback) {
        this.callback = callback;
    }

    public ScreenCaptureEditerView(Context context) {
        this(context, null);
    }

    public ScreenCaptureEditerView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ScreenCaptureEditerView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.mContext = context;
    }

    public void init(Uri scUri, String sharePath, int statusHeight, int offset) {
        this.scUri = scUri;
        this.sharePath = sharePath;
        this.statusHeight = statusHeight;
        this.offset = offset;
        mBitmap = getBitmap(scUri, false);
        mCanvas = new Canvas(mBitmap);
        COLORS = getResources().getStringArray(R.array.screen_capture_editer_color);
        mColor = Color.parseColor(COLORS[0]);

        //初始化画笔
        mArrowsLinePaint = createPaint(true);
        mBightPaint = createPaint(false);

        drawPaths = new ArrayList<>();

        rect = new Rect();
        rect.left = 0;
        rect.top = statusHeight;
        rect.right = getWidth();
        rect.bottom = mBitmap.getHeight();
        //绘制截图的一部分，底部菜单按钮不绘制
        dstRectF = new RectF();
        dstRectF.left = 0;
        dstRectF.top = 0;
        dstRectF.right = getWidth();
        dstRectF.bottom = mBitmap.getHeight()-statusHeight;
//        postInvalidate();
    }

    /**
     * 初始化图像
     *
     * @param canvas
     */
    @Override
    protected void onDraw(Canvas canvas) {
        this.canvas = canvas;
        Log.e(TAG, "ScreenCapture onDraw()...");

        if (mBitmap == null)
            return;
        canvas.drawBitmap(mBitmap, rect, dstRectF, null);
        Log.e(TAG, "Bitmap：" + mBitmap.getWidth() + "," + mBitmap.getHeight() + ";Canvas：" + getWidth() + "," + getHeight());
        if (isArrowsLine && isTouch) {

            canvas.drawPath(mPath, mArrowsLinePaint);
        }
    }

    /**
     * 获取Bitmap
     *
     * @param uri
     * @param isLogo
     * @return
     */
    private Bitmap getBitmap(Uri uri, boolean isLogo) {
        Bitmap bitmap = null;
        try {
            if (isLogo) {

                bitmap = BitmapFactory.decodeResource(this.getContext().getResources(), R.drawable.bg_share_logo);
            } else {
                // 读取uri所在的图片
                bitmap = MediaStore.Images.Media.getBitmap(mContext.getContentResolver(), uri);
            }
            //拷贝图片进行编辑修改（否则会出现崩溃）
            return bitmap.copy(Bitmap.Config.ARGB_8888, true);
        } catch (Exception e) {
            e.printStackTrace();
            throw new Resources.NotFoundException();
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {

        switch (event.getAction()) {

            case MotionEvent.ACTION_DOWN: {

//                isTouch = true;
                startX = (int) event.getX();
                startY = (int) event.getY();

                mPath = new Path();
                drawPath = new DrawPath();
                drawPath.paint = createPaint(isArrowsLine);
                drawPath.path = mPath;
                if (!isArrowsLine) {

                    mPath.moveTo(startX, startY);
                }
                return true;
            }
            case MotionEvent.ACTION_MOVE:

                isTouch = true;
                endX = (int) event.getX();
                endY = (int) event.getY();
                if (!isArrowsLine) {

                    //绘制曲线路径
                    mPath.lineTo(endX, endY);
                    mCanvas.drawPath(mPath, mBightPaint);
                }
                break;
            case MotionEvent.ACTION_UP:

                isTouch = false;
                endX = (int) event.getX();
                endY = (int) event.getY();
                if (!isArrowsLine) {

                    drawPaths.add(drawPath);
                    if (callback != null)
                        callback.onAbleRepeal(true);
                }
                break;
        }
        if (isArrowsLine) {//绘制箭头直线
            drawAL(startX, startY, endX, endY);
        }
        postInvalidate();
        return true;
    }

    /**
     * 画箭头
     *
     * @param sx
     * @param sy
     * @param ex
     * @param ey
     */
    public void drawAL(int sx, int sy, int ex, int ey) {
        Log.e(TAG, sx + "," + sy + "," + ex + "," + ey);

        if (!isTouch){
            sy += statusHeight;
            ey += statusHeight;
        }
        double H = 25; // 箭头高度
        double L = 10; // 底边的一半
        int x3 = 0;
        int y3 = 0;
        int x4 = 0;
        int y4 = 0;
        double awrad = Math.atan(L / H); // 箭头角度
        double arraow_len = Math.sqrt(L * L + H * H); // 箭头的长度
        double[] arrXY_1 = rotateVec(ex - sx, ey - sy, awrad, true, arraow_len);
        double[] arrXY_2 = rotateVec(ex - sx, ey - sy, -awrad, true, arraow_len);
        double x_3 = ex - arrXY_1[0]; // (x3,y3)是第一端点
        double y_3 = ey - arrXY_1[1];
        double x_4 = ex - arrXY_2[0]; // (x4,y4)是第二端点
        double y_4 = ey - arrXY_2[1];
        Double X3 = new Double(x_3);
        x3 = X3.intValue();
        Double Y3 = new Double(y_3);
        y3 = Y3.intValue();
        Double X4 = new Double(x_4);
        x4 = X4.intValue();
        Double Y4 = new Double(y_4);
        y4 = Y4.intValue();

        if (sx == ex && sy == ey) {
            return;
        }
        mPath.reset();
        mPath.moveTo(sx, sy);
        mPath.lineTo(ex, ey);
        mPath.lineTo(x3, y3);
        mPath.moveTo(ex, ey);
        mPath.lineTo(x4, y4);
        if (!isTouch) {
            mCanvas.drawPath(mPath, mArrowsLinePaint);
            drawPaths.add(drawPath);
            if (callback != null)
                callback.onAbleRepeal(true);
        }
    }

    // 计算箭头角度
    public double[] rotateVec(int px, int py, double ang, boolean isChLen, double newLen) {
        double mathstr[] = new double[2];
        // 矢量旋转函数，参数含义分别是x分量、y分量、旋转角、是否改变长度、新长度
        double vx = px * Math.cos(ang) - py * Math.sin(ang);
        double vy = px * Math.sin(ang) + py * Math.cos(ang);
        if (isChLen) {
            double d = Math.sqrt(vx * vx + vy * vy);
            vx = vx / d * newLen;
            vy = vy / d * newLen;
            mathstr[0] = vx;
            mathstr[1] = vy;
        }
        return mathstr;
    }

    /**
     * 设置曲线画笔宽度
     *
     * @param mStrokeWidth
     */
    public void setStrokeWidth(PaintStrokeWidth mStrokeWidth) {
        this.mBightStrokeWidth = mStrokeWidth.value();
        mBightPaint.setStrokeWidth(mBightStrokeWidth);
    }

    public int getStrokeWidth() {
        return mBightStrokeWidth;
    }

    /**
     * 设置曲线画笔颜色
     *
     * @param index
     */
    public void setColor(int index) {
        this.mColor = Color.parseColor(COLORS[index]);
        mBightPaint.setColor(mColor);
        mArrowsLinePaint.setColor(mColor);
    }

    /**
     * 获取画笔颜色组
     *
     * @return
     */
    public int[] getColors() {

        int[] colors = new int[COLORS.length];
        for (int i = 0; i < COLORS.length; i++) {
            colors[i] = Color.parseColor(COLORS[i]);
        }
        return colors;
    }

    /**
     * 设置绘制箭头直线Or曲线
     *
     * @param arrowsLine
     */
    public void setArrowsLine(boolean arrowsLine) {
        isArrowsLine = arrowsLine;
    }

    private Paint createPaint(boolean isArrowsLine) {

        //初始化画笔
        Paint paint = new Paint();
        paint.setStrokeWidth(isArrowsLine ? mALStrokeWidth : mBightStrokeWidth);
        paint.setAntiAlias(true);
        paint.setColor(mColor);
        paint.setStyle(Paint.Style.STROKE);
        //设置线帽ROUND圆 SQUARE方 BUTT无
        paint.setStrokeCap(isArrowsLine ? Paint.Cap.BUTT : Paint.Cap.ROUND);
        return paint;
    }

    /**
     * 撤销|恢复
     */
    public void revocation() {

        if (drawPaths != null && drawPaths.size() > 0) {

            if (callback != null) {

                callback.onAbleRepeal(drawPaths.size() != 1);
            }
            drawPaths.remove(drawPaths.size() - 1);
            if (mBitmap != null && !mBitmap.isRecycled()) {
                mBitmap.recycle();
                mBitmap = null;
            }
//            System.gc();
            mBitmap = getBitmap(scUri, false);
            mCanvas.setBitmap(mBitmap);// 清空画布  重新设置画布
            Iterator<DrawPath> it = drawPaths.iterator();
            while (it.hasNext()) {
                DrawPath drawPath = it.next();
                mCanvas.drawPath(drawPath.path, drawPath.paint);
                Log.e(TAG, "恢复，重绘...");
            }
        }
        postInvalidate();
    }

    /**
     * 图片合成
     */
    public void compound() {
        //获取底部logo
        Bitmap logoBitmap = getBitmap(null, true);
        //获取截图宽、截图+Logo高
        int width = mBitmap.getWidth();
        int height = mBitmap.getHeight();
        Log.e(TAG, "截屏宽高：" + width + "," + height);
        //创建宽度为截图宽、高度为截图+Logo高和的空Bitmap并画在新的画布上
        Bitmap result = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(result);
        //画处理过的截图
        canvas.drawBitmap(mBitmap, rect, dstRectF, null);
//        if (offset != 0){
            //去除分时、两日分时下部阴影线
            Paint paint = new Paint();
            paint.setColor(Color.WHITE);
            paint.setAntiAlias(true);
            paint.setStyle(Paint.Style.FILL_AND_STROKE);
            canvas.drawRect(new RectF(0, mBitmap.getHeight() - logoBitmap.getHeight(), mBitmap.getWidth(), mBitmap.getHeight()), paint);
//        }
        if (offset != 0)
            offset -= 30;
        //拼接logo
        canvas.drawBitmap(logoBitmap, (width - logoBitmap.getWidth()) / 2, mBitmap.getHeight() - logoBitmap.getHeight()-offset/2-20, null);
        //保存并写入到SD卡...
        canvas.save();
        saveToSDCard(result);
    }

    /**
     * 合并保存到SD卡
     *
     * @param bitmap
     */
    public void saveToSDCard(final Bitmap bitmap) {

        new Thread(
                new Runnable() {
                    @Override
                    public void run() {
                        try {
                            FileOutputStream fos = new FileOutputStream(new File(sharePath));
                            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
                            fos.flush();
                            fos.close();
                            if (callback != null)
                                callback.onCompoundSuccess(sharePath);
                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
        ).start();
    }

    public interface ScreenCaptureCallback {

        /**
         * 是否可以撤销
         *
         * @param enableRepeal
         */
        void onAbleRepeal(boolean enableRepeal);

        /**
         * 图像合成成功
         *
         * @param shareImgPath 最终图像路径
         */
        void onCompoundSuccess(String shareImgPath);
    }
}
