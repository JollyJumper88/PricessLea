package at.android.princesslea;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import static at.android.princesslea.R.drawable.floating_bubble_0;
import static at.android.princesslea.R.drawable.floating_bubble_1_360x320;
import static at.android.princesslea.R.drawable.floating_bubble_2_300x278;

public class MyImageView extends ImageView {

    private String text = "";
    private String name = "";
    private int imageType, imageScale;
    private WindowManager.LayoutParams mLayoutParams;
    private SharedPreferences preferences;
    private final String TAG = "MyImG";

    public MyImageView(Context context, WindowManager.LayoutParams mLayoutParams) {
        super(context);

        // params reference from service (windowmanager) to change the size
        this.mLayoutParams = mLayoutParams;

        // Preferences
        preferences = PreferenceManager.getDefaultSharedPreferences(context);
        imageType = preferences.getInt("imagetype", 2);
        imageScale = Integer.parseInt(preferences.getString("size_list", "10"));
        name = preferences.getString("name", "Put name here");


        nextImage();

    }

    /**
     * Define scale of the bubble - 10, 15, 20
     * @param imageScale
     */
    public void setImageScale(int imageScale) {
        this.imageScale = imageScale;


        resizeMyImage();
    }

    public void nextImage() {
        // save last image type
        preferences.edit().putInt("imagetype", imageType).apply();
        Log.d(TAG, "nextImage: ");
        imageType++;

/*
        switch (imageType %= 3) {
            case 0: // Round
                setImageResource(floating_bubble_0);
                break;
            case 1: // Butterfly
                setImageResource(floating_bubble_1_360x320);
                break;
            case 2: // Heart
                setImageResource(floating_bubble_2_300x278);
                break;
            default:
                break;
        }
*/

        Bitmap mask = null;
        switch (imageType %= 4) {
            case 0: // Heart
                mask = BitmapFactory.decodeResource(getResources(),R.drawable.heart_mask);
                break;
            case 1: // Star
                mask = BitmapFactory.decodeResource(getResources(),R.drawable.star_mask);
                break;
           case 2: // Flower
                mask = BitmapFactory.decodeResource(getResources(),R.drawable.flower_mask);
                break;
           case 3: // Flower2
                mask = BitmapFactory.decodeResource(getResources(),R.drawable.flower2_mask);
                break;
            default:
                break;
        }

        Bitmap original = BitmapFactory.decodeResource(getResources(),R.drawable.lea);
        int edgeLength = 0;

        original = Bitmap.createScaledBitmap(original, mask.getWidth(),mask.getHeight(), true);
        Bitmap result = Bitmap.createBitmap(mask.getWidth(), mask.getHeight(), Bitmap.Config.ARGB_8888);

        Canvas mCanvas = new Canvas(result);
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_IN));
        mCanvas.drawBitmap(original, 0, 0, null);
        mCanvas.drawBitmap(mask, 0, 0, paint);
        setImageBitmap(result);
        setScaleType(ScaleType.FIT_XY);

        // update the image
        resizeMyImage();

    }

    private void resizeMyImage() {

        mLayoutParams.width  = 30 * imageScale;
        mLayoutParams.height = 30 * imageScale;
        /*
        switch (imageType) {
            case 0: // Round
                mLayoutParams.width = 28 * imageScale;
                mLayoutParams.height = 28 *imageScale;
                break;
            case 1: // Butterfly
                mLayoutParams.width = 36 * imageScale;
                mLayoutParams.height = 36 * imageScale;
                break;
            case 2: // Heart
                mLayoutParams.width = 32 * imageScale;
                mLayoutParams.height = 32 * imageScale;
                break;
            default:
                break;
        }
        */

    }


    public void setText(String text) {
        this.text = text;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        //Resources resources = getContext().getResources();
        //float scale = resources.getDisplayMetrics().density;

        /*
        switch (imageType) {
            case 0:
                drawRoundImage(canvas);
                break;
            case 1:
                drawButterflyImage(canvas);
                break;
            case 2:
                drawHeartImage(canvas);
                break;
            default:
                break;
        }
        */

        drawTextOnImage(canvas);

    }

    private void drawTextOnImage(Canvas canvas) {
        //Resources resources = getContext().getResources();
        //float scale = resources.getDisplayMetrics().density;

        int x;
//        int y = 180;
        int y = 18 * imageScale;

        Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
        p.setTypeface(Typeface.createFromAsset(getContext().
                getAssets(), "fonts/SouthernAire.ttf"));
        // p.setTextSize(68);
        p.setTextSize(6.8f * imageScale);
        p.setColor(Color.WHITE);
        p.setFakeBoldText(true);
        text = name + "\n" + text;

        int row = 0;
        for (String line : text.split("\n")) {
            row++;
            if (row >= 2) {
                p.setTypeface(null);
                if (row == 2) {
                    // y -= 30;
                    y -= 3 * imageScale;
                }
                // p.setTextSize(32);
                p.setTextSize(3.2f * imageScale);
            }
            // centered text
            Rect bounds = new Rect();
            p.getTextBounds(line, 0, line.length(), bounds);
            x = (this.getWidth() - bounds.width()) / 2;
            // canvas.drawText(line, x, y, mLayoutParams);
            drawFatText(line, x, y, p, canvas, 4);
            y += p.descent() - p.ascent();
        }
    }

    private void drawHeartImage(Canvas canvas) {
        int x_init = -75;
        int x;
        int y = 135;

        Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
        //mLayoutParams.setColor(Color.WHITE);
        //mLayoutParams.setTypeface(Typeface.createFromAsset(getContext().
        //        getAssets(), "fonts/segoeui.ttf"));
        p.setTypeface(Typeface.createFromAsset(getContext().
                getAssets(), "fonts/SouthernAire.ttf"));

        p.setTextSize(100);

        int row = 0;
        for (String line : text.split("\n")) {
            row++;
            if (row >= 2) {
                p.setTypeface(null);
                //mLayoutParams.setTypeface(Typeface.createFromAsset(getContext().
                //        getAssets(), "fonts/segoeui.ttf"));

                // spacing between row 1 and row 2
                if (row == 2) {
                    y -= 60;
                }
                x_init += 10;
                p.setTextSize(25);
            }
            // centered text
            Rect bounds = new Rect();
            p.getTextBounds(line, 0, line.length(), bounds);
            x = x_init + (this.getWidth() - bounds.width()) / 2;

            drawFatText(line, x, y, p, canvas, 3);

            y += p.descent() - p.ascent();
        }
    }

    private void drawButterflyImage(Canvas canvas) {
        int x_init = -65;
        int x;
        int y = 175;

        Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
        //mLayoutParams.setColor(Color.WHITE);
        //mLayoutParams.setTypeface(Typeface.createFromAsset(getContext().
        //        getAssets(), "fonts/segoeui.ttf"));
        p.setTypeface(Typeface.createFromAsset(getContext().
                getAssets(), "fonts/SouthernAire.ttf"));

        p.setTextSize(100);

        // canvas.drawLine(0, 0, 200, 200, mLayoutParams);
        int row = 0;
        for (String line : text.split("\n")) {
            row++;
            if (row >= 2) {
                p.setTypeface(null);
                //mLayoutParams.setTypeface(Typeface.createFromAsset(getContext().
                //        getAssets(), "fonts/segoeui.ttf"));

                // spacing between row 1 and row 2
                if (row == 2) {
                    y -= 70;
                }
                x_init += 5;
                p.setTextSize(25);
            }
            // centered text
            Rect bounds = new Rect();
            p.getTextBounds(line, 0, line.length(), bounds);
            x = x_init + (this.getWidth() - bounds.width()) / 2;

            drawFatText(line, x, y, p, canvas, 3);

            y += p.descent() - p.ascent();

        }
    }

    private void drawRoundImage(Canvas canvas) {
        //Resources resources = getContext().getResources();
        //float scale = resources.getDisplayMetrics().density;

        int x;
//        int y = 180;
        int y = 18 * imageScale;

        Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
        p.setTypeface(Typeface.createFromAsset(getContext().
                getAssets(), "fonts/SouthernAire.ttf"));
        // p.setTextSize(68);
        p.setTextSize(6.8f * imageScale);
        p.setColor(Color.WHITE);
        p.setFakeBoldText(true);
        text = "Princess " + text;

        int row = 0;
        for (String line : text.split("\n")) {
            row++;
            if (row >= 2) {
                p.setTypeface(null);
                if (row == 2) {
                    // y -= 30;
                    y -= 3 * imageScale;
                }
                // p.setTextSize(32);
                p.setTextSize(3.2f * imageScale);
            }
            // centered text
            Rect bounds = new Rect();
            p.getTextBounds(line, 0, line.length(), bounds);
            x = (this.getWidth() - bounds.width()) / 2;
            // canvas.drawText(line, x, y, mLayoutParams);
            drawFatText(line, x, y, p, canvas, 4);
            y += p.descent() - p.ascent();
        }
    }

    private void drawFatText(String line, int x, int y, Paint p, Canvas canvas, int stroke) {
        p.setStyle(Paint.Style.STROKE);
        p.setStrokeWidth(stroke);
        p.setColor(Color.BLACK);
        canvas.drawText(line, x, y, p);
        p.setStyle(Paint.Style.FILL);
        p.setColor(Color.WHITE);
        canvas.drawText(line, x, y, p);
    }
}