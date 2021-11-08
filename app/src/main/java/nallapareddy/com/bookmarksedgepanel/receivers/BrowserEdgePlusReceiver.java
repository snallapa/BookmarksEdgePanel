package nallapareddy.com.bookmarksedgepanel.receivers;


import static android.app.PendingIntent.FLAG_IMMUTABLE;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.widget.RemoteViews;
import android.widget.Toast;

import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.crashlytics.FirebaseCrashlytics;
import com.samsung.android.sdk.look.cocktailbar.SlookCocktailManager;
import com.samsung.android.sdk.look.cocktailbar.SlookCocktailProvider;

import java.io.File;
import java.io.FileInputStream;

import nallapareddy.com.bookmarksedgepanel.R;
import nallapareddy.com.bookmarksedgepanel.activity.ConfigureActivity;
import nallapareddy.com.bookmarksedgepanel.model.Bookmark;
import nallapareddy.com.bookmarksedgepanel.model.BookmarkModel;
import nallapareddy.com.bookmarksedgepanel.model.GridDisplayType;
import nallapareddy.com.bookmarksedgepanel.model.IBookmarkModel;
import nallapareddy.com.bookmarksedgepanel.model.Position;
import nallapareddy.com.bookmarksedgepanel.utils.ViewUtils;

public class BrowserEdgePlusReceiver extends SlookCocktailProvider {

    private static final String BOOKMARK_CLICKED = "bookmark_clicked";
    private static int[][] imageViewId = {
            {R.id.edge_row_favicon_1, R.id.edge_row_favicon_2},
            {R.id.edge_row_favicon_3, R.id.edge_row_favicon_4},
            {R.id.edge_row_favicon_5, R.id.edge_row_favicon_6},
            {R.id.edge_row_favicon_7, R.id.edge_row_favicon_8},
            {R.id.edge_row_favicon_9, R.id.edge_row_favicon_10},
            {R.id.edge_row_favicon_11, R.id.edge_row_favicon_12}

    };
    private static int[][] textViewId = {
            {R.id.edge_row_uri_1, R.id.edge_row_uri_2},
            {R.id.edge_row_uri_3, R.id.edge_row_uri_4},
            {R.id.edge_row_uri_5, R.id.edge_row_uri_6},
            {R.id.edge_row_uri_7, R.id.edge_row_uri_8},
            { R.id.edge_row_uri_9, R.id.edge_row_uri_10},
            {R.id.edge_row_uri_11, R.id.edge_row_uri_12}
    };
    private static int[][] bookmarkId = {
            {R.id.bookmark_1, R.id.bookmark_2},
            {R.id.bookmark_3, R.id.bookmark_4},
            {R.id.bookmark_5, R.id.bookmark_6},
            {R.id.bookmark_7, R.id.bookmark_8},
            {R.id.bookmark_9, R.id.bookmark_10},
            {R.id.bookmark_11, R.id.bookmark_12}
    };

    private static int[] singleImageViewId = {R.id.single_edge_row_favicon_1, R.id.single_edge_row_favicon_2, R.id.single_edge_row_favicon_3, R.id.single_edge_row_favicon_4, R.id.single_edge_row_favicon_5, R.id.single_edge_row_favicon_6};
    private static int[] singleTextViewId = {R.id.single_edge_row_uri_1, R.id.single_edge_row_uri_2, R.id.single_edge_row_uri_3, R.id.single_edge_row_uri_4, R.id.single_edge_row_uri_5, R.id.single_edge_row_uri_6};
    private static int[] singleBookmarkId = {R.id.single_bookmark_1, R.id.single_bookmark_2, R.id.single_bookmark_3, R.id.single_bookmark_4, R.id.single_bookmark_5, R.id.single_bookmark_6};

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

