package cn.imeina.screeencaptureshare;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.IdRes;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;
import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * 截图编辑分享
 * Created by AKid on 2017/6/6.
 */

public class ScreenCaptureShareActivity extends Activity implements View.OnClickListener, ScreenCaptureEditerView.ScreenCaptureCallback {

    private static final String TAG = ScreenCaptureEditerView.class.getSimpleName();
    private static final String SCREENSHOT_PATH = Environment.getExternalStorageDirectory() + File.separator + "screenshot.png";
    private static final String SCREENSHOT_SHARE_PATH = Environment.getExternalStorageDirectory() + File.separator + "share_screenshot.png";
    private ScreenCaptureEditerView editerView;
    private LinearLayout llClose, llArrows, llBight, llRevocation, llShare;
    private LinearLayout llPalette;
    private PopupWindow popStrokeWidth;
    private PopupWindow popColorPalette;
    private List<ImageView> popStrokeWidthList;
    private List<RadioButton> popColorPaletteList;
    private int previousCheckedStrokeWidthIndex, currentCheckedStrokeWidthIndex, currentCheckedColorIndex;
    private View strokeWidthContentView, colorPaletteContentView;
    private ImageView ivArrows, ivBight, ivPalette, ivRevocation;
    private boolean isNoOperate = true;

