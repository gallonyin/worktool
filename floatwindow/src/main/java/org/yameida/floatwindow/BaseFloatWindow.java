package org.yameida.floatwindow;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.PixelFormat;
import android.os.Build;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.provider.Settings;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;

import com.blankj.utilcode.util.BarUtils;
import com.blankj.utilcode.util.ScreenUtils;
import com.blankj.utilcode.util.Utils;
import org.yameida.floatwindow.view.HiderView;


public abstract class BaseFloatWindow extends Service {
    protected final String TAG = this.getClass().getSimpleName();

    /**
     * 悬浮球 坐落 左 右 标记
     */
    public static final int LEFT = 0;
    public static final int RIGHT = 1;

    /**
     * 记录 logo 停放的位置，以备下次恢复
     */
    protected final String LOCATION_X = TAG + "_x";
    protected final String LOCATION_Y = TAG + "_y";

    /**
     * 停靠默认位置
     */
    protected int mDefaultLocation = RIGHT;


    /**
     * 悬浮窗 坐落 位置
     */
    protected int mHintLocation = mDefaultLocation;


    /**
     * 记录当前手指位置在屏幕上的横坐标值
     */
    private float mXInScreen;

    /**
     * 记录当前手指位置在屏幕上的纵坐标值
     */
    private float mYInScreen;

    /**
     * 记录手指按下时在屏幕上的横坐标的值
     */
    private float mXDownInScreen;

    /**
     * 记录手指按下时在屏幕上的纵坐标的值
     */
    private float mYDownInScreen;

    /**
     * 记录手指按下时在小悬浮窗的View上的横坐标的值
     */
    private float mXInView;

    /**
     * 记录手指按下时在小悬浮窗的View上的纵坐标的值
     */
    private float mYinview;

    /**
     * 记录屏幕的宽度
     */
    private int mScreenWidth;

    protected WindowManager mWindowManager;
    protected WindowManager.LayoutParams params;

    private Context context = Utils.getApp();

    /**
     * 退出悬浮窗区域
     */
    private HiderView mHiderView = new HiderView(context);
    private boolean showHider = true;


    /**
     * 用于 定时 隐藏 logo的定时器
     */
    private CountDownTimer mHideTimer;


    /**
     * float menu的高度
     */
    private Handler mHandler = new Handler(Looper.getMainLooper());


    /**
     * 悬浮窗左右移动到默认位置 动画的 加速器
     */
    private Interpolator mLinearInterpolator = new LinearInterpolator();

    /**
     * 标记是否拖动中
     */
    private boolean isDrag = false;

    /**
     * 用于恢复悬浮球的location的属性动画值
     */
    private int mResetLocationValue;

