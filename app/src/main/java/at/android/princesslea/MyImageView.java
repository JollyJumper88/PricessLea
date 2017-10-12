package at.android.princesslea;

import android.annotation.SuppressLint;
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
import android.net.Uri;
import android.os.ParcelFileDescriptor;
import android.preference.PreferenceManager;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.FileDescriptor;
import java.io.FileNotFoundException;
import java.io.IOException;

@SuppressLint("AppCompatCustomView")
public class MyImageView extends ImageView {

    private String text = "";
    private String imgUri;
    private String maskName;
    private int imageScale;

    private Bitmap mask = null;
    private Bitmap image = null;

    private WindowManager.LayoutParams mLayoutParams;
    // private final String TAG = "MyImG";
    private Context context;

    public MyImageView(Context context, WindowManager.LayoutParams mLayoutParams) {
        super(context);

        this.context = context;

        // params reference from service (windowmanager) to change the size
        this.mLayoutParams = mLayoutParams;

        loadPreferences();

        // createDrawablesHashtable();

        updateMask();

        if (imgUri != null)
            setImageFromUri(imgUri);
        else
            setImageFromBitmap(null);


    }
    /*
    private void createDrawablesHashtable() {
        Field[] drawables = R.drawable.class.getFields();
        drawablesHashtable = new Hashtable<Integer, String>();
        int index = 0;
        for (Field f : drawables) {
            //if the drawable name contains "pic" in the filename...
            if (f.getName().startsWith("mask_")) {
                drawablesHashtable.put(index, f.getName());
                index++;
            }
        }
        maskCount = index;
        Log.d(TAG, "createDrawablesHashtable: Masks found = " + maskCount);
    }
    */

    private void loadPreferences() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);

        imageScale = Integer.parseInt(preferences.getString("size_list", "10"));
        imgUri = preferences.getString("imguri", null);
        maskName = preferences.getString("mask_list", "star");

    }
    /*
    public void nextMask() {
        // increase and save last image type
        imageType++;
        imageType %= maskCount;
        preferences.edit().putInt("imagetype", imageType).apply();

        updateMask();
        setImageFromBitmap(null);
    }
    */

    public void setMaskByName(String mask) {
        this.maskName = mask;

        updateMask();

        setImageFromBitmap(null);/*null -> use current image (update mask)*/
    }

    private void updateMask() {

        mask = BitmapFactory.decodeResource(getResources(),
                getResources().getIdentifier(maskName, "drawable", context.getPackageName()));
        /*
        switch (imageType %= 4) {
            case 0: // Heart
                mask = BitmapFactory.decodeResource(getResources(), R.drawable.heart_mask);
                break;
            default:
                break;
        }*/
    }

    public void setImageScale(int imageScale) {
        // Define scale of the bubble - 10, 15, 20
        this.imageScale = imageScale;

        resizeMyImage();
    }


    public void setImageFromBitmap(Bitmap img/*null -> use current image (update mask)*/) {

        //call UpdateMask first! - Member mask must not be null
        assert mask != null;

        if (img != null) { // use new image
            image = img;
        } else if (image == null) { // use default
            image = BitmapFactory.decodeResource(getResources(), R.drawable.lea);
        }
        // else - use current

        image = Bitmap.createScaledBitmap(image, mask.getWidth(), mask.getHeight(), true);
        Bitmap result = Bitmap.createBitmap(mask.getWidth(), mask.getHeight(), Bitmap.Config.ARGB_8888);

        Canvas mCanvas = new Canvas(result);
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_IN));
        mCanvas.drawBitmap(image, 0, 0, null);
        mCanvas.drawBitmap(mask, 0, 0, paint);
        setImageBitmap(result);
        setScaleType(ScaleType.FIT_XY);

        // update the size
        resizeMyImage();

    }

    public void setImageFromUri(String selectedImageUriString) {
        Uri selectedImageUri = Uri.parse(selectedImageUriString);
        try {
            ParcelFileDescriptor parcelFileDescriptor = context.getContentResolver().openFileDescriptor(selectedImageUri, "r");
            FileDescriptor fileDescriptor;
            if (parcelFileDescriptor != null) {
                fileDescriptor = parcelFileDescriptor.getFileDescriptor();
                Bitmap image = BitmapFactory.decodeFileDescriptor(fileDescriptor);
                parcelFileDescriptor.close();

                setImageFromBitmap(image);
            } else {
                Toast.makeText(context, R.string.errorcache1, Toast.LENGTH_SHORT).show();
            }


        } catch (FileNotFoundException e) {
            Toast.makeText(context, R.string.errorcache2, Toast.LENGTH_SHORT).show();
            setImageFromBitmap(null);
            e.printStackTrace();

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private void resizeMyImage() {

        mLayoutParams.width = 30 * imageScale;
        mLayoutParams.height = 30 * imageScale;

    }


    public void setText(String text) {
        this.text = text;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        //Resources resources = getContext().getResources();
        //float scale = resources.getDisplayMetrics().density;

        drawTextOnImage(canvas);

    }

    private void drawTextOnImage(Canvas canvas/*, byte textformat*/) {
        //Resources resources = getContext().getResources();
        //float scale = resources.getDisplayMetrics().density;
        //p.setTypeface(Typeface.createFromAsset(getContext().getAssets(), "fonts/SouthernAire.ttf"));

        int x;
        int y = 19 * imageScale; // 190
        int y_offsetCorrection;

        Rect bounds = new Rect();

        String[] lines = text.split("\n");

        Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);

        for (String line : lines) {
            if (line.startsWith("name=")) {
                line = line.replace("name=", "");
                p.setTextSize(4.5f * imageScale);//68
                y_offsetCorrection = (int) (1.6f * imageScale);
            } else {
                p.setTextSize(3.2f * imageScale);
                y_offsetCorrection = 0;
            }

            // centered text
            p.getTextBounds(line, 0, line.length(), bounds);
            x = (this.getWidth() - bounds.width()) / 2;
            drawFatText(line, x, y, p, canvas, 4);
            y += p.descent() - p.ascent() - y_offsetCorrection;

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


    /////////////////////////////////////////////////////////////////////////////////////////

/*
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
*/
}