    /**
     * 截图分享
     * @param view 任意View
     * @param offset 偏移量
     */
    public static void screenCapture(final View view, final int offset) {
        final Context context = view.getContext();
        final View rootView = view.getRootView();
        rootView.setDrawingCacheEnabled(true);
        rootView.buildDrawingCache();
        final Bitmap bitmap = Bitmap.createBitmap(rootView.getDrawingCache());
        rootView.setDrawingCacheEnabled(false);  //禁用DrawingCahce否则会影响性能
        new Thread(
                new Runnable() {
                    @Override
                    public void run() {
                        if (bitmap != null) {
                            try {
                                File file = new File(SCREENSHOT_PATH);
                                if (file.exists())
                                    file.delete();
                                Uri uri = Uri.fromFile(file);
                                FileOutputStream out = new FileOutputStream(file);
                                bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);

                                Intent intent = new Intent(context, ScreenCaptureShareActivity.class);
                                intent.putExtra("scUri", uri);
                                intent.putExtra("offset", offset);
                                context.startActivity(intent);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        } else {
                            System.out.println("bitmap is NULL!");
                        }
                    }
                }
        ).start();
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_share);
        initView();
    }

    private void initView() {

        editerView = (ScreenCaptureEditerView) findViewById(R.id.sc_editer);
        llClose = (LinearLayout) findViewById(R.id.ll_close);
        llArrows = (LinearLayout) findViewById(R.id.ll_arrows);
        llBight = (LinearLayout) findViewById(R.id.ll_bight);
        llPalette = (LinearLayout) findViewById(R.id.ll_palette);
        llRevocation = (LinearLayout) findViewById(R.id.ll_revocation);
        llShare = (LinearLayout) findViewById(R.id.ll_screencapture_share);

        ivArrows = (ImageView) findViewById(R.id.iv_arrows);
        ivBight = (ImageView) findViewById(R.id.iv_bight);
        ivPalette = (ImageView) findViewById(R.id.iv_palette_color);
        ivRevocation = (ImageView) findViewById(R.id.iv_revocation);
        ivArrows.setSelected(true);
        ivRevocation.setEnabled(false);

        llClose.setOnClickListener(this);
        llArrows.setOnClickListener(this);
        llBight.setOnClickListener(this);
        llPalette.setOnClickListener(this);
        llRevocation.setOnClickListener(this);
        llShare.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {

        if (v.getId() == R.id.ll_close) {
            //关闭
            showCloseDialog();
        } else if (v.getId() == R.id.ll_arrows) {
            //箭头直线
            editerView.setArrowsLine(true);
            ivArrows.setSelected(true);
            ivBight.setSelected(false);
        } else if (v.getId() == R.id.ll_bight) {
            //涂鸦曲线
            editerView.setArrowsLine(false);
            ivArrows.setSelected(false);
            ivBight.setSelected(true);
            showPaintStrokeWidth();
        } else if (v.getId() == R.id.ll_palette) {
            //调色盘
            showPalette();
        } else if (v.getId() == R.id.ll_revocation) {
            //撤销
            editerView.revocation();
        } else if (v.getId() == R.id.ll_screencapture_share) {
            //合成分享图片...
            editerView.compound();
        } else if (v.getId() == R.id.iv_size_one) {
            popStrokeWidthList.get(previousCheckedStrokeWidthIndex).setSelected(false);
            previousCheckedStrokeWidthIndex = 0;
            currentCheckedStrokeWidthIndex = 0;
            editerView.setStrokeWidth(ScreenCaptureEditerView.PaintStrokeWidth.SIZE_ONE);
            popStrokeWidth.dismiss();
        } else if (v.getId() == R.id.iv_size_two) {
            popStrokeWidthList.get(previousCheckedStrokeWidthIndex).setSelected(false);
            previousCheckedStrokeWidthIndex = 1;
            currentCheckedStrokeWidthIndex = 1;
            editerView.setStrokeWidth(ScreenCaptureEditerView.PaintStrokeWidth.SIZE_TWO);
            popStrokeWidth.dismiss();
        } else if (v.getId() == R.id.iv_size_three) {
            popStrokeWidthList.get(previousCheckedStrokeWidthIndex).setSelected(false);
            previousCheckedStrokeWidthIndex = 2;
            currentCheckedStrokeWidthIndex = 2;
            editerView.setStrokeWidth(ScreenCaptureEditerView.PaintStrokeWidth.SIZE_THREE);
            popStrokeWidth.dismiss();
        } else if (v.getId() == R.id.iv_size_four) {
            popStrokeWidthList.get(previousCheckedStrokeWidthIndex).setSelected(false);
            previousCheckedStrokeWidthIndex = 3;
            currentCheckedStrokeWidthIndex = 3;
            editerView.setStrokeWidth(ScreenCaptureEditerView.PaintStrokeWidth.SIZE_FOUR);
            popStrokeWidth.dismiss();
        } else if (v.getId() == R.id.iv_size_five) {
            popStrokeWidthList.get(previousCheckedStrokeWidthIndex).setSelected(false);
            previousCheckedStrokeWidthIndex = 4;
            currentCheckedStrokeWidthIndex = 4;
            editerView.setStrokeWidth(ScreenCaptureEditerView.PaintStrokeWidth.SIZE_FIVE);
            popStrokeWidth.dismiss();
        }
    }

    /**
     * 放弃分享
     */
    private void showCloseDialog() {

        if (isNoOperate) {
            finish();
        } else {
            final String[] items = new String[]{"分享", "放弃分享", "继续编辑"};
            new AlertDialog.Builder(this).setTitle("提示").setItems(items, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {

                    switch (which) {
                        case 0:
                            //合成分享图片...
                            editerView.compound();
                            dialog.dismiss();
                            break;
                        case 1:

                            dialog.dismiss();
                            finish();
                            break;
                        case 2:
                            dialog.dismiss();
                            break;
                    }
                }
            }).show();
        }
    }

    /**
     * 选择曲线画笔宽度
     */
    private void showPaintStrokeWidth() {

        if (popStrokeWidth == null) {
            popStrokeWidthList = new ArrayList<>();
            strokeWidthContentView = LayoutInflater.from(this).inflate(R.layout.layout_pop_size, null);
            ImageView ivSizeOne = (ImageView) strokeWidthContentView.findViewById(R.id.iv_size_one);
            ImageView ivSizeTwo = (ImageView) strokeWidthContentView.findViewById(R.id.iv_size_two);
            ImageView ivSizeThree = (ImageView) strokeWidthContentView.findViewById(R.id.iv_size_three);
            ImageView ivSizeFour = (ImageView) strokeWidthContentView.findViewById(R.id.iv_size_four);
            ImageView ivSizeFive = (ImageView) strokeWidthContentView.findViewById(R.id.iv_size_five);
            ivSizeOne.setOnClickListener(this);
            ivSizeTwo.setOnClickListener(this);
            ivSizeThree.setOnClickListener(this);
            ivSizeFour.setOnClickListener(this);
            ivSizeFive.setOnClickListener(this);
            popStrokeWidthList.add(ivSizeOne);
            popStrokeWidthList.add(ivSizeTwo);
            popStrokeWidthList.add(ivSizeThree);
            popStrokeWidthList.add(ivSizeFour);
            popStrokeWidthList.add(ivSizeFive);
            popStrokeWidth = new PopupWindow();
            popStrokeWidth.setContentView(strokeWidthContentView);
            popStrokeWidth.setWidth(ViewGroup.LayoutParams.WRAP_CONTENT);
            popStrokeWidth.setHeight(ViewGroup.LayoutParams.WRAP_CONTENT);
            //外部是否可以点击
            popStrokeWidth.setBackgroundDrawable(new ColorDrawable());
            popStrokeWidth.setOutsideTouchable(true);
        } else {

            if (popStrokeWidth.isShowing()) {

                popStrokeWidth.dismiss();
                return;
            }
        }
        //回显选中...
        popStrokeWidthList.get(currentCheckedStrokeWidthIndex).setSelected(true);
        int[] location = PopupWindowUtil.calculatePopWindowPos(this, llBight, strokeWidthContentView);
        popStrokeWidth.showAtLocation(llBight, Gravity.NO_GRAVITY, location[0], location[1]);
    }

    /**
     * 显示调色盘
     */
    private void showPalette() {

        int[] colors = editerView.getColors();
        if (popColorPalette == null) {
            popColorPaletteList = new ArrayList<>();
            colorPaletteContentView = LayoutInflater.from(this).inflate(R.layout.layout_pop_color, null);
            RadioGroup rgColorPalette = (RadioGroup) colorPaletteContentView.findViewById(R.id.rg_color_palette);
            RadioButton rbColorOne = (RadioButton) colorPaletteContentView.findViewById(R.id.rb_color_one);
            RadioButton rbColorTwo = (RadioButton) colorPaletteContentView.findViewById(R.id.rb_color_two);
            RadioButton rbColorThree = (RadioButton) colorPaletteContentView.findViewById(R.id.rb_color_three);
            RadioButton rbColorFour = (RadioButton) colorPaletteContentView.findViewById(R.id.rb_color_four);
            RadioButton rbColorFive = (RadioButton) colorPaletteContentView.findViewById(R.id.rb_color_five);
            popColorPaletteList.add(rbColorOne);
            popColorPaletteList.add(rbColorTwo);
            popColorPaletteList.add(rbColorThree);
            popColorPaletteList.add(rbColorFour);
            popColorPaletteList.add(rbColorFive);
            rgColorPalette.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(RadioGroup group, @IdRes int checkedId) {
                    if (checkedId == R.id.rb_color_one) {
                        editerView.setColor(0);
                        currentCheckedColorIndex = 0;
                        ivPalette.setImageResource(R.drawable.ic_palette_color_one);
                    } else if (checkedId == R.id.rb_color_two) {
                        editerView.setColor(1);
                        currentCheckedColorIndex = 1;
                        ivPalette.setImageResource(R.drawable.ic_palette_color_two);
                    } else if (checkedId == R.id.rb_color_three) {
                        editerView.setColor(2);
                        currentCheckedColorIndex = 2;
                        ivPalette.setImageResource(R.drawable.ic_palette_color_three);
                    } else if (checkedId == R.id.rb_color_four) {
                        editerView.setColor(3);
                        currentCheckedColorIndex = 3;
                        ivPalette.setImageResource(R.drawable.ic_palette_color_four);
                    } else if (checkedId == R.id.rb_color_five) {
                        editerView.setColor(4);
                        currentCheckedColorIndex = 4;
                        ivPalette.setImageResource(R.drawable.ic_palette_color_five);
                    }
                    popColorPalette.dismiss();
                }
            });
            popColorPalette = new PopupWindow();
            popColorPalette.setContentView(colorPaletteContentView);
            popColorPalette.setWidth(ViewGroup.LayoutParams.WRAP_CONTENT);
            popColorPalette.setHeight(ViewGroup.LayoutParams.WRAP_CONTENT);
            //外部是否可以点击
            popColorPalette.setBackgroundDrawable(new ColorDrawable());
            popColorPalette.setOutsideTouchable(true);
        } else {

            if (popColorPalette.isShowing()) {

                popColorPalette.dismiss();
                return;
            }
        }
        popColorPaletteList.get(currentCheckedColorIndex).setChecked(true);
        int[] location = PopupWindowUtil.calculatePopWindowPos(this, llPalette, colorPaletteContentView);
        popColorPalette.showAtLocation(llPalette, Gravity.NO_GRAVITY, location[0], location[1]);
    }

    @Override
    public void onAbleRepeal(boolean enableRepeal) {

        isNoOperate = !enableRepeal;
        ivRevocation.setEnabled(enableRepeal);
    }

    @Override
    public void onCompoundSuccess(final String shareImgPath) {

        //此方法中不能进行UI更新.....因为是在子线程进行的回调
        //弹出分享面板...
        runOnUiThread(new Runnable() {
            @Override
            public void run() {

                Toast.makeText(ScreenCaptureShareActivity.this, "在此调用分享...", Toast.LENGTH_SHORT).show();
            }
        });
        Log.e(TAG, "图像个合并成功，路径：" + shareImgPath);
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        //获取状态栏高度
        Rect rectangle = new Rect();
        getWindow().getDecorView().getWindowVisibleDisplayFrame(rectangle);
        int statusHeight = rectangle.top;
        //开启截屏编辑
        Intent intent = getIntent();
        Uri scUri = intent.getParcelableExtra("scUri");
        int offset = intent.getIntExtra("offset", 0);
        editerView.init(scUri, SCREENSHOT_SHARE_PATH, statusHeight, offset);
        editerView.setCallback(this);
    }

    public boolean onKeyDown(int keyCode, KeyEvent event) {

        if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
            showCloseDialog();
            //这里重写返回键
            return true;
        }
        return false;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
