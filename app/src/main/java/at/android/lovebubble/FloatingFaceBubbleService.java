package at.android.lovebubble;

import android.animation.Animator;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.PixelFormat;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.View;
import android.view.ViewPropertyAnimator;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
import android.widget.Toast;

import org.joda.time.DateTime;
import org.joda.time.Interval;
import org.joda.time.Months;
import org.joda.time.Period;
import org.joda.time.Weeks;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class FloatingFaceBubbleService extends Service {

    private String TAG = "FaceBubbleService";
    private String name = "";
    private String bubbleText = "";

    private int timeformat = 0;

    private boolean switch_time, switch_name;

    private WindowManager windowManager;
    private WindowManager.LayoutParams myParams;
    private MyImageView floatingFaceBubble;

    private Handler h = new Handler(Looper.getMainLooper());

    private SimpleDateFormat formater = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.GERMANY);
    private DateTime birthDt, currDt;

    private GestureDetector mDetector;
    private int initialX, initialY/*, lastX, lastY, screenWidth, screenHeight*/;
    private float initialTouchX, initialTouchY;

    private int delay;

    private MyBroadcastReceiver mBroadcastReceiver;

    int year = 0;
    int month = 0;
    int days = 0;
    int hours = 0;
    int min = 0;
    int sec = 0;
    int birthDay = 0/*13*/;
    int birthSecondOfDay = 0;
    private SharedPreferences preferences;

    String tf0, tf1, tf1_p, tf2, tf2_p, tf3, tf4, tf5,
            tf3_ps, tf3_sp, tf3_pp;


