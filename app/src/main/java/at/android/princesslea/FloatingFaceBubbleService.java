package at.android.princesslea;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.PixelFormat;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;

import org.joda.time.DateTime;
import org.joda.time.Interval;
import org.joda.time.Months;
import org.joda.time.Period;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class FloatingFaceBubbleService extends Service {

    private String TAG = "FaceBubbleService";
    private WindowManager windowManager;
    private WindowManager.LayoutParams myParams;
    private MyImageView floatingFaceBubble;
    private Handler h = new Handler(Looper.getMainLooper());
    private SimpleDateFormat formater = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.GERMANY);

    private DateTime birthDt, currDt;
    private boolean quitService = false;

    private MyBroadcastReceiver mBroadcastReceiver;

    int month = 0, days = 0, hours = 0, min = 0, sec = 0, birthDay = 13, birthMinOfDay = 1142;

/*    private Runnable mUpdateTimeTask = new Runnable() {
        public void run() {
            String s = java.text.DateFormat.getTimeInstance()*//*DateTimeInstance()*//*.format(Calendar.getInstance().getTime());
            floatingFaceBubble.setText(s);
            h.postDelayed(mUpdateTimeTask, 100);
        }
    };*/


    public void onCreate() {
        super.onCreate();

        if (mBroadcastReceiver == null)
            mBroadcastReceiver = new MyBroadcastReceiver();
        IntentFilter intentFilter = new IntentFilter("pref_broadcast");
        registerReceiver(mBroadcastReceiver, intentFilter);

        windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);

        myParams = new WindowManager.LayoutParams(
                LayoutParams.WRAP_CONTENT,
                LayoutParams.WRAP_CONTENT,
                LayoutParams.TYPE_PHONE,
                LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT);

        floatingFaceBubble = new MyImageView(this, myParams);

        myParams.gravity = Gravity.TOP | Gravity.START;
        myParams.x = 0;
        myParams.y = 100;

        windowManager.addView(floatingFaceBubble, myParams);

        Date birth;
        try {
            birth = formater.parse("2017-06-13 19:02:00");
            // birth = formater.parse("2017-03-17 15:02:00");
        } catch (ParseException e) {
            birth = new Date();
            e.printStackTrace();
        }

        birthDt = new DateTime(birth);

        final int delay = 1000;
        h.postDelayed(new Runnable() {
            public void run() {

                currDt = new DateTime();

                Interval interval = new Interval(birthDt, currDt);
                Period period = interval.toPeriod();

                Months diffMonth = Months.monthsBetween(currDt, birthDt);

                // Days diffDay = Days.daysBetween(currDt, birthDt);
                // Hours diffHours = Hours.hoursBetween(currDt, birthDt);
                // Minutes diffMinute = Minutes.minutesBetween(currDt, birthDt);
                ///Seconds diffSecond = Seconds.secondsBetween(currDt, birthDt);

                int currDay = currDt.getDayOfMonth();
                int currMinOfDay = currDt.getMinuteOfDay();

                // birthMinOfDay = 15 * 60 + 6;
                days = currDay - birthDay;

                // day not fully completed
                if (currMinOfDay < birthMinOfDay) {
                    // if birthday we take the last day of the last month
                    if (days == 0) {
                        days = currDt.minusMonths(1).dayOfMonth().withMaximumValue().getDayOfMonth();
                    }
                    // we add the days of  last month
                    else if (days < 0) {
                        days += currDt.minusMonths(1).dayOfMonth().withMaximumValue().getDayOfMonth() - 1;
                    }
                    // reduce because day not completed
                    else {
                        days--;
                    }
                } else {
                    if (days < 0) {
                        days += currDt.minusMonths(1).dayOfMonth().withMaximumValue().getDayOfMonth();
                    }
                }


                month = diffMonth.negated().getMonths();

                // hours = diffHours.minus(diffDay.toStandardHours()).negated().getHours();
                hours = period.getHours();
                // min = diffMinute.minus(diffHours.toStandardMinutes()).negated().getMinutes();
                min = period.getMinutes();
                // sec = diffSecond.minus(diffMinute.toStandardSeconds()).negated().getSeconds();
                sec = period.getSeconds();

                String s = "Lea\n" +
                        month + "m " +
                        days + "d " +
                        hours + "h \n" +
                        min + "m " +
                        sec + "s";

                // Log.d(TAG, "run: " + s);

                floatingFaceBubble.setText(s);

                floatingFaceBubble.invalidate();

                if (!quitService)
                    h.postDelayed(this, delay);
            }
        }, delay);


        try {
            //for moving the picture on touch and slide
            floatingFaceBubble.setOnTouchListener(new View.OnTouchListener() {
                // WindowManager.LayoutParams paramsT = myParams;
                private int initialX, initialY;
                private float initialTouchX, initialTouchY;
                private long touchStartTime, touchEndTime = 0;
                boolean firstTouch = false;

                long time = 0;
                int dragThreshold = 10;

                @Override
                public boolean onTouch(View v, MotionEvent event) {

                    switch (event.getAction()) {
                        case MotionEvent.ACTION_DOWN:
                            touchStartTime = System.currentTimeMillis();
                            initialX = myParams.x;
                            initialY = myParams.y;
                            initialTouchX = event.getRawX();
                            initialTouchY = event.getRawY();

                            if (firstTouch && (System.currentTimeMillis() - time) <= ViewConfiguration.getDoubleTapTimeout()) {
                                //DOUBLE tap

                                Intent prefActivity = new Intent(getBaseContext(),
                                        MyPreferenceActivity.class);
                                startActivity(prefActivity);

                                /*
                                // trigger next image resource change and also set size via params object
                                floatingFaceBubble.nextImage();
                                windowManager.updateViewLayout(v, myParams);
                                */

                                firstTouch = false;
                                return true; // event consumed

                            } else {
                                // SINGLE tap (do nothing)
                                firstTouch = true;
                                time = System.currentTimeMillis();
                                return false;
                            }
                            // break;
                        case MotionEvent.ACTION_UP:
                            touchEndTime = System.currentTimeMillis();

//                             LONG press
//                            if (touchEndTime - touchStartTime > ViewConfiguration.getLongPressTimeout()
//                                    && Math.abs(initialTouchX - event.getRawX()) <= dragThreshold) {
//
//                                stopMyService();
//
//                                // trigger next image resource change and also set size via params object
//                                floatingFaceBubble.nextImage();
//                                windowManager.updateViewLayout(v, myParams);
//
//
//                                firstTouch = false;
//                            }
                            return true;
                        // break;
                        case MotionEvent.ACTION_MOVE:
                            myParams.x = initialX + (int) (event.getRawX() - initialTouchX);
                            myParams.y = initialY + (int) (event.getRawY() - initialTouchY);

                            windowManager.updateViewLayout(v, myParams);
                            break;
                    }
                    return false;
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void stopMyService() {
        // remove view and stop the service
        windowManager.removeView(floatingFaceBubble);
        stopSelf();
        quitService = true;
    }


    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void onDestroy() {

        if (mBroadcastReceiver != null)
            unregisterReceiver(mBroadcastReceiver);

        super.onDestroy();
        Log.d(TAG, "onDestroy: " + "DESTROY");
    }

    class MyBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(TAG, "onReceive: " + "MyBroadcastReceiver" + intent.getAction());

            //if (intent.getAction().equals("pref_broadcast")) {
            Bundle extras = intent.getExtras();
            if (extras != null) {
                if (extras.getString("action") != null) {
                    if (extras.getString("action").equalsIgnoreCase("exit")) {
                        stopMyService();
                    } else if (extras.getString("action").equalsIgnoreCase("nextpicture")) {
                        floatingFaceBubble.nextImage();
                        windowManager.updateViewLayout(floatingFaceBubble, myParams);
                    }
                } else if (extras.getInt("scale", -1) != -1) {
                    floatingFaceBubble.setImageScale(extras.getInt("scale"));
                    windowManager.updateViewLayout(floatingFaceBubble, myParams);
                }
//                else if (extras.getBoolean("service_switch") == false) {
//                    stopMyService();
//                }
                else {
                    Log.i(TAG, "onReceive: found data in bundle but was not handled.");
                }
            }
        }
    }
}


