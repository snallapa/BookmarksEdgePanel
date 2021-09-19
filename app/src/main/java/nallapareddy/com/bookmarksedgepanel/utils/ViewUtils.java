package nallapareddy.com.bookmarksedgepanel.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;

import com.amulyakhare.textdrawable.TextDrawable;


public class ViewUtils {

    public static Drawable getTileDrawableEdge(Context context, String text, int color) {
        return TextDrawable.builder().beginConfig().withBorder(4).endConfig()
                .buildRoundRect(text, context.getColor(color), 10);
    }

    public static Drawable getTileDrawable(Context context, String text, int color) {
        return TextDrawable.builder().beginConfig().withBorder(4).endConfig()
                .buildRoundRect(text, context.getColor(color), 30);
    }


    public static Bitmap drawableToBitmap (Drawable drawable) {

        if (drawable instanceof BitmapDrawable) {
            return ((BitmapDrawable)drawable).getBitmap();
        }
        int width = 100;
        int height = 100;
        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);

        return bitmap;
    }
}
