package nallapareddy.com.bookmarksedgepanel.receivers;


import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.util.Log;
import android.widget.RemoteViews;

import com.samsung.android.sdk.look.cocktailbar.SlookCocktailManager;
import com.samsung.android.sdk.look.cocktailbar.SlookCocktailProvider;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.util.ArrayList;
import java.util.List;

import nallapareddy.com.bookmarksedgepanel.R;
import nallapareddy.com.bookmarksedgepanel.activity.ConfigureActivity;
import nallapareddy.com.bookmarksedgepanel.model.Bookmark;
import nallapareddy.com.bookmarksedgepanel.model.BookmarkModel;
import nallapareddy.com.bookmarksedgepanel.model.IBookmarkModel;
import nallapareddy.com.bookmarksedgepanel.utils.ViewUtils;

public class BrowserEdgePlusReceiver extends SlookCocktailProvider {

    private static final String BOOKMARK_CLICKED = "bookmark_clicked";

    private int[] imageViewId = {R.id.edge_row_favicon_1, R.id.edge_row_favicon_2, R.id.edge_row_favicon_3, R.id.edge_row_favicon_4, R.id.edge_row_favicon_5, R.id.edge_row_favicon_6};
    private int[] textViewId = {R.id.edge_row_uri_1, R.id.edge_row_uri_2, R.id.edge_row_uri_3, R.id.edge_row_uri_4, R.id.edge_row_uri_5, R.id.edge_row_uri_6};
    private int[] bookmarkId = {R.id.bookmark_1, R.id.bookmark_2, R.id.bookmark_3, R.id.bookmark_4, R.id.bookmark_5, R.id.bookmark_6};
    private RemoteViews remoteViews;
    private int networkCalls;
    private int completedCalls;
    private List<Target> targets = new ArrayList<>();

    @Override
    public void onUpdate(Context context, SlookCocktailManager cocktailManager, int[] cocktailIds) {
        update(context, cocktailIds);
        for (int i = 0; i < cocktailIds.length; i++) {
            cocktailManager.updateCocktail(cocktailIds[i], remoteViews);
        }
    }

    @Override
    public void onVisibilityChanged(Context context, int cocktailId, int visibility) {
        update(context, new int[]{cocktailId});
        if (visibility == SlookCocktailManager.COCKTAIL_VISIBILITY_SHOW) {
            if (networkCalls == completedCalls) {
                SlookCocktailManager.getInstance(context).updateCocktail(cocktailId, remoteViews);
            }
        }

    }

    private void update(final Context context, int[] cocktailIds) {
        remoteViews = new RemoteViews(context.getPackageName(), R.layout.layout_edge_panel);
        IBookmarkModel<Bookmark> model = new BookmarkModel(context.getApplicationContext());
        for (int i = 0; i < imageViewId.length; i++) {
            //reset the image view so nothing shows if favicon is not there
            remoteViews.setImageViewResource(imageViewId[i], android.R.color.transparent);
            if (i < model.size()) {
                Bookmark currentBookmark = model.getBookmark(i);
                remoteViews.setTextViewText(textViewId[i], currentBookmark.getShortUrl());
                if (currentBookmark.useFavicon()) {
                    networkCalls++;
                    SlookTarget target = new SlookTarget(imageViewId[i], context, cocktailIds);
                    targets.add(target);
                    Picasso.with(context).setLoggingEnabled(true);
                    Picasso.with(context).load(currentBookmark.getFaviconUrl())
                            .into(target);
                } else {
                    Drawable tileDrawable = ViewUtils.getTileDrawableEdge(context, currentBookmark.getTextOption(), currentBookmark.getColorId());
                    remoteViews.setImageViewBitmap(imageViewId[i], ViewUtils.drawableToBitmap(tileDrawable));
                }

            } else {
                remoteViews.setTextViewText(textViewId[i], context.getString(R.string.add_bookmark));
                remoteViews.setImageViewResource(imageViewId[i], R.drawable.ic_add_white);
            }
            remoteViews.setOnClickPendingIntent(bookmarkId[i], getPendingSelfIntent(context, BOOKMARK_CLICKED + "" + i));
        }
    }

    private PendingIntent getPendingSelfIntent(Context context, String action) {
        Intent intent = new Intent(context, getClass());
        intent.setAction(action);
        return PendingIntent.getBroadcast(context, 0, intent, 0);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);
        String action = intent.getAction();
        if (action.startsWith(BOOKMARK_CLICKED)) {
            IBookmarkModel<Bookmark> model = new BookmarkModel(context);
            int index = Integer.parseInt(action.replace(BOOKMARK_CLICKED, ""));
            if (index < model.size()) {
                Uri currentUri = model.getBookmark(index).getUri();
                if (!currentUri.toString().startsWith("http://") && !currentUri.toString().startsWith("https://")) {
                    currentUri = Uri.parse("http://" + currentUri.toString());
                }
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, currentUri);
                browserIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(browserIntent);
            } else {
                Intent addNewIntent = new Intent(context, ConfigureActivity.class);
                addNewIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                context.startActivity(addNewIntent);
            }
        }
    }

   public class SlookTarget implements Target {
       private int imageViewId;
       private Context context;
       private int[] cocktailId;

       public SlookTarget(int imageViewId, Context context, int[] cocktailId) {
           this.imageViewId = imageViewId;
           this.context = context;
           this.cocktailId = cocktailId;
       }

       @Override
       public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
           completedCalls++;
           remoteViews.setImageViewBitmap(imageViewId, bitmap);
           updateSlookIfNecessary();
       }

       @Override
       public void onBitmapFailed(Drawable errorDrawable) {
           completedCalls++;
           updateSlookIfNecessary();
       }

       @Override
       public void onPrepareLoad(Drawable placeHolderDrawable) {

       }

       private void updateSlookIfNecessary() {
           if (completedCalls == networkCalls) {

               for (int i = 0; i < cocktailId.length; i++) {
                   SlookCocktailManager.getInstance(context)
                           .updateCocktail(cocktailId[i], remoteViews);
               }
               networkCalls = 0;
               completedCalls = 0;
           }
       }
   }
}
