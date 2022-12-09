package org.yameida.floatwindow.view;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.PixelFormat;
import android.os.Build;
import android.provider.Settings;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.WindowManager;
import android.widget.LinearLayout;

import org.yameida.floatwindow.R;


public class HiderView extends LinearLayout {
    private Context mContext;
    private WindowManager mWindowManager;
    public WindowManager.LayoutParams mLp;
    private ValueAnimator valueAnimator;
    private int mResetLocationValue;

    public HiderView(Context context) {
        super(context);
        this.mContext = context;
        init();
    }

    public HiderView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.mContext = context;
        init();
    }

    private void init() {
        inflate(mContext, R.layout.widget_hiderview_ll, this);
    }

    public void attachToWindow() {
        if (this.getParent() != null) {
            return;
        }
        mWindowManager = (WindowManager) mContext
                .getSystemService(Context.WINDOW_SERVICE);
        mLp = new WindowManager.LayoutParams();
        if (Build.VERSION.SDK_INT >= 23) {
            if (!Settings.canDrawOverlays(getContext())) {
                if (Build.VERSION.SDK_INT >= 26) {
                    mLp.type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
                } else {
                    mLp.type = WindowManager.LayoutParams.TYPE_SYSTEM_ALERT;
                }
            } else {
                if (Build.VERSION.SDK_INT >= 26) {
                    mLp.type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
                } else {
                    mLp.type = WindowManager.LayoutParams.TYPE_SYSTEM_ALERT;
                }
            }
        } else {
            mLp.type = WindowManager.LayoutParams.TYPE_SYSTEM_ALERT;
        }
//        mLp.type = WindowManager.LayoutParams.TYPE_SYSTEM_ALERT;
        mLp.format = PixelFormat.RGBA_8888;
        mLp.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
                | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
        mLp.gravity = Gravity.CENTER | Gravity.TOP;
        mLp.width = WindowManager.LayoutParams.WRAP_CONTENT;
        mLp.height = WindowManager.LayoutParams.WRAP_CONTENT;
        int y = mWindowManager.getDefaultDisplay().getHeight();
        mLp.y = y;
        mWindowManager.addView(this, mLp);

        valueAnimator = ValueAnimator.ofInt(0, y / 4);
        valueAnimator.setDuration(200);

        valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                mResetLocationValue = (int) animation.getAnimatedValue();
                mLp.y = y - getHeight() - mResetLocationValue;
                mWindowManager.updateViewLayout(HiderView.this, mLp);
            }
        });

        valueAnimator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {}

            @Override
            public void onAnimationEnd(Animator animation) {
                mWindowManager.updateViewLayout(HiderView.this, mLp);
            }

            @Override
            public void onAnimationCancel(Animator animation) {
                mWindowManager.updateViewLayout(HiderView.this, mLp);
            }

            @Override
            public void onAnimationRepeat(Animator animation) {}
        });
        if (!valueAnimator.isRunning()) {
            valueAnimator.start();
        }
    }

    public void release() {
        if (this.getParent() != null) {
            if (valueAnimator.isRunning()) {
                valueAnimator.cancel();
            }
            mWindowManager.removeView(this);
        }
    }

}
