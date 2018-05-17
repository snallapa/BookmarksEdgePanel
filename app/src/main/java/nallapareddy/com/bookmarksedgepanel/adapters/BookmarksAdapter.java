package nallapareddy.com.bookmarksedgepanel.adapters;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.text.Html;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

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
        ViewHolder viewHolder;
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.bookmarks_list_row, parent, false);
            viewHolder = new ViewHolder(convertView);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        Bookmark currentBookmark = getItem(position);
        final String url = currentBookmark.getUri().toString();
        viewHolder.bookmarkUri.setText(Html.fromHtml(String.format("<a href='%s'>%s</a>", url, url)));
        viewHolder.bookmarkUri.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://" + url));
                getContext().startActivity(browserIntent);
            }
        });
        viewHolder.bookmarkTitle.setVisibility(currentBookmark.isFullInfo() ? View.VISIBLE : View.GONE);
        viewHolder.bookmarkTitle.setText(currentBookmark.getTitle() == null ? "" : currentBookmark.getSafeTitle());
        viewHolder.checkBox.setVisibility(selectionMode ? View.VISIBLE : View.GONE);
        viewHolder.checkBox.setChecked(selected.get(position));
        if (currentBookmark.useFavicon()) {
            Picasso.with(getContext()).load(currentBookmark.getFaviconUrl()).error(R.drawable.ic_error_outline_black).into(viewHolder.bookmarkFavicon);
        } else {
            Drawable tileDrawable = ViewUtils.getTileDrawable(getContext(), currentBookmark.getTextOption(), currentBookmark.getColorId());
            viewHolder.bookmarkFavicon.setImageDrawable(tileDrawable);
        }

        return convertView;
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
