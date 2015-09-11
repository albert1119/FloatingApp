package com.app.albertstudio.floatingapp;

import android.app.Service;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.os.IBinder;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.RelativeLayout;

/**
 * Created by albert on 15/9/10.
 */
public class FloatingAppService extends Service {

    private WindowManager mWindowManager;
    private RelativeLayout mFloatingView;
    private ImageView mImageView;
    private int mXStart, mYStart, mXStartLayout, mYStartLayout;
    private long mTimeDown, mTimeUp;
    private boolean mIsPlaying;
    private AnimationSet mAnimationSet;
    @Override
    public IBinder onBind(Intent intent) {
        // Not used
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mTimeDown = 0;
        mTimeUp = 0;
        mIsPlaying = false;

        mAnimationSet = new AnimationSet(true);
        RotateAnimation roataeAnimation = new RotateAnimation(0, 359, Animation.RELATIVE_TO_SELF, 0.49f, Animation.RELATIVE_TO_SELF, 0.501f);
        roataeAnimation.setDuration(500);
        roataeAnimation.setRepeatCount(Animation.INFINITE);
        mAnimationSet.setInterpolator(new LinearInterpolator());
        mAnimationSet.addAnimation(roataeAnimation);

        handleStart();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mFloatingView != null) {
            mWindowManager.removeView(mFloatingView);
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // TODO Auto-generated method stu

        if (startId == Service.START_STICKY) {
            //handleStart();
            return super.onStartCommand(intent, flags, startId);
        } else {
            return Service.START_NOT_STICKY;
        }

    }

    private void handleStart() {
        mWindowManager = (WindowManager) getSystemService(WINDOW_SERVICE);

        LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
        mFloatingView = (RelativeLayout) inflater.inflate(R.layout.floating, null);

        mImageView= (ImageView)mFloatingView.findViewById(R.id.floating);
        Bitmap original = BitmapFactory.decodeResource(getResources(), R.drawable.albert);
        Bitmap mask = BitmapFactory.decodeResource(getResources(),R.drawable.music_default_thumbnail);
        Bitmap result = Bitmap.createBitmap(mask.getWidth(), mask.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas mCanvas = new Canvas(result);
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_IN));
        mCanvas.drawBitmap(original, 0, 0, null);
        mCanvas.drawBitmap(mask, 0, 0, paint);
        paint.setXfermode(null);
        mImageView.setImageBitmap(result);
        mImageView.setScaleType(ImageView.ScaleType.CENTER);
        mImageView.setBackgroundResource(R.drawable.music_default_thumbnail);

        WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_PHONE,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH | WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                PixelFormat.TRANSLUCENT);
        params.gravity = Gravity.TOP | Gravity.LEFT;
        params.x = 0;
        params.y = 100;
        mWindowManager.addView(mFloatingView, params);

        mFloatingView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                WindowManager.LayoutParams layoutParams = (WindowManager.LayoutParams) mFloatingView.getLayoutParams();

                int x = (int) event.getRawX();
                int y = (int) event.getRawY();

                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        mTimeDown = System.currentTimeMillis();

                        mXStart = x;
                        mYStart = y;
                        mXStartLayout = layoutParams.x;
                        mYStartLayout = layoutParams.y;

                        break;
                    case MotionEvent.ACTION_MOVE:
                        int x_move = x - mXStart;
                        int y_move = y - mYStart;
                        layoutParams.x = mXStartLayout + x_move;
                        layoutParams.y = mYStartLayout + y_move;
                        mWindowManager.updateViewLayout(mFloatingView, layoutParams);
                        break;
                    case MotionEvent.ACTION_UP:
                        mTimeUp = System.currentTimeMillis();

                        if(mTimeUp - mTimeDown >= 500 )
                        {
                            if(mIsPlaying == false)
                            {
                                mImageView.startAnimation(mAnimationSet);
                                mIsPlaying = true;
                            }
                            else
                            {
                                mImageView.clearAnimation();
                                mIsPlaying = false;
                            }
                        }
                        break;
                    default:
                        break;
                }
                return true;
            }
        });
    }
}