/*    private Runnable mUpdateTimeTask = new Runnable() {
        public void run() {
            String s = java.text.DateFormat.getTimeInstance()*//*DateTimeInstance()*//*.format(Calendar.getInstance().getTime());
            floatingFaceBubble.setText(s);
            h.postDelayed(mUpdateTimeTask, 100);
        }
    };*/


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return Service.START_STICKY;
    }


    public void onCreate() {
        super.onCreate();

        // Receiver registration
        if (mBroadcastReceiver == null)
            mBroadcastReceiver = new MyBroadcastReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("pref_broadcast");
        intentFilter.addAction(Intent.ACTION_SCREEN_ON);
        registerReceiver(mBroadcastReceiver, intentFilter);


        // Load Preferences
        preferences = PreferenceManager.getDefaultSharedPreferences(this);
        name = preferences.getString("name", getString(R.string.putnamehere));
        timeformat = Integer.parseInt(preferences.getString("timeformat", "4"));
        Long mills = preferences.getLong("birthdatetime", -1);
        switch_time = preferences.getBoolean("switch_time", true);
        switch_name = preferences.getBoolean("switch_name", true);
        int posX = preferences.getInt("posX", 0);
        int posY = preferences.getInt("posY", 100);

        // Load Formatter Resources
        loadFormatterStrings();


        // Floating Bubble
        myParams = new WindowManager.LayoutParams(
                LayoutParams.WRAP_CONTENT,
                LayoutParams.WRAP_CONTENT,
                LayoutParams.TYPE_PHONE, // deprecated in 26 use TYPE_APPLICATION_OVERLAY
                LayoutParams.FLAG_NOT_FOCUSABLE
                // | LayoutParams.FLAG_LAYOUT_IN_SCREEN // draw over status bar
                // | LayoutParams.FLAG_LAYOUT_IN_OVERSCAN
                // | LayoutParams.FLAG_LAYOUT_NO_LIMITS
                ,
                PixelFormat.TRANSLUCENT);
        myParams.gravity = Gravity.TOP | Gravity.START;
        // myParams.gravity = Gravity.CENTER | Gravity.CENTER;
        myParams.x = posX;
        myParams.y = posY;


        floatingFaceBubble = new MyImageView(this, myParams);


        // Only add the bubble if the orientation from settings matches the current orientation
        preferences.edit().putBoolean("hiddenByLongpress", false).apply(); // reset hiddenByLongpress when service starts
        windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        showBubble(windowManager.getDefaultDisplay().getRotation() == Surface.ROTATION_0
                ? Configuration.ORIENTATION_PORTRAIT : Configuration.ORIENTATION_LANDSCAPE);

        /*
        // get screen size in pixel
        Point size = new Point();
        windowManager.getDefaultDisplay().getRealSize(size);
        screenWidth = size.x;
        screenHeight = size.y;
        */

        Date birth;
        if (mills != -1) { // found in preferences
            setBirthDayInfoByMills(mills);
        } else {
            try {
                birth = formater.parse("2017-06-13 19:02:00");
                birthDt = new DateTime(birth);
                birthDay = 13;
                birthSecondOfDay = 68520; // (19h*60 + 2s)*60 => 19:02
            } catch (ParseException e) {
                birth = new Date();
                birthDt = new DateTime(birth);
                e.printStackTrace();
            }
        }

        postDelayed();

        addGestureListener();

    }

    private void postDelayed() {

        h.postDelayed(new Runnable() {
            public void run() {

                //Log.d(TAG, "run: Post DELAYED  :" + new DateTime());

                if (switch_time) { /*Name and Time || Time only*/

                    currDt = new DateTime();
                    int currDay = currDt.getDayOfMonth();
                    int currSecondOfDay = currDt.getSecondOfDay();
                    days = currDay - birthDay;

                    // DAY calculation
                    // day not fully completed
                    if (currSecondOfDay < birthSecondOfDay) {
                        // if birthday we take the last day of the last month
                        if (days == 0) {
                            days = currDt.minusMonths(1).dayOfMonth().withMaximumValue().getDayOfMonth();
                        }
                        // we add the days of last month
                        else if (days < 0) {
                            days += currDt.minusMonths(1).dayOfMonth().withMaximumValue().getDayOfMonth() - 1;
                        }
                        // reduce because day not completed
                        else {
                            days--;
                        }
                    } else { // day fully completed
                        if (days < 0) {
                            days += currDt.minusMonths(1).dayOfMonth().withMaximumValue().getDayOfMonth();
                        }
                    }

                    // Interval interval = new Interval(birthDt, currDt);
                    Period period = new Interval(birthDt, currDt).toPeriod();
                    Months diffMonth = Months.monthsBetween(currDt, birthDt);

                    month = diffMonth.negated().getMonths();
                    hours = period.getHours();
                    min = period.getMinutes();
                    sec = period.getSeconds();


                    if (switch_name) { /*Name and Time*/
                        bubbleText = "name=" + name;
                    } else {
                        bubbleText = "";
                    }

                    // Detailed
                    if (timeformat == 0) {
                        delay = 1000;
                        bubbleText += String.format(tf0, month, days, hours, min, sec);

                        // Weeks
                    } else if (timeformat == 1) {
                        delay = 60000;
                        Weeks diffWeeks = Weeks.weeksBetween(currDt, birthDt);
                        int weeks = diffWeeks.negated().getWeeks();
                        bubbleText += String.format(weeks == 1 ? tf1 : tf1_p, weeks);

                        // Months
                    } else if (timeformat == 2) {
                        delay = 60000;
                        int weeks = period.minusMonths(month).getWeeks();
                        String monthStr = month == 0 ? "" : String.valueOf(month);

                        if (weeks == 1)
                            monthStr += "\u00BC";
                        else if (weeks == 2)
                            monthStr += "\u00BD";
                        else if (weeks >= 3)
                            monthStr += "\u00BE";

                        bubbleText += String.format(month == 1 && weeks == 0 ? tf2 : tf2_p, monthStr);

                        // Year and Months
                    } else if (timeformat == 3) {
                        delay = 60000;
                        year = month / 12;

                        if (year != 1 && month != 1)
                            bubbleText += String.format(tf3_pp, year, month % 12);
                        else if (year != 1)
                            bubbleText += String.format(tf3_ps, year, month % 12);
                        else if (month != 1)
                            bubbleText += String.format(tf3_sp, year, month % 12);
                        else
                            bubbleText += String.format(tf3, year, month % 12);

                        // Detailed with Year
                    } else if (timeformat == 4) {
                        delay = 1000;
                        bubbleText += String.format(tf4, month / 12, month % 12, days, hours, min, sec);

                        // Year Month Day (y m d)
                    } else if (timeformat == 5) {
                        delay = 60000;
                        bubbleText += String.format(tf5, month / 12, month % 12, days);

                    }
                    floatingFaceBubble.setText(bubbleText);
                    floatingFaceBubble.invalidate();

                } else if (switch_name) { /*Name only*/
                    delay = 60000;
                    floatingFaceBubble.setText("\n" + "name=" + name + "\n");
                    floatingFaceBubble.invalidate();

                } else { // emtpy
                    delay = 60000;
                    floatingFaceBubble.setText("");
                    floatingFaceBubble.invalidate();
                }

                h.postDelayed(this, delay);

            }
        }, 100);
    }

    private void addGestureListener() {

        floatingFaceBubble.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                return mDetector.onTouchEvent(motionEvent);
            }
        });

        mDetector = new GestureDetector(this, new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onDown(MotionEvent event) {
                // Log.d("TAG","onDown: ");
                initialX = myParams.x;
                initialY = myParams.y;
                initialTouchX = event.getRawX();
                initialTouchY = event.getRawY();
                return true; // don't return false here or else none of the other gestures will work
            }

            @Override
            public boolean onSingleTapConfirmed(MotionEvent e) {
                // Log.i("TAG", "onSingleTapConfirmed: ");
                return true;
            }

            @Override
            public void onLongPress(MotionEvent e) {
                // Log.i("TAG", "onLongPress: ");

                if (preferences.getBoolean("quick_hide", false)) {

                    floatingFaceBubble.animate().setDuration(800).alpha(0f).withEndAction(new Runnable() {
                        public void run() {
                            preferences.edit().putBoolean("hiddenByLongpress", true).apply();
                            showBubble2(false);
                        }
                    }).start();
                }
            }

            @Override
            public boolean onDoubleTap(MotionEvent e) {
                // Log.i("TAG", "onDoubleTap: ");

                Intent prefActivity = new Intent(getBaseContext(),
                        MyPreferenceActivity.class);
                startActivity(prefActivity);

                return true;
            }

            @Override
            public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
                // Log.i("TAG", "onScroll: ");

                myParams.x = Math.max(initialX + (int) (e2.getRawX() - initialTouchX), 0);
                myParams.y = Math.max(initialY + (int) (e2.getRawY() - initialTouchY), 0);

                //myParams.x = lastX = Math.max(initialX + (int) (e2.getRawX() - initialTouchX), 0);
                //myParams.y = lastY = Math.max(initialY + (int) (e2.getRawY() - initialTouchY), 0);
                // Log.d(TAG, "onScroll: "+myParams.y + " " + screenHeight + " " + e1.getRawY() + " " + e2.getY());
                // Log.d(TAG, "onScroll: "+myParams.x + " " + screenWidth + " " + e1.getRawX() + " " + e2.getX());

                windowManager.updateViewLayout(floatingFaceBubble, myParams);
                return true;
            }
            /*
            @Override
            public boolean onFling(MotionEvent event1, MotionEvent event2,
                                   float velocityX, float velocityY) {
                // Log.d("TAG", "onFling: velX=" + velocityX + "/velY=" + velocityY);

                //FloatValueHolder floatValueHolder = new FloatValueHolder(0);
                //FloatValueHolder floatValueHolder2 = new FloatValueHolder(0);

                //FlingAnimation flingX = new FlingAnimation(floatValueHolder);

                FlingAnimation flingX = new FlingAnimation(floatingFaceBubble, DynamicAnimation.TRANSLATION_X);
                //                flingX.addUpdateListener(new DynamicAnimation.OnAnimationUpdateListener() {
                //                    @Override
                //                    public void onAnimationUpdate(DynamicAnimation animation, float value, float velocity) {
                //                        myParams.x = (int) (lastX + value);
                //                        windowManager.updateViewLayout(floatingFaceBubble, myParams);
                //                    }
                //                });
                flingX.setStartVelocity(velocityX)
                        .setMinValue(-1000) // minimum translationX property
                        .setMaxValue(1000)  // maximum translationX property
                        .setFriction(1)
                        .start();

                ////////////////////
                //FlingAnimation flingY = new FlingAnimation(floatValueHolder2);
                FlingAnimation flingY = new FlingAnimation(floatingFaceBubble, DynamicAnimation.TRANSLATION_Y);
                //                flingY.addUpdateListener(new DynamicAnimation.OnAnimationUpdateListener() {
                //                    @Override
                //                    public void onAnimationUpdate(DynamicAnimation animation, float value, float velocity) {
                //                        Log.d(TAG, "onAnimationUpdate: "+ value);
                //                        myParams.y = (int) (lastY + value);
                //                        windowManager.updateViewLayout(floatingFaceBubble, myParams);
                //                    }
                //                });
                flingY.setStartVelocity(velocityY)
                        .setMinValue(-1000) // minimum translationX property
                        .setMaxValue(1000)  // maximum translationX property
                        .setFriction(1)
                        .start();

                return true;
            }
            */
        });
    }

    private void stopMyService() {
        // Log.d(TAG, "stopMyService: " + "stopMyService");

        // Save last Bubble position
        preferences.edit().putInt("posX", myParams.x).apply();
        preferences.edit().putInt("posY", myParams.y).apply();

        // remove view if attached
        if (floatingFaceBubble.isAttachedToWindow()) {
            windowManager.removeView(floatingFaceBubble);
        }

        // reset Hidden flag
        preferences.edit().putBoolean("hiddenByLongpress", false).apply();

        // remove all callbacks from handler
        h.removeCallbacksAndMessages(null);

        // stop service
        stopSelf();

    }

    private void setBirthDayInfoByMills(Long mills) {
        birthDt = new DateTime(mills);
        if (birthDt.isBeforeNow()) {
            birthDay = birthDt.getDayOfMonth();
            birthSecondOfDay = birthDt.getSecondOfDay();
        } else {
            Toast.makeText(this, R.string.birthdaybeforenow, Toast.LENGTH_SHORT).show();
            birthDt = new DateTime();
        }
    }


    @Override
    public IBinder onBind(Intent intent) {
        Log.d(TAG, "onBind: onbind called");
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void onDestroy() {

        if (mBroadcastReceiver != null)
            unregisterReceiver(mBroadcastReceiver);

        h.removeCallbacksAndMessages(null);

        // Log.d(TAG, "onDestroy: " + "DESTROY");

        super.onDestroy();
    }

    class MyBroadcastReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            // Log.d(TAG, "onReceive: " + "MyBroadcastReceiver=" + intent.getAction());

            if (intent.getAction().equals("pref_broadcast")) {
                Bundle extras = intent.getExtras();
                if (extras != null) {
                    if (extras.getString("action") != null) {
                        try {
                            if ("exit".equalsIgnoreCase(extras.getString("action"))) {
                                stopMyService();

                            } else if ("timeformat".equalsIgnoreCase(extras.getString("action"))) {
                                timeformat = Integer.parseInt(preferences.getString("timeformat", "0"));

                                h.removeCallbacksAndMessages(null);
                                postDelayed();

                            } else if ("switch_datetime".equalsIgnoreCase(extras.getString("action"))) {
                                switch_time = preferences.getBoolean("switch_time", true);
                                switch_name = preferences.getBoolean("switch_name", true);

                                h.removeCallbacksAndMessages(null);
                                postDelayed();

                            } else if ("updateYoffset".equalsIgnoreCase(extras.getString("action"))) {
                                floatingFaceBubble.updateYoffset();

                                h.removeCallbacksAndMessages(null);
                                postDelayed();

                            } else if ("updateFontSize".equalsIgnoreCase(extras.getString("action"))) {
                                floatingFaceBubble.updateFontSize();

                                h.removeCallbacksAndMessages(null);
                                postDelayed();

                            }
                        } catch (NullPointerException e) {
                            Log.e(TAG, "onReceive: ", e);
                        }
                    } else if (extras.getString("choosepic") != null) {
                        preferences.edit().putString("imguri", extras.getString("choosepic")).apply();
                        floatingFaceBubble.setImageFromUri(extras.getString("choosepic"));

                    } else if (extras.getString("mask") != null) {
                        floatingFaceBubble.setMaskByName(extras.getString("mask"));
                        if (floatingFaceBubble.isAttachedToWindow()) {
                            windowManager.updateViewLayout(floatingFaceBubble, myParams);
                        }

                    } else if (extras.getString("name") != null) {
                        name = extras.getString("name");

                        h.removeCallbacksAndMessages(null);
                        postDelayed();

                    } else if (extras.getInt("scale", -1) != -1) {
                        floatingFaceBubble.setImageScale(extras.getInt("scale"));
                        if (floatingFaceBubble.isAttachedToWindow()) {
                            windowManager.updateViewLayout(floatingFaceBubble, myParams);
                        }

                    } else if (extras.getLong("birthdatetime", -1) != -1) {
                        preferences.edit().putLong("birthdatetime", extras.getLong("birthdatetime")).apply();
                        Long mills = extras.getLong("birthdatetime");
                        setBirthDayInfoByMills(mills);

                        h.removeCallbacksAndMessages(null);
                        postDelayed();
                    } else {
                        Log.i(TAG, "onReceive: found data in bundle but was not handled.");
                    }
                }

            } else if (intent.getAction().equals(Intent.ACTION_SCREEN_ON)) {
                // Toast.makeText(context, "Screen on", Toast.LENGTH_SHORT).show();

                preferences.edit().putBoolean("hiddenByLongpress", false).apply(); // reset hiddenByLongpress when screen turns on

                if (windowManager != null) {
                    showBubble(windowManager.getDefaultDisplay().getRotation() == Surface.ROTATION_0
                            ? Configuration.ORIENTATION_PORTRAIT : Configuration.ORIENTATION_LANDSCAPE);
                }

            } else {
                Log.d(TAG, "onReceive: Received Unknown Broadcast");
            }
        }
    }

    private void loadFormatterStrings() {
        tf0 = getString(R.string.tf0);
        tf1 = getString(R.string.tf1);
        tf1_p = getString(R.string.tf1_p);
        tf2 = getString(R.string.tf2);
        tf2_p = getString(R.string.tf2_p);
        tf3 = getString(R.string.tf3);
        tf3_sp = getString(R.string.tf3_sp);
        tf3_ps = getString(R.string.tf3_ps);
        tf3_pp = getString(R.string.tf3_pp);
        tf4 = getString(R.string.tf4);
        tf5 = getString(R.string.tf5);

    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        // newConfig.orientation
        // port: 1
        // land: 2
        showBubble(newConfig.orientation);
    }

    private void showBubble(int orientation) {
        String pref_orientation = preferences.getString("orientation", "0");

        switch (pref_orientation) {
            case "0": // Portrait + Landscape
                showBubble2(true);
                break;
            case "1": // Portrait
                showBubble2(orientation == Configuration.ORIENTATION_PORTRAIT/*1*/);
                break;
            case "2": // Landscape
                showBubble2(orientation != Configuration.ORIENTATION_PORTRAIT/*1*/);
                break;
            default:
                Log.d(TAG, "onConfigurationChanged: wrong orientation received");
        }
    }

    private void showBubble2(boolean state) {
        if (windowManager != null && myParams != null && floatingFaceBubble != null) {
            if (state) { // Show Bubble
                Log.d(TAG, "showBubble2: HERE3.2 "+!floatingFaceBubble.isAttachedToWindow()+" "+!preferences.getBoolean("hiddenByLongpress", false));
                if (!floatingFaceBubble.isAttachedToWindow() && !preferences.getBoolean("hiddenByLongpress", false)) {

                    //floatingFaceBubble.setAlpha(0f);
                    windowManager.addView(floatingFaceBubble, myParams);
                    //floatingFaceBubble.animate().setDuration(800).alpha(1f).start();
                    //floatingFaceBubble.setAlpha(1f);

                }
            } else { // Hide Bubble
                if (floatingFaceBubble.isAttachedToWindow()) {

                    //floatingFaceBubble.setAlpha(0f);
                    windowManager.removeView(floatingFaceBubble);
                    floatingFaceBubble.setAlpha(1f); // just in case alpha was set to 0 by long-press hide
                }
            }
        } else {
            Log.d(TAG, "showBubble: windowManager or myParams or floatingFaceBubble => null");
        }
    }
}


