package at.android.princesslea;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.widget.ImageView;

public class MyImageView extends ImageView {

    private String text = "x";

    public MyImageView(Context context) {
        super(context);

        // TODO Auto-generated constructor stub
    }

    public void setText(String text) {
        this.text = text;
    }

    // https://stackoverflow.com/questions/6756975/draw-multi-line-text-to-canvas

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        Resources resources = getContext().getResources();
        float scale = resources.getDisplayMetrics().density;

        // drawRoundImage(canvas);
        if (FloatingFaceBubbleService.imageType == 1) {
            drawButterflyImage(canvas);
        } else if (FloatingFaceBubbleService.imageType == 2) {
            drawHeartImage(canvas);
        }


    }

    private void drawHeartImage(Canvas canvas) {
        int x_init = -75;
        int x = 0;
        int y = 125;

        Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
        //p.setColor(Color.WHITE);
        //p.setTypeface(Typeface.createFromAsset(getContext().
        //        getAssets(), "fonts/segoeui.ttf"));
        p.setTypeface(Typeface.createFromAsset(getContext().
                getAssets(), "fonts/SouthernAire.ttf"));

        p.setTextSize((int) (90));//  scale));
        //p.setFakeBoldText(true);

        // canvas.drawLine(0, 0, 200, 200, p);
        int row = 0;
        for (String line : text.split("\n")) {
            row++;
            if (row >= 2) {
                p.setTypeface(null);
                //p.setTypeface(Typeface.createFromAsset(getContext().
                //        getAssets(), "fonts/segoeui.ttf"));
                if (row == 2) {
                    y -= 55;
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

    private void drawButterflyImage(Canvas canvas) {
        int x_init = -65;
        int x = 0;
        int y = 175;

        Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
        //p.setColor(Color.WHITE);
        //p.setTypeface(Typeface.createFromAsset(getContext().
        //        getAssets(), "fonts/segoeui.ttf"));
        p.setTypeface(Typeface.createFromAsset(getContext().
                getAssets(), "fonts/SouthernAire.ttf"));

        p.setTextSize((int) (90));//  scale));
        //p.setFakeBoldText(true);

        // canvas.drawLine(0, 0, 200, 200, p);
        int row = 0;
        for (String line : text.split("\n")) {
            row++;
            if (row >= 2) {
                p.setTypeface(null);
                //p.setTypeface(Typeface.createFromAsset(getContext().
                //        getAssets(), "fonts/segoeui.ttf"));
                if (row == 2) {
                    y -= 60;
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

    private void drawFatText(String line, int x, int y, Paint p, Canvas canvas, int stroke) {
        p.setStyle(Paint.Style.STROKE);
        p.setStrokeWidth(stroke);
        p.setColor(Color.BLACK);
        //p.setAlpha(100);
        canvas.drawText(line, x, y, p);

        p.setStyle(Paint.Style.FILL);

        //p.setStrokeWidth(0);
        p.setColor(Color.WHITE);
        //p.setAlpha(210);
        canvas.drawText(line, x, y, p);

        //return p;
    }

    private void drawRoundImage(Canvas canvas) {
        Resources resources = getContext().getResources();
        float scale = resources.getDisplayMetrics().density;

        int x = 0;
        int y = 255;

        Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
        p.setColor(Color.WHITE);
        p.setTextSize((int) (48));//  scale));
        p.setFakeBoldText(true);

        // p.setAlpha(210);
        // canvas.drawLine(0, 0, 200, 200, p);
        int row = 0;
        for (String line : text.split("\n")) {
            row++;
            if (row >= 2) {
                if (row == 2)
                    y -= 9;
                p.setTextSize(38);
            }
            // centered text
            Rect bounds = new Rect();
            p.getTextBounds(line, 0, line.length(), bounds);
            x = (this.getWidth() - bounds.width()) / 2;
            canvas.drawText(line, x, y, p);
            y += p.descent() - p.ascent();
        }
        // canvas.drawText(text, 220, 150, p);

    }

}