    private void getViewFromRemoteView(final Bookmark currentBookmark, final Context context, final int imageViewId, int textViewId, int bookmarkId, String action) {
        if (currentBookmark == null) {
            remoteViews.setTextViewText(textViewId, context.getString(R.string.add_bookmark));
            remoteViews.setImageViewResource(imageViewId, R.drawable.ic_add_white);
        } else {
            remoteViews.setImageViewResource(imageViewId, android.R.color.transparent);
            remoteViews.setTextViewText(textViewId, currentBookmark.getShortUrl());
            if (currentBookmark.useFavicon()) {
                try {
                    File fileStreamPath = context.getFileStreamPath(currentBookmark.getFileSafe());
                    if (fileStreamPath.exists()) {
                        FileInputStream fileInputStream = context.openFileInput(currentBookmark.getFileSafe());
                        Bitmap b = BitmapFactory.decodeStream(fileInputStream);
                        remoteViews.setImageViewBitmap(imageViewId, b);
                    } else {
                        remoteViews.setImageViewResource(imageViewId, R.drawable.ic_error_outline_black);
                    }
                } catch (Exception e) {
                    FirebaseCrashlytics.getInstance().recordException(e);
                }
            } else {
                Drawable tileDrawable = ViewUtils.getTileDrawableEdge(context, currentBookmark.getTileText(), currentBookmark.getColorId());
                remoteViews.setImageViewBitmap(imageViewId, ViewUtils.drawableToBitmap(tileDrawable));
            }
        }

        remoteViews.setOnClickPendingIntent(bookmarkId, getPendingSelfIntent(context, action));
    }


    private void update(Context context, int[] cocktailIds) {
        IBookmarkModel<Bookmark> model = new BookmarkModel(context.getApplicationContext());
        if (model.getDisplayType() == GridDisplayType.SINGLE_COLUMN) {
            remoteViews = new RemoteViews(context.getPackageName(), R.layout.layout_edge_panel_single);
            for (int i = 0; i < BookmarkModel.ROWS; i++) {
                int j = 0;
                final Bookmark currentBookmark = model.getBookmark(new Position(i,j));
                String pos = "(" + i + "," + j + ")";
                String action = BOOKMARK_CLICKED + "" + pos;
                getViewFromRemoteView(currentBookmark, context, singleImageViewId[i], singleTextViewId[i], singleBookmarkId[i], action);
            }
        } else {
            remoteViews = new RemoteViews(context.getPackageName(), R.layout.layout_edge_panel);
            for (int i = 0; i < BookmarkModel.ROWS; i++) {
                for (int j = 0; j < BookmarkModel.COLUMNS;j++) {
                    final Bookmark currentBookmark = model.getBookmark(new Position(i,j));
                    String pos = "(" + i + "," + j + ")";
                    String action = BOOKMARK_CLICKED + "" + pos;
                    getViewFromRemoteView(currentBookmark, context, imageViewId[i][j], textViewId[i][j], bookmarkId[i][j], action);
                }
            }
        }

    }

    private PendingIntent getPendingSelfIntent(Context context, String action) {
        Intent intent = new Intent(context, getClass());
        intent.setAction(action);
        return PendingIntent.getBroadcast(context, 0, intent, FLAG_IMMUTABLE);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);
        String action = intent.getAction();
        if (action.startsWith(BOOKMARK_CLICKED)) {
            IBookmarkModel<Bookmark> model = new BookmarkModel(context);
            action = action.replace(BOOKMARK_CLICKED, "");
            Position pos = Position.fromString(action);
            Bookmark bookmark = model.getBookmark(pos);
            if (bookmark != null) {
                FirebaseAnalytics firebaseAnalytics = FirebaseAnalytics.getInstance(context);
                Bundle bundle = new Bundle();
                bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, bookmark.getUri().toString());
                firebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_ITEM, bundle);
                Uri currentUri = bookmark.getBrowserUri();
                try {
                    Intent browserIntent = new Intent(Intent.ACTION_VIEW, currentUri);
                    browserIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    context.startActivity(browserIntent);
                } catch (Exception e) {
                    FirebaseCrashlytics.getInstance().recordException(e);
                    Toast.makeText(context, R.string.open_error, Toast.LENGTH_LONG).show();
                }

            } else {
                Intent addNewIntent = new Intent(context, ConfigureActivity.class);
                addNewIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                context.startActivity(addNewIntent);
            }
        }
    }

}
