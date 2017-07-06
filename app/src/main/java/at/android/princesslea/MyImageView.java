package at.android.princesslea;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
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
        Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
        p.setTextSize(50.0f);
        p.setFakeBoldText(true);
        // canvas.drawLine(0, 0, 200, 200, p);
        canvas.drawText(text, 220, 150, p);

    }

}