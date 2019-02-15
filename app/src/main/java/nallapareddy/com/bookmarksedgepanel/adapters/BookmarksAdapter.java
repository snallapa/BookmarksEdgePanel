package nallapareddy.com.bookmarksedgepanel.adapters;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.text.Html;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import com.crashlytics.android.Crashlytics;
import com.crashlytics.android.answers.Answers;
import com.crashlytics.android.answers.CustomEvent;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import nallapareddy.com.bookmarksedgepanel.R;
import nallapareddy.com.bookmarksedgepanel.model.Bookmark;
import nallapareddy.com.bookmarksedgepanel.utils.ViewUtils;


public class BookmarksAdapter extends ArrayAdapter<Bookmark> {

    private SparseBooleanArray selected = new SparseBooleanArray();
    private boolean selectionMode;

    public BookmarksAdapter(Context context, List<Bookmark> bookmarks) {
        super(context, R.layout.bookmarks_list_row, bookmarks);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final ViewHolder viewHolder;
        final Context context = getContext();
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.bookmarks_list_row, parent, false);
            viewHolder = new ViewHolder(convertView);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        final Bookmark currentBookmark = getItem(position);
        final String url = currentBookmark.getUri().toString();
        viewHolder.bookmarkUri.setText(Html.fromHtml(String.format("<a href='%s'>%s</a>", url, url)));
        viewHolder.bookmarkUri.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, currentBookmark.getBrowserUri());
                context.startActivity(browserIntent);
            }
        });
        viewHolder.bookmarkTitle.setVisibility(currentBookmark.isFullInfo() ? View.VISIBLE : View.GONE);
        viewHolder.bookmarkTitle.setText(currentBookmark.getTitle() == null ? "" : currentBookmark.getSafeTitle());
        viewHolder.checkBox.setVisibility(selectionMode ? View.VISIBLE : View.GONE);
        viewHolder.checkBox.setChecked(selected.get(position));
        if (currentBookmark.useFavicon()) {
            File fileStreamPath = context.getFileStreamPath(currentBookmark.getFileSafe());
            if (fileStreamPath.exists()) {
                FileInputStream fileInputStream = null;
                try {
                    fileInputStream = context.openFileInput(currentBookmark.getFileSafe());
                } catch (FileNotFoundException e) {
                    currentBookmark.setUseFavicon(false);
                    setTileDrawable(viewHolder, currentBookmark);
                    e.printStackTrace();
                }
                Bitmap b = BitmapFactory.decodeStream(fileInputStream);
                viewHolder.bookmarkFavicon.setImageBitmap(b);
            } else {
                Target target = new Target() {
                    @Override
                    public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                        viewHolder.bookmarkFavicon.setImageBitmap(bitmap);
                        try {
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
                    public void onBitmapFailed(Exception e, Drawable errorDrawable) {
                        Answers.getInstance().logCustom(new CustomEvent("Favicon Failed")
                                .putCustomAttribute("Bookmark", currentBookmark.getUri().toString())
                                .putCustomAttribute("Favicon Url", currentBookmark.getFaviconUrl()));
                        currentBookmark.setUseFavicon(false);
                        setTileDrawable(viewHolder, currentBookmark);
                    }

                    @Override
                    public void onPrepareLoad(Drawable placeHolderDrawable) {
                        viewHolder.bookmarkFavicon.setImageDrawable(placeHolderDrawable);
                    }
                };
                Picasso.get().load(currentBookmark.getFaviconUrl())
                        .error(R.drawable.ic_error_outline_black)
                        .placeholder(R.drawable.ic_prepare)
                        .into(target);
                viewHolder.bookmarkFavicon.setTag(target);
            }

        } else {
            setTileDrawable(viewHolder, currentBookmark);
        }

        return convertView;
    }

    private void setTileDrawable(ViewHolder vh, Bookmark currentBookmark) {
        Context context = getContext();
        Drawable tileDrawable = ViewUtils.getTileDrawable(context, currentBookmark.getTileText(), currentBookmark.getColorId());
        vh.bookmarkFavicon.setImageDrawable(tileDrawable);
    }

    public void toggleSelection(int position) {
        if (!selectionMode) {
            return;
        }
        if (selected.get(position)) {
            selected.delete(position);
        } else {
            selected.put(position, true);
        }
        notifyDataSetChanged();
    }

    public void resetSelection() {
        selected = new SparseBooleanArray();
        selectionMode = false;
        notifyDataSetChanged();
    }

    public void setSelectionMode(boolean selectionMode) {
        this.selectionMode = selectionMode;
    }

    public SparseBooleanArray getSelection() {
        return selected;
    }

    static class ViewHolder {
        @BindView(R.id.list_row_checkbox)
        CheckBox checkBox;
        @BindView(R.id.list_row_bookmark_uri)
        TextView bookmarkUri;
        @BindView(R.id.list_row_bookmark_title)
        TextView bookmarkTitle;
        @BindView(R.id.list_row_favicon_image)
        ImageView bookmarkFavicon;

        public ViewHolder(View view) {
            ButterKnife.bind(this, view);
        }
    }
}
