package at.android.princesslea;

import android.app.Service;
import android.content.Intent;
import android.graphics.PixelFormat;
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
import android.widget.ImageView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import static at.android.princesslea.R.drawable.floating_bubble;

public class FloatingFaceBubbleService extends Service {

    private WindowManager windowManager;
    private Date birth;
    private MyImageView floatingFaceBubble;
    private boolean mip = false;
    private Handler h = new Handler(Looper.getMainLooper());
    private SimpleDateFormat formater = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");


/*    private Runnable mUpdateTimeTask = new Runnable() {
        public void run() {
            String s = java.text.DateFormat.getTimeInstance()*//*DateTimeInstance()*//*.format(Calendar.getInstance().getTime());
            floatingFaceBubble.setText(s);
            h.postDelayed(mUpdateTimeTask, 100);
        }
    };*/

    public void onCreate() {
        super.onCreate();

        try {
            birth = formater.parse("2017-06-13 19:02:00");
        } catch (ParseException e) {
            birth = new Date();
            e.printStackTrace();
        }

        floatingFaceBubble = new MyImageView(this);
        // floatingFaceBubble.setText("hal");
        //a face floating bubble as imageView
        floatingFaceBubble.setImageResource(floating_bubble);

        windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        //here is all the science of params
        final WindowManager.LayoutParams myParams = new WindowManager.LayoutParams(
                LayoutParams.WRAP_CONTENT,
                LayoutParams.WRAP_CONTENT,
                LayoutParams.TYPE_PHONE,
                LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT);


        myParams.gravity = Gravity.TOP | Gravity.START;
        myParams.x = 0;
        myParams.y = 100;
        myParams.height = 300;
        myParams.width = 600;

        windowManager.addView(floatingFaceBubble, myParams);

        final int delay = 100;
        h.postDelayed(new Runnable() {
            public void run() {

                String s = "";

                // Date date1 = formater.parse("2017-06-13 19:02:00");
                Date date2 = new Date(); //formater.parse(dateInit);

                long diffInMillisec = date2.getTime() - birth.getTime();

                long diffInDays = TimeUnit.MILLISECONDS.toDays(diffInMillisec);
                long diffInHours = TimeUnit.MILLISECONDS.toHours(diffInMillisec)
                        - 24 * diffInDays;
                long diffInMin = TimeUnit.MILLISECONDS.toMinutes(diffInMillisec)
                        - (60 * diffInHours + 24 * diffInDays * 60);
                long diffInSec = TimeUnit.MILLISECONDS.toSeconds(diffInMillisec)
                        % 60;

                    /*
                    // formater = new SimpleDateFormat("HH:mm:ss")*//*);
                    long diff = date2.getTime() - date1.getTime();
                    long seconds = diff / 1000;
                    long minutes = seconds / 60;
                    long hours = minutes / 60;
                    long days = hours / 24;
                    s = days + " " + hours + " " + minutes + " " + seconds;
                    */

                s = diffInDays + "d " +
                        diffInHours + "h " +
                        diffInMin + "m " +
                        diffInSec + "s";

                floatingFaceBubble.setText(s);
                floatingFaceBubble.invalidate();
                h.postDelayed(this, delay);
            }
        }, delay);


        try {
            //for moving the picture on touch and slide
            floatingFaceBubble.setOnTouchListener(new View.OnTouchListener() {
                WindowManager.LayoutParams paramsT = myParams;
                private int initialX;
                private int initialY;
                private float initialTouchX;
                private float initialTouchY;
                private long touchStartTime, touchEndTime = 0;

                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    //remove face bubble on long press
                    if (System.currentTimeMillis() - touchStartTime > ViewConfiguration.getLongPressTimeout() && initialTouchX == event.getX()) {
                        windowManager.removeView(floatingFaceBubble);
                        stopSelf();
                        return false;

                    }
                    switch (event.getAction()) {
                        case MotionEvent.ACTION_DOWN:
                            touchStartTime = System.currentTimeMillis();
                            initialX = myParams.x;
                            initialY = myParams.y;
                            initialTouchX = event.getRawX();
                            initialTouchY = event.getRawY();
                            break;
                        case MotionEvent.ACTION_UP:
                            touchEndTime = System.currentTimeMillis();
                            if (touchEndTime - touchStartTime < 50/*ms*/) {
                                //if (!mip) {
                                windowManager.removeView(floatingFaceBubble);
                                stopSelf();
                                return false;
                            }
                            //else
                            //    mip = false;
                            break;
                        case MotionEvent.ACTION_MOVE:
                            mip = true;
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

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
/*
arcs https://stackoverflow.com/questions/39206733/android-drawing-an-arc-inside-a-circle
transparant box mulitline text https://stackoverflow.com/questions/6756975/draw-multi-line-text-to-canvas
gesture double tap https://stackoverflow.com/questions/4804798/doubletap-in-android

 */