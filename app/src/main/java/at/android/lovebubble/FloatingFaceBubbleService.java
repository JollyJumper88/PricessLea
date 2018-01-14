package at.android.lovebubble;

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
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.View;
import android.view.ViewConfiguration;
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
    private boolean bubbleVisible;

    private WindowManager windowManager;
    private WindowManager.LayoutParams myParams;
    private MyImageView floatingFaceBubble;

    private Handler h = new Handler(Looper.getMainLooper());

    private SimpleDateFormat formater = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.GERMANY);
    private DateTime birthDt, currDt;

    private int delay;

    private MyBroadcastReceiver mBroadcastReceiver;

    int year = 0;
    int month = 0;
    int days = 0;
    int hours = 0;
    int min = 0;
    int sec = 0;
    int birthDay = 0/*13*/;
    int birthMinOfDay = 0/*1142*/;
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
        bubbleVisible = false;
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
                birthMinOfDay = 1142;
            } catch (ParseException e) {
                birth = new Date();
                birthDt = new DateTime(birth);
                e.printStackTrace();
            }
        }

        postDelayed();

        addTouchListener();


    }

    private void postDelayed() {

        h.postDelayed(new Runnable() {
            public void run() {

                // Log.d(TAG, "run: Post DELAYED  :" + new DateTime());
                /*
                0 Detailed</item>
                1 Week</item>
                2 Month</item>
                3 Month and Week</item>
                4 Year and Month</item>
                5 Detailed (with year)</item>
                */

                if (switch_time) { /*Name and Time || Time only*/

                    delay = 1000;

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
                    if (currMinOfDay <= birthMinOfDay) {
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


                    if (switch_name) { /*Name and Time*/
                        bubbleText = "name=" + name;
                    } else {
                        bubbleText = "";
                    }

                    // Detailed
                    if (timeformat == 0) {
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

    private void addTouchListener() {
        try {
            //for moving the picture on touch and slide
            floatingFaceBubble.setOnTouchListener(new View.OnTouchListener() {
                //layout.setOnTouchListener(new View.OnTouchListener() {
                private int initialX, initialY;
                private float initialTouchX, initialTouchY;
                // private long touchStartTime, touchEndTime = 0;
                boolean firstTouch = false;

                long time = 0;

                @Override
                public boolean onTouch(View v, MotionEvent event) {

                    switch (event.getAction()) {
                        case MotionEvent.ACTION_DOWN:
                            // touchStartTime = System.currentTimeMillis();
                            initialX = myParams.x;
                            initialY = myParams.y;
                            initialTouchX = event.getRawX();
                            initialTouchY = event.getRawY();

                            if (firstTouch && (System.currentTimeMillis() - time) <= ViewConfiguration.getDoubleTapTimeout()) {
                                //DOUBLE tap

                                Intent prefActivity = new Intent(getBaseContext(),
                                        MyPreferenceActivity.class);
                                startActivity(prefActivity);

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
                            // touchEndTime = System.currentTimeMillis();

                            // LONG press
                            /*
                            if (touchEndTime - touchStartTime > ViewConfiguration.getLongPressTimeout()
                                    && Math.abs(initialTouchX - event.getRawX()) <= dragThreshold) {

                                stopMyService();
                                firstTouch = false;
                            }
                            */
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
        // Save last Bubble position
        preferences.edit().putInt("posX", myParams.x).apply();
        preferences.edit().putInt("posY", myParams.y).apply();

        // remove view and stop the service
        if (bubbleVisible) {
            windowManager.removeView(floatingFaceBubble);
            bubbleVisible = false;
        }

        h.removeCallbacksAndMessages(null);

        stopSelf();

        Log.d(TAG, "stopMyService: " + "stopMyService");
    }

    private void setBirthDayInfoByMills(Long mills) {
        birthDt = new DateTime(mills);
        if (birthDt.isBeforeNow()) {
            birthDay = birthDt.getDayOfMonth();
            birthMinOfDay = birthDt.getMinuteOfDay();
            Log.d(TAG, "setBirthDayInfoByMills: " + birthDay + " " + birthMinOfDay);
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

        Log.d(TAG, "onDestroy: " + "DESTROY");

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
                        if (bubbleVisible) {
                            windowManager.updateViewLayout(floatingFaceBubble, myParams);
                        }

                    } else if (extras.getString("name") != null) {
                        name = extras.getString("name");

                        h.removeCallbacksAndMessages(null);
                        postDelayed();

                    } else if (extras.getInt("scale", -1) != -1) {
                        floatingFaceBubble.setImageScale(extras.getInt("scale"));
                        if (bubbleVisible) {
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

                // windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
                if (windowManager != null)
                    showBubble(windowManager.getDefaultDisplay().getRotation() == Surface.ROTATION_0
                            ? Configuration.ORIENTATION_PORTRAIT : Configuration.ORIENTATION_LANDSCAPE);

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
            if (state) { // Add View
                if (!bubbleVisible && !preferences.getBoolean("hiddenByLongpress", false)) {
                    windowManager.addView(floatingFaceBubble, myParams);
                    bubbleVisible = true;
                }
            } else { // Remove View
                if (bubbleVisible) {
                    windowManager.removeView(floatingFaceBubble);
                    bubbleVisible = false;
                }
            }
        } else {
            Log.d(TAG, "showBubble: windowManager or myParams or floatingFaceBubble => null");
        }
    }

}


