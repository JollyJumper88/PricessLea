package at.android.lovebubble;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.PixelFormat;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
import android.widget.Toast;

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
    private String name = "";
    private String bubbleText = "";
    // private String format = "";

    private int timeformat = 0;

    private boolean switch_time, switch_name;

    private WindowManager windowManager;
    private WindowManager.LayoutParams myParams;
    private MyImageView floatingFaceBubble;
    //private RelativeLayout layout;
    //private TextView textView;

    private Handler h = new Handler(Looper.getMainLooper());

    private SimpleDateFormat formater = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.GERMANY);
    private DateTime birthDt, currDt;

    // private boolean stopService = false;
    private int delay;

    private MyBroadcastReceiver mBroadcastReceiver;

    int month = 0, days = 0, hours = 0, min = 0, sec = 0, birthDay = 0/*13*/, birthMinOfDay = 0/*1142*/;
    private SharedPreferences preferences;

/*    private Runnable mUpdateTimeTask = new Runnable() {
        public void run() {
            String s = java.text.DateFormat.getTimeInstance()*//*DateTimeInstance()*//*.format(Calendar.getInstance().getTime());
            floatingFaceBubble.setText(s);
            h.postDelayed(mUpdateTimeTask, 100);
        }
    };*/


    public void onCreate() {
        super.onCreate();

        // Receiver registration
        if (mBroadcastReceiver == null)
            mBroadcastReceiver = new MyBroadcastReceiver();
        IntentFilter intentFilter = new IntentFilter("pref_broadcast");
        registerReceiver(mBroadcastReceiver, intentFilter);


        // Floating Bubble
        myParams = new WindowManager.LayoutParams(
                LayoutParams.WRAP_CONTENT,
                LayoutParams.WRAP_CONTENT,
                LayoutParams.TYPE_PHONE,
                LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT);
        myParams.gravity = Gravity.TOP | Gravity.START;
        myParams.x = 0;
        myParams.y = 100;

        floatingFaceBubble = new MyImageView(this, myParams);


        /* // TextView
        textView = new TextView(this);
        LayoutParams textViewLayout = new WindowManager.LayoutParams(
                LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT, LayoutParams.FLAG_NOT_FOCUSABLE);
        textView.setLayoutParams(textViewLayout);
        textView.setGravity(Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL);
        textView.setTextSize(30);
        textView.setTextColor(Color.WHITE);
        Paint p = new Paint();
        p.setStyle(Paint.Style.FILL);
        textView.setLayerPaint(p);
        textView.setShadowLayer(10f, 1f, 1f, Color.BLACK); */
        // RelativeLayout
        // layout = new RelativeLayout(this);
        // layout.addView(floatingFaceBubble);
        //layout.addView(textView);


        // WindowManager
        windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        // windowManager.addView(layout, myParams);
        windowManager.addView(floatingFaceBubble, myParams);


        // Load Preferences
        preferences = PreferenceManager.getDefaultSharedPreferences(this);
        name = preferences.getString("name", getString(R.string.putnamehere));
        textformat = (byte) Integer.parseInt(preferences.getString("textformat", "0"));
        Long mills = preferences.getLong("birthdatetime", -1);
        switch_time = preferences.getBoolean("switch_time", true);
        switch_name = preferences.getBoolean("switch_name", true);

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

                Log.d(TAG, "run: Post DELAYED  :" + new DateTime());

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


                    if (switch_name) { /*Name and Time*/
                        bubbleText = "name=" + name;
                    } else {
                        bubbleText = "";
                    }

                    if (timeformat == 0) {
                        bubbleText += "\n" + month + "m " +
                                days + "d " +
                                hours + "h \n" +
                                min + "m " +
                                sec + "s";

                    } else if(timeformat == 1) {
                        delay = 60000;
                        Weeks diffWeeks = Weeks.weeksBetween(currDt, birthDt);
                        int weeks = diffWeeks.negated().getWeeks();
                        //period.getWeeks();
                        bubbleText += "\n" + weeks + " Weeks";

                    } else if(timeformat == 2) {
                        delay = 60000;
                        bubbleText += "\n" + month + " Months";

                    }else if(timeformat == 3) {
                        delay = 60000;
                        bubbleText += "\n" + month/12 + " Years" + "\n"
                                            + month%12 + " Months";
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


                // if (!stopService)
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

                                // Toast.makeText(getApplicationContext(), "Hit the Back Button to close the Settings View.", Toast.LENGTH_SHORT).show();

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
        // remove view and stop the service
        windowManager.removeView(floatingFaceBubble);
        // windowManager.removeView(layout);

        //Todo: check this part here if we can remove the stopservice member
        h.removeCallbacksAndMessages(null);

        stopSelf();

        // stopService = true;

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


//    private void choosePicFromGallery() {
//        Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
//        photoPickerIntent.setType("image/*");
//        startActivityforResult();
//    }


    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void onDestroy() {
        if (mBroadcastReceiver != null)
            unregisterReceiver(mBroadcastReceiver);

        //Todo:
        h.removeCallbacksAndMessages(null);

        Log.d(TAG, "onDestroy: " + "DESTROY");

        super.onDestroy();
    }

    class MyBroadcastReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(TAG, "onReceive: " + "MyBroadcastReceiver" + intent.getAction());

            //if (intent.getAction().equals("pref_broadcast")) {
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

                        }
                    } catch (NullPointerException e) {
                        Log.e(TAG, "onReceive: ", e);
                    }
                } else if (extras.getString("choosepic") != null) {
                    preferences.edit().putString("imguri", extras.getString("choosepic")).apply();
                    floatingFaceBubble.setImageFromUri(extras.getString("choosepic"));

                } else if (extras.getString("mask") != null) {
                    floatingFaceBubble.setMaskByName(extras.getString("mask"));
                    windowManager.updateViewLayout(floatingFaceBubble, myParams);

                } else if (extras.getString("name") != null) {
                    name = extras.getString("name");

                    h.removeCallbacksAndMessages(null);
                    postDelayed();

                } else if (extras.getInt("scale", -1) != -1) {
                    floatingFaceBubble.setImageScale(extras.getInt("scale"));
                    windowManager.updateViewLayout(floatingFaceBubble, myParams);
                    //windowManager.updateViewLayout(layout, myParams);
                } else if (extras.getLong("birthdatetime", -1) != -1) {
                    preferences.edit().putLong("birthdatetime", extras.getLong("birthdatetime")).apply();
                    Long mills = extras.getLong("birthdatetime");
                    setBirthDayInfoByMills(mills);
                    // birthDt = new DateTime(x);
                    // Log.d(TAG, "XXXXX " + birthDt.toString());
                    //DateTime activity_privacy_policy = new DateTime(extras.getLong("birthdatetime"));
                    //Log.d(TAG, "onReceive: "+ activity_privacy_policy.toString());
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

    /*
    private void loadPreferences() {

        // for building the text on the bubble
        //name = preferences.getString("name", "Put name here");
        //textformat = (byte) Integer.parseInt(preferences.getString("textformat", "0"));


        //imgUri = preferences.getString("imguri", null);
        // timeformat = (byte) Integer.parseInt(preferences.getString("timeformat", "0"));
        // Log.i(TAG, "loadOrUpdatePreferences: "  + timeformat + " " + textformat);

    }
    */

}


