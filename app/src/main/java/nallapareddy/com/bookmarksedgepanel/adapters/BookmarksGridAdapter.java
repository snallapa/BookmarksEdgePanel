package nallapareddy.com.bookmarksedgepanel.adapters;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.crashlytics.FirebaseCrashlytics;
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
    private final IBookmarkModel<Bookmark> model;
    private final OnGridItemClickListener listener;
    private final int gridLimit;
    private boolean editMode;

    public BookmarksGridAdapter(IBookmarkModel<Bookmark> model, int rows, int cols, OnGridItemClickListener listener) {
        this.model = model;
        this.listener = listener;
        this.gridLimit = rows * cols;
        this.editMode = false;
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
        int row = i / 2;
        int col = i % 2;
        return new Position(row, col);
    }

    @Override
    public void onBindViewHolder(@NonNull final ItemViewHolder viewHolder, int i) {
        final Bookmark currentBookmark = model.getBookmark(convertToPosition(i));
        viewHolder.icon.setTranslationZ(editMode ? 25 : 0);

        if (currentBookmark == null) {
            viewHolder.icon.setImageResource(R.drawable.ic_add_pure_black);
            viewHolder.link.setText(R.string.add_bookmark);
            viewHolder.deleteIcon.setVisibility(View.GONE);
            return;
        }
        viewHolder.deleteIcon.setVisibility(editMode ? View.VISIBLE : View.GONE);


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
                            FirebaseCrashlytics.getInstance().recordException(e);
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onBitmapFailed(Exception e, Drawable errorDrawable) {
                        Bundle bundle = new Bundle();
                        bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, currentBookmark.getUri().toString());
                        bundle.putString(FirebaseAnalytics.Param.CONTENT, currentBookmark.getFaviconUrl());
                        FirebaseAnalytics firebaseAnalytics = FirebaseAnalytics.getInstance(context);
                        firebaseAnalytics.logEvent(FirebaseAnalytics.Event.REFUND, bundle);
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

    public void toggleEdit() {
        this.editMode = !this.editMode;

        notifyDataSetChanged();
    }

    public boolean inEditMode() {
        return editMode;
    }

    @Override
    public boolean onItemMove(int fromPosition, int toPosition) {
        if (fromPosition < toPosition) {
            for (int i = fromPosition; i < toPosition; i++) {
                Bookmark from = model.getBookmark(convertToPosition(i));
                Bookmark to = model.getBookmark(convertToPosition(i+1));
                model.setBookmark(convertToPosition(i), to);
                model.setBookmark(convertToPosition(i+1), from);
            }
        } else {
            for (int i = fromPosition; i > toPosition; i--) {
                Bookmark from = model.getBookmark(convertToPosition(i));
                Bookmark to = model.getBookmark(convertToPosition(i-1));
                model.setBookmark(convertToPosition(i), to);
                model.setBookmark(convertToPosition(i-1), from);
            }
        }
        return true;
    }

    public class ItemViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnTouchListener {
        @BindView(R.id.bookmark_item_icon)
        ImageView icon;
        @BindView(R.id.bookmark_item_name)
        TextView link;
        @BindView(R.id.bookmark_item_container)
        View container;
        @BindView(R.id.bookmark_item_delete)
        ImageView deleteIcon;

        ItemViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            deleteIcon.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    int adapterPosition = getAdapterPosition();
                    listener.onItemDeleted(convertToPosition(adapterPosition));
                }
            });
            container.setOnClickListener(this);
            container.setOnTouchListener(this);
        }

        @Override
        public void onClick(View view) {
            int adapterPosition = getAdapterPosition();
            listener.onItemClicked(convertToPosition(adapterPosition));
        }

        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            if (motionEvent.getActionMasked() ==
                    MotionEvent.ACTION_DOWN && editMode) {
                listener.onItemTouched(this);
            }
            return false;
        }
    }

    public interface OnGridItemClickListener {
        void onItemClicked(Position pos);

        void onItemDeleted(Position pos);

        void onItemTouched(RecyclerView.ViewHolder vh);
    }
}