    /**
     * 限制拖动的y轴坐标
     */
    protected int minY = 0;
    protected int maxY = ScreenUtils.getScreenHeight();

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    /**
     * 这个事件用于处理移动、自定义点击或者其它事情，return true可以保证onclick事件失效
     */
    private View.OnTouchListener touchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    floatEventDown(event);
                    break;
                case MotionEvent.ACTION_MOVE:
                    floatEventMove(event);
                    break;
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                    floatEventUp();
                    break;
            }
            return true;
        }
    };

    ValueAnimator valueAnimator = null;
    protected boolean isExtended = false;

    protected View logoView;
    protected View rightView;
    protected View leftView;

    private GetViewCallback mGetViewCallback;

    protected BaseFloatWindow() {
        initFloatWindow();
        initTimer();
        initFloatView();

    }

    private void initFloatView() {
        LayoutInflater inflater = LayoutInflater.from(context);
        logoView = mGetViewCallback == null ? getLogoView(inflater) : mGetViewCallback.getLogoView(inflater);
        leftView = mGetViewCallback == null ? getLeftView(inflater) : mGetViewCallback.getLeftView(inflater);
        rightView = mGetViewCallback == null ? getRightView(inflater) : mGetViewCallback.getRightView(inflater);

        if (logoView == null) {
            throw new IllegalArgumentException("Must impl GetViewCallback or impl " + this.getClass().getSimpleName() + "and make getLogoView() not return null !");
        }

        logoView.setOnTouchListener(touchListener);//恢复touch事件
    }

    /**
     * 初始化 隐藏悬浮球的定时器
     */
    private void initTimer() {
        mHideTimer = new CountDownTimer(2000, 10) {        //悬浮窗超过5秒没有操作的话会自动隐藏
            @Override
            public void onTick(long millisUntilFinished) {
                if (isExtended) {
                    mHideTimer.cancel();
                }
            }

            @Override
            public void onFinish() {
                if (isExtended) {
                    mHideTimer.cancel();
                    return;
                }
                if (!isDrag) {
                    if (mHintLocation == LEFT) {
                        if (mGetViewCallback == null) {
                            shrinkLeftLogoView(logoView);
                        } else {
                            mGetViewCallback.shrinkLeftLogoView(logoView);
                        }
                    } else {
                        if (mGetViewCallback == null) {
                            shrinkRightLogoView(logoView);
                        } else {
                            mGetViewCallback.shrinkRightLogoView(logoView);
                        }
                    }
                }
            }
        };
    }

    /**
     * 初始化悬浮球 window
     */
    private void initFloatWindow() {
        params = new WindowManager.LayoutParams();
        mWindowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);

        if (Build.VERSION.SDK_INT >= 23) {
            if (!Settings.canDrawOverlays(context)) {
                if (Build.VERSION.SDK_INT >= 26) {
                    params.type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
                } else {
                    params.type = WindowManager.LayoutParams.TYPE_SYSTEM_ALERT;
                }
            } else {
                if (Build.VERSION.SDK_INT >= 26) {
                    params.type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
                } else {
                    params.type = WindowManager.LayoutParams.TYPE_SYSTEM_ALERT;
                }
            }
        } else {
            params.type = WindowManager.LayoutParams.TYPE_SYSTEM_ALERT;
        }

        mScreenWidth = mWindowManager.getDefaultDisplay().getWidth();
        int screenHeigth = mWindowManager.getDefaultDisplay().getHeight();
        params.format = PixelFormat.RGBA_8888;
        params.gravity = Gravity.LEFT | Gravity.TOP;
        params.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN;
        mHintLocation = getSetting(LOCATION_X, mDefaultLocation);
        int defaultY = ((screenHeigth) / 2) / 3;
        int y = getSetting(LOCATION_Y, defaultY);
        if (mHintLocation == LEFT) {
            params.x = 0;
        } else {
            params.x = mScreenWidth;
        }
        if (y != 0 && y != defaultY) {
            params.y = y;
        } else {
            params.y = defaultY;
        }
        params.alpha = 1;
        params.width = WindowManager.LayoutParams.WRAP_CONTENT;
        params.height = WindowManager.LayoutParams.WRAP_CONTENT;
    }

    /**
     * 悬浮窗touch事件的 down 事件
     */
    private void floatEventDown(MotionEvent event) {
        isDrag = false;
        mHideTimer.cancel();

        if (mGetViewCallback == null) {
            resetLogoViewSize(mHintLocation, logoView);
        } else {
            mGetViewCallback.resetLogoViewSize(mHintLocation, logoView);
        }

        mXInView = event.getX();
        mYinview = event.getY();
        mXDownInScreen = event.getRawX();
        mYDownInScreen = event.getRawY();
        mXInScreen = event.getRawX();
        mYInScreen = event.getRawY();


    }

    /**
     * 悬浮窗touch事件的 move 事件
     */
    private void floatEventMove(MotionEvent event) {
        mXInScreen = event.getRawX();
        mYInScreen = event.getRawY();

        //连续移动的距离超过3则更新一次视图位置
        if (Math.abs(mXInScreen - mXDownInScreen) > logoView.getWidth() / 4 || Math.abs(mYInScreen - mYDownInScreen) > logoView.getWidth() / 4) {
            params.x = (int) (mXInScreen - mXInView);
            params.y = (int) (mYInScreen - mYinview) - logoView.getHeight() / 2;
            updateViewPosition(); // 手指移动的时候更新小悬浮窗的位置
//            double a = mScreenWidth / 2;
//            float offset = (float) ((a - (Math.abs(params.x - a))) / a);
//            if (mGetViewCallback == null) {
//                dragLogoViewOffset(logoView, isDrag, false, offset);
//            } else {
//                mGetViewCallback.dragLogoViewOffset(logoView, isDrag, false, offset);
//            }
            mHiderView.attachToWindow();
        } else {
//            isDrag = false;
////            logoView.setDrag(false, 0, true);
//            if (mGetViewCallback == null) {
//                dragLogoViewOffset(logoView, false, true, 0);
//            } else {
//                mGetViewCallback.dragLogoViewOffset(logoView, false, true, 0);
//            }
        }
    }

    /**
     * 悬浮窗touch事件的 up 事件
     */
    private void floatEventUp() {
        if (mXInScreen < mScreenWidth / 2) {   //在左边
            mHintLocation = LEFT;
        } else {                   //在右边
            mHintLocation = RIGHT;
        }


        valueAnimator = ValueAnimator.ofInt(64);
        valueAnimator.setInterpolator(mLinearInterpolator);
        valueAnimator.setDuration(1000);
        valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                mResetLocationValue = (int) animation.getAnimatedValue();
                mHandler.post(updatePositionRunnable);
            }
        });

        valueAnimator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                if (Math.abs(params.x) < 0) {
                    params.x = 0;
                } else if (Math.abs(params.x) > mScreenWidth) {
                    params.x = mScreenWidth;
                }
                updateViewPosition();
                isDrag = false;
                if (mGetViewCallback == null) {
                    dragLogoViewOffset(logoView, false, true, 0);
                } else {
                    mGetViewCallback.dragLogoViewOffset(logoView, false, true, 0);
                }
                mHideTimer.start();
            }

            @Override
            public void onAnimationCancel(Animator animation) {
                if (Math.abs(params.x) < 0) {
                    params.x = 0;
                } else if (Math.abs(params.x) > mScreenWidth) {
                    params.x = mScreenWidth;
                }

                updateViewPosition();
                isDrag = false;
                if (mGetViewCallback == null) {
                    dragLogoViewOffset(logoView, false, true, 0);
                } else {
                    mGetViewCallback.dragLogoViewOffset(logoView, false, true, 0);
                }
                mHideTimer.start();

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
        if (!valueAnimator.isRunning()) {
            valueAnimator.start();
        }

        //这里需要判断如果如果手指所在位置和logo所在位置在一个宽度内则不移动,
        if (Math.abs(mXInScreen - mXDownInScreen) > logoView.getWidth() / 5F || Math.abs(mYInScreen - mYDownInScreen) > logoView.getHeight() / 5F) {
            isDrag = false;
        } else {
            openMenu();
        }

        mHiderView.release();
        if (mHiderView.mLp != null) {
            int lx = mScreenWidth / 2 - mHiderView.getWidth() / 2;
            int ly = mHiderView.mLp.y + BarUtils.getStatusBarHeight();
            int rx = lx + mHiderView.getWidth() + logoView.getWidth();
            int ry = ly + mHiderView.getHeight() + logoView.getHeight();
            if (mXInScreen >= lx && mXInScreen <= rx
                    && mYInScreen >= ly && mYInScreen <= ry) {
                valueAnimator.cancel();
                // 复原X
                if (mXDownInScreen < mScreenWidth / 2) {   //在左边
                    mHintLocation = LEFT;
                    params.x = 0;
                } else {                   //在右边
                    mHintLocation = RIGHT;
                    params.x = mScreenWidth;
                }
                // 复原Y
                params.y = (int) mYDownInScreen;
                // 滑动的位置在隐藏视图内
                hide();
            }
        }
    }

    /**
     * 手指离开屏幕后 用于恢复 悬浮球的 logo 的左右位置
     */
    private Runnable updatePositionRunnable = new Runnable() {
        @Override
        public void run() {
            isDrag = true;
            checkPosition();
        }
    };

    /**
     * 用于检查并更新悬浮球的位置
     */
    private void checkPosition() {
        if (params.x > 0 && params.x < mScreenWidth) {
            if (mHintLocation == LEFT) {
                params.x = params.x - mResetLocationValue;
            } else {
                params.x = params.x + mResetLocationValue;
            }
            updateViewPosition();
            double a = mScreenWidth / 2;
            float offset = (float) ((a - (Math.abs(params.x - a))) / a);
//            logoView.setDrag(isDrag, offset, true);
            if (mGetViewCallback == null) {
                dragLogoViewOffset(logoView, false, true, 0);
            } else {
                mGetViewCallback.dragLogoViewOffset(logoView, isDrag, true, offset);
            }
            return;
        }

        if (Math.abs(params.x) < 0) {
            params.x = 0;
        } else if (Math.abs(params.x) > mScreenWidth) {
            params.x = mScreenWidth;
        }
        if (valueAnimator.isRunning()) {
            valueAnimator.cancel();
        }

        updateViewPosition();
        isDrag = false;
    }

    public void show() {
        try {
            dismiss();
            if (mWindowManager != null && params != null && logoView != null) {
                params.width = WindowManager.LayoutParams.WRAP_CONTENT;
                params.height = WindowManager.LayoutParams.WRAP_CONTENT;
                mWindowManager.addView(logoView, params);
            }
            if (mHideTimer != null) {
                mHideTimer.start();
            } else {
                initTimer();
                mHideTimer.start();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void hide() {
        dismiss();
    }

    /**
     * 打开菜单/关闭菜单
     */
    protected void openMenu() {
        if (isDrag) return;

        if (!isExtended) {
//            logoView.setDrawDarkBg(false);
            try {
                mWindowManager.removeViewImmediate(logoView);
                params.width = WindowManager.LayoutParams.MATCH_PARENT;
                params.height = WindowManager.LayoutParams.MATCH_PARENT;
                if (mHintLocation == RIGHT) {
                    mWindowManager.addView(rightView, params);
                    if (mGetViewCallback == null) {
                        rightViewOpened(rightView);
                    } else {
                        mGetViewCallback.rightViewOpened(rightView);
                    }
                } else {
                    mWindowManager.addView(leftView, params);
                    if (mGetViewCallback == null) {
                        leftViewOpened(leftView);
                    } else {
                        mGetViewCallback.leftViewOpened(leftView);
                    }


                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            isExtended = true;
            mHideTimer.cancel();
        } else {
//            logoView.setDrawDarkBg(true);
            try {
                mWindowManager.removeViewImmediate(mHintLocation == LEFT ? leftView : rightView);
                params.width = WindowManager.LayoutParams.WRAP_CONTENT;
                params.height = WindowManager.LayoutParams.WRAP_CONTENT;
                mWindowManager.addView(logoView, params);
                if (mGetViewCallback == null) {
                    leftOrRightViewClosed(logoView);
                } else {
                    mGetViewCallback.leftOrRightViewClosed(logoView);
                }

            } catch (Exception e) {
                e.printStackTrace();
            }

            isExtended = false;
            mHideTimer.start();
        }

    }


    /**
     * 更新悬浮窗在屏幕中的位置。
     */
    private void updateViewPosition() {
        isDrag = true;
        try {
            if (!isExtended) {
                if (minY == 0) minY = logoView.getHeight();
                if (maxY == ScreenUtils.getScreenHeight()) maxY -= logoView.getHeight();
                if (params.y < minY) params.y = minY;
                if (params.y > maxY) params.y = maxY;
                mWindowManager.updateViewLayout(logoView, params);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 移除所有悬浮窗 释放资源
     */
    public void dismiss() {
        //记录上次的位置logo的停放位置，以备下次恢复
        saveSetting(LOCATION_X, mHintLocation);
        saveSetting(LOCATION_Y, params.y);
        logoView.clearAnimation();
        try {
            mHideTimer.cancel();
            if (isExtended) {
                mWindowManager.removeViewImmediate(mHintLocation == LEFT ? leftView : rightView);
            } else {
                if (logoView.getParent() != null) {
                    mWindowManager.removeViewImmediate(logoView);
                }
            }
            isExtended = false;
            isDrag = false;
            if (mGetViewCallback == null) {
                onDestroyed();
            } else {
                mGetViewCallback.onDestroyed();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    protected abstract View getLeftView(LayoutInflater inflater);

    protected abstract View getRightView(LayoutInflater inflater);

    protected abstract View getLogoView(LayoutInflater inflater);

    protected abstract void resetLogoViewSize(int hintLocation, View logoView);//logo恢复原始大小

    protected abstract void dragLogoViewOffset(View logoView, boolean isDrag, boolean isResetPosition, float offset);

    protected abstract void shrinkLeftLogoView(View logoView);//logo左边收缩

    protected abstract void shrinkRightLogoView(View logoView);//logo右边收缩

    protected abstract void leftViewOpened(View leftView);//左菜单打开

    protected abstract void rightViewOpened(View rightView);//右菜单打开

    protected abstract void leftOrRightViewClosed(View logoView);

    protected abstract void onDestroyed();

    public interface GetViewCallback {
        View getLeftView(LayoutInflater inflater);

        View getRightView(LayoutInflater inflater);

        View getLogoView(LayoutInflater inflater);


        void resetLogoViewSize(int hintLocation, View logoView);//logo恢复原始大小

        void dragLogoViewOffset(View logoView, boolean isDrag, boolean isResetPosition, float offset);//logo正被拖动，或真在恢复原位

        void shrinkLeftLogoView(View logoView);//logo左边收缩

        void shrinkRightLogoView(View logoView);//logo右边收缩

        void leftViewOpened(View leftView);//左菜单打开

        void rightViewOpened(View rightView);//右菜单打开

        void leftOrRightViewClosed(View logoView);

        void onDestroyed();

    }

    /**
     * 用于保存悬浮球的位置记录
     *
     * @param key          String
     * @param defaultValue int
     * @return int
     */
    private int getSetting(String key, int defaultValue) {
        try {
            SharedPreferences sharedata = context.getSharedPreferences("floatLogo", 0);
            return sharedata.getInt(key, defaultValue);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return defaultValue;
    }

    /**
     * 用于保存悬浮球的位置记录
     *
     * @param key   String
     * @param value int
     */
    public void saveSetting(String key, int value) {
        try {
            SharedPreferences.Editor sharedata = context.getSharedPreferences("floatLogo", 0).edit();
            sharedata.putInt(key, value);
            sharedata.apply();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
        } else {
        }
        mScreenWidth = mWindowManager.getDefaultDisplay().getWidth();
        if (mHintLocation == RIGHT) {
            params.x = mScreenWidth;
        }
        show();
    }
}
