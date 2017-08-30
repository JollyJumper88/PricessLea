package at.android.princesslea;

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
    // private String format = "";

    private byte textformat = 0/*, timeformat = 0*/;

    private WindowManager windowManager;
    private WindowManager.LayoutParams myParams;
    private MyImageView floatingFaceBubble;
    //private RelativeLayout layout;
    //private TextView textView;

    private Handler h = new Handler(Looper.getMainLooper());

    private SimpleDateFormat formater = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.GERMANY);
    private DateTime birthDt, currDt;

    private boolean quitService = false;
    private int delay = 1000;

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
        name = preferences.getString("name", "Put name here");
        textformat = (byte) Integer.parseInt(preferences.getString("textformat", "0"));

        // Load BirthDate from Preferences
        Date birth;
        Long mills = preferences.getLong("birthdatetime", -1);

        if (mills != -1) {
            setBirthDayInfoByMills(mills);
        } else {
            try {
                birth = formater.parse("2017-06-13 19:02:00");
                // birth = formater.parse("2017-03-17 15:02:00");
                birthDt = new DateTime(birth);
                birthDay = 13;
                birthMinOfDay = 1142;
            } catch (ParseException e) {
                birth = new Date();
                birthDt = new DateTime(birth);
                e.printStackTrace();
            }
        }


        h.postDelayed(new Runnable() {
            public void run() {
                // textformat
                //<item>0 Name and Time</item>
                //<item>1 Name only</item>
                //<item>2 Time only</item>
                //<item>3 No Text</item>
                // timeformat
                //<item>0 Full</item>
                //<item>1 Weeks</item>
                //<item>2 Month</item>
                //<item>3 Years and Month</item>

                delay = 1000;

                if (textformat == 0 || textformat == 2) {

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

                    String s = "";
                    if (textformat == 0) {

                        s = "name=" + name;
                    }
                    s += "\n" + month + "m " +
                            days + "d " +
                            hours + "h \n" +
                            min + "m " +
                            sec + "s";

                    // Log.d(TAG, "run: " + s);
                    // String s = String.format(format, "%1$s");

                    //textView.setText(s);
                    floatingFaceBubble.setText(s);
                    floatingFaceBubble.invalidate();

                } else if (textformat == 1) { // Name only
                    floatingFaceBubble.setText("\n" + "name=" + name + "\n");
                    floatingFaceBubble.invalidate();

                } else { // nothing / emtpy
                    floatingFaceBubble.setText("");
                    floatingFaceBubble.invalidate();
                }

                if (!quitService)
                    h.postDelayed(this, delay);
            }
        }, 100);


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

                                /*
                                // trigger next image resource change and also set size via params object
                                floatingFaceBubble.nextMask();
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
                            // touchEndTime = System.currentTimeMillis();

                            // LONG press
                            /*
                            if (touchEndTime - touchStartTime > ViewConfiguration.getLongPressTimeout()
                                    && Math.abs(initialTouchX - event.getRawX()) <= dragThreshold) {

                                stopMyService();

                                // trigger next image resource change and also set size via params object
                                floatingFaceBubble.nextMask();
                                windowManager.updateViewLayout(v, myParams);


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
        stopSelf();
        quitService = true;

        Log.d(TAG, "stopMyService: " + "stopMyService");
    }

    private void setBirthDayInfoByMills(Long mills) {
        birthDt = new DateTime(mills);
        if (birthDt.isBeforeNow()) {
            birthDay = birthDt.getDayOfMonth();
            birthMinOfDay = birthDt.getMinuteOfDay();
            Log.d(TAG, "setBirthDayInfoByMills: " + birthDay + " " + birthMinOfDay);
        } else {
            Toast.makeText(this, "Birthday must be set before now!", Toast.LENGTH_SHORT).show();
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
                    try {
                        if ("exit".equalsIgnoreCase(extras.getString("action"))) {
                            stopMyService();

                        } else if ("nextmask".equalsIgnoreCase(extras.getString("action"))) {
                            floatingFaceBubble.nextMask();
                            windowManager.updateViewLayout(floatingFaceBubble, myParams);

                        } else if ("textformat".equalsIgnoreCase(extras.getString("action"))) {
                            textformat = (byte) Integer.parseInt(preferences.getString("textformat", "0"));

                        }
                    } catch (NullPointerException e) {
                        Log.e(TAG, "onReceive: ", e);
                    }
                } else if (extras.getString("choosepic") != null) {
                    preferences.edit().putString("imguri", extras.getString("choosepic")).apply();
                    floatingFaceBubble.setImageFromUri(extras.getString("choosepic"));

                } else if (extras.getString("name") != null) {
                    name = extras.getString("name");

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
                    //DateTime test = new DateTime(extras.getLong("birthdatetime"));
                    //Log.d(TAG, "onReceive: "+ test.toString());
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


