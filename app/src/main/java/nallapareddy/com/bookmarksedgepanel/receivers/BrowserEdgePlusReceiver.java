package nallapareddy.com.bookmarksedgepanel.receivers;


import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.widget.RemoteViews;

import com.crashlytics.android.Crashlytics;
import com.samsung.android.sdk.look.cocktailbar.SlookCocktailManager;
import com.samsung.android.sdk.look.cocktailbar.SlookCocktailProvider;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;

import nallapareddy.com.bookmarksedgepanel.R;
import nallapareddy.com.bookmarksedgepanel.activity.ConfigureActivity;
import nallapareddy.com.bookmarksedgepanel.model.Bookmark;
import nallapareddy.com.bookmarksedgepanel.model.BookmarkModel;
import nallapareddy.com.bookmarksedgepanel.model.IBookmarkModel;
import nallapareddy.com.bookmarksedgepanel.utils.ViewUtils;

public class BrowserEdgePlusReceiver extends SlookCocktailProvider {

    private static final String BOOKMARK_CLICKED = "bookmark_clicked";

    private int[] imageViewId = {R.id.edge_row_favicon_1, R.id.edge_row_favicon_2, R.id.edge_row_favicon_3, R.id.edge_row_favicon_4, R.id.edge_row_favicon_5, R.id.edge_row_favicon_6, R.id.edge_row_favicon_7, R.id.edge_row_favicon_8,     R.id.edge_row_favicon_9, R.id.edge_row_favicon_10, R.id.edge_row_favicon_11, R.id.edge_row_favicon_12};
    private int[] textViewId = {R.id.edge_row_uri_1, R.id.edge_row_uri_2, R.id.edge_row_uri_3, R.id.edge_row_uri_4, R.id.edge_row_uri_5, R.id.edge_row_uri_6, R.id.edge_row_uri_7, R.id.edge_row_uri_8, R.id.edge_row_uri_9, R.id.edge_row_uri_10, R.id.edge_row_uri_11, R.id.edge_row_uri_12};
    private int[] bookmarkId = {R.id.bookmark_1, R.id.bookmark_2, R.id.bookmark_3, R.id.bookmark_4, R.id.bookmark_5, R.id.bookmark_6, R.id.bookmark_7, R.id.bookmark_8, R.id.bookmark_9, R.id.bookmark_10, R.id.bookmark_11, R.id.bookmark_12};
    private RemoteViews remoteViews;

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
            SlookCocktailManager.getInstance(context).updateCocktail(cocktailId, remoteViews);
        }

    }

    private void update(final Context context, int[] cocktailIds) {
        remoteViews = new RemoteViews(context.getPackageName(), R.layout.layout_edge_panel);
        IBookmarkModel<Bookmark> model = new BookmarkModel(context.getApplicationContext());
        for (int i = 0; i < imageViewId.length; i++) {
            //reset the image view so nothing shows if favicon is not there
            remoteViews.setImageViewResource(imageViewId[i], android.R.color.transparent);
            if (i < model.size()) {
                final Bookmark currentBookmark = model.getBookmark(i);
                remoteViews.setTextViewText(textViewId[i], currentBookmark.getShortUrl());
                if (currentBookmark.useFavicon()) {
                    try {
                        File fileStreamPath = context.getFileStreamPath(currentBookmark.getFileSafe());
                        if (fileStreamPath.exists()) {
                            FileInputStream fileInputStream = context.openFileInput(currentBookmark.getFileSafe());
                            Bitmap b = BitmapFactory.decodeStream(fileInputStream);
                            remoteViews.setImageViewBitmap(imageViewId[i], b);
                        } else {
                            final int finalI = i;
                            Target target = new Target() {
                                @Override
                                public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                                    try {
                                        remoteViews.setImageViewBitmap(imageViewId[finalI], bitmap);
                                        String filename = currentBookmark.getFileSafe();
                                        File fileStreamPath = context.getFileStreamPath(filename);
                                        if (!fileStreamPath.exists()) {
                                            FileOutputStream outputStream = context.openFileOutput(filename, Context.MODE_PRIVATE);
                                            bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream);
                                            outputStream.close();
                                        }
                                    } catch (Exception e) {
                                        Crashlytics.logException(e);
                                        e.printStackTrace();
                                    }
                                }

                                @Override
                                public void onBitmapFailed(Drawable errorDrawable) {

                                }

                                @Override
                                public void onPrepareLoad(Drawable placeHolderDrawable) {

                                }
                            };
                            Picasso.with(context).load(currentBookmark.getFaviconUrl()).into(target);
                        }
                    } catch (Exception e) {
                        Crashlytics.logException(e);
                        e.printStackTrace();
                    }
                } else {
                    Drawable tileDrawable = ViewUtils.getTileDrawableEdge(context, currentBookmark.getTileText(), currentBookmark.getColorId());
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

}
