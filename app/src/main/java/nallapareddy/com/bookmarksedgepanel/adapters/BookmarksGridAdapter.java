package nallapareddy.com.bookmarksedgepanel.adapters;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.crashlytics.android.Crashlytics;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;

import butterknife.BindView;
import butterknife.ButterKnife;
import nallapareddy.com.bookmarksedgepanel.R;
import nallapareddy.com.bookmarksedgepanel.model.Bookmark;
import nallapareddy.com.bookmarksedgepanel.model.IBookmarkModel;
import nallapareddy.com.bookmarksedgepanel.model.Position;
import nallapareddy.com.bookmarksedgepanel.utils.ViewUtils;

public class BookmarksGridAdapter extends RecyclerView.Adapter<BookmarksGridAdapter.ItemViewHolder> implements GridItemTouchHelperCallback.ItemTouchHelperAdapter {
    private IBookmarkModel<Bookmark> model;
    private OnGridItemClickListener listener;
    private int gridLimit;

    public BookmarksGridAdapter(IBookmarkModel<Bookmark> model, int rows, int cols, OnGridItemClickListener listener) {
        this.model = model;
        this.listener = listener;
        this.gridLimit = rows * cols;
    }

    @NonNull
    @Override
    public ItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int i) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.bookmarks_grid_item, parent, false);
        return new ItemViewHolder(v);
    }

    public void notifyTranslatedItemChanged(Position pos) {
        notifyItemChanged(pos.getRow() * 2 + pos.getCol());
    }

    private Position convertToPosition(int i) {
        int row = i/2;
        int col = i % 2;
        return new Position(row, col);
    }

    @Override
    public void onBindViewHolder(@NonNull final ItemViewHolder viewHolder, int i) {
       final Bookmark currentBookmark = model.getBookmark(convertToPosition(i));
        if (currentBookmark == null) {
            viewHolder.icon.setImageResource(R.drawable.ic_add_pure_black);
            viewHolder.link.setText(R.string.add_bookmark);
            return;
        }
        final Context context = viewHolder.icon.getContext();
        final String url = currentBookmark.getUri().toString();
        viewHolder.link.setText(Html.fromHtml(String.format("<a href='%s'>%s</a>", url, url)));
        viewHolder.link.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, currentBookmark.getBrowserUri());
                context.startActivity(browserIntent);
            }
        });
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
                viewHolder.icon.setImageBitmap(b);
            } else {
                Target target = new Target() {
                    @Override
                    public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                        viewHolder.icon.setImageBitmap(bitmap);
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
//                        Answers.getInstance().logCustom(new CustomEvent("Favicon Failed")
//                                .putCustomAttribute("Bookmark", currentBookmark.getUri().toString())
//                                .putCustomAttribute("Favicon Url", currentBookmark.getFaviconUrl()));
                        currentBookmark.setUseFavicon(false);
                        setTileDrawable(viewHolder, currentBookmark);
                    }

                    @Override
                    public void onPrepareLoad(Drawable placeHolderDrawable) {
                        viewHolder.icon.setImageDrawable(placeHolderDrawable);
                    }
                };
                Picasso.get().load(currentBookmark.getFaviconUrl())
                        .error(R.drawable.ic_error_outline_black)
                        .placeholder(R.drawable.ic_prepare)
                        .into(target);
            }

        } else {
            setTileDrawable(viewHolder, currentBookmark);
        }
    }

    private void setTileDrawable(ItemViewHolder vh, Bookmark currentBookmark) {
        Context context = vh.icon.getContext();
        Drawable tileDrawable = ViewUtils.getTileDrawable(context, currentBookmark.getTileText(), currentBookmark.getColorId());
        vh.icon.setImageDrawable(tileDrawable);
    }

    @Override
    public int getItemCount() {
        return gridLimit;
    }



    @Override
    public boolean onItemMove(int fromPosition, int toPosition) {
        Bookmark from = model.getBookmark(convertToPosition(fromPosition));
        Bookmark to = model.getBookmark(convertToPosition(toPosition));
        model.setBookmark(convertToPosition(fromPosition), to);
        model.setBookmark(convertToPosition(toPosition), from);
        notifyItemMoved(fromPosition, toPosition);
        return true;
    }

    public class ItemViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        @BindView(R.id.bookmark_item_icon)
        ImageView icon;
        @BindView(R.id.bookmark_item_name)
        TextView link;
        @BindView(R.id.bookmark_item_container)
        View container;


        ItemViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            container.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            int adapterPosition = getAdapterPosition();
            listener.onItemClicked(convertToPosition(adapterPosition));

        }
    }

    public interface OnGridItemClickListener {
        void onItemClicked(Position pos);
    }
}
