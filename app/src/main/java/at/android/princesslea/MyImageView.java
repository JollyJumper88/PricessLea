package at.android.princesslea;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
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

        int x = 0;
        int y = 245;

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
                    y-=9;
                p.setTextSize(38);
            }

            Rect bounds = new Rect();
            p.getTextBounds(line, 0, line.length(), bounds);
            x = (this.getWidth() - bounds.width()) / 2;
            canvas.drawText(line, x, y, p);
            y += p.descent() - p.ascent();
        }
        // canvas.drawText(text, 220, 150, p);

    }

}