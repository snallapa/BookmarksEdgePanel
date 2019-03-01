package nallapareddy.com.bookmarksedgepanel.activity;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.samsung.android.sdk.look.Slook;

import org.parceler.Parcels;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import nallapareddy.com.bookmarksedgepanel.R;
import nallapareddy.com.bookmarksedgepanel.adapters.BookmarksGridAdapter;
import nallapareddy.com.bookmarksedgepanel.adapters.GridItemTouchHelperCallback;
import nallapareddy.com.bookmarksedgepanel.dialogs.AddNewBookmarkDialog;
import nallapareddy.com.bookmarksedgepanel.model.Bookmark;
import nallapareddy.com.bookmarksedgepanel.model.BookmarkModel;
import nallapareddy.com.bookmarksedgepanel.model.IBookmarkModel;
import nallapareddy.com.bookmarksedgepanel.tasks.UrlDetailedTask;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;


public class ConfigureActivity extends AppCompatActivity implements AddNewBookmarkDialog.onNewBookmarkAddedListener, UrlDetailedTask.onUrlDetailedTaskFinished, BookmarksGridAdapter.OnGridItemClickListener {

    private final int REQUEST_CODE = 1729;

    private final String AD_CODE = "ca-app-pub-3135803015555141~4955511510";

    static final String EXTRA_BOOKMARK = "extra_bookmark";
    static final String EXTRA_POSITION = "extra_position";

    private IBookmarkModel<Bookmark> model;
    private BookmarksGridAdapter gridAdapter;

    @BindView(R.id.bookmarks_grid_view)
    RecyclerView bookmarksGrid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_configure);
        ButterKnife.bind(this);
        initializeAds();
        model = new BookmarkModel(getApplicationContext());
        setupRecyclerView();
        updateUrlInformation();

        Slook slook = new Slook();
        try {
            slook.initialize(this);
        } catch (Exception e) {
            Log.e("Configure", "SLOOK not initialized");
        }
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(base));
    }

    private void setupRecyclerView() {
        bookmarksGrid.setHasFixedSize(true);
        GridLayoutManager gridLayoutManager = new GridLayoutManager(this, 2, LinearLayoutManager.VERTICAL, false);
        bookmarksGrid.setLayoutManager(gridLayoutManager);
        gridAdapter = new BookmarksGridAdapter(model, BookmarkModel.LIMIT, this);
        bookmarksGrid.setAdapter(gridAdapter);
        setupReordering();
    }

    private void setupReordering() {
        ItemTouchHelper.Callback callback = new GridItemTouchHelperCallback(gridAdapter);
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(callback);
        itemTouchHelper.attachToRecyclerView(bookmarksGrid);
    }

    private void openAddDialog(int positionOpened) {
        android.app.FragmentManager supportFragmentManager = getFragmentManager();
        AddNewBookmarkDialog newBookmarkDialog = AddNewBookmarkDialog.newInstance(positionOpened);
        newBookmarkDialog.show(supportFragmentManager, AddNewBookmarkDialog.TAG);
    }

    private void openEditActivity(int position) {
        Bookmark bookmark = model.getBookmark(position);
//                Answers.getInstance().logCustom(new CustomEvent("Edit Bookmark")
//                        .putCustomAttribute("Bookmark", bookmark.getUri().toString()));
        Intent intent = new Intent(ConfigureActivity.this, EditBookmarkActivity.class);
        intent.putExtra(EXTRA_BOOKMARK, Parcels.wrap(bookmark));
        intent.putExtra(EXTRA_POSITION, position);
        startActivityForResult(intent, REQUEST_CODE);

    }

    private void deleteBookmarks() {
//        SparseBooleanArray selection = bookmarksAdapter.getSelection();
//        for (int i = model.size() - 1; i >= 0; i--) {
//            if (selection.get(i)) {
//                Bookmark bookmark = model.getBookmark(i);
//                bookmark.setCanceled(true);
////                Answers.getInstance().logCustom(new CustomEvent("Delete Bookmark")
////                        .putCustomAttribute("Bookmark", bookmark.getUri().toString()));
//                try {
//                    deleteFile(bookmark.getFileSafe());
//                } catch (Exception e) {
//                    Crashlytics.logException(e);
//                }
//
//                model.removeBookmark(i);
//            }
//        }
//        actionMode.finish();
//        model.save();
    }


    @Override
    public void newBookmarkAdded(String uri, int position) {
        Uri newBookmark = Uri.parse(uri);
        if (newBookmark != null) {
            model.addBookmark(new Bookmark(newBookmark, position));
        }
        gridAdapter.notifyTranslatedItem(model.size() - 1);
        updateUrlInformation();
        model.save();
//        Answers.getInstance().logCustom(new CustomEvent("New Bookmark")
//                .putCustomAttribute("Bookmark", uri));
    }

    @Override
    protected void onStop() {
        super.onStop();
        model.save();
    }


    private void updateUrlInformation() {
        List<Bookmark> bookmarks = model.getBookmarks();
        for (int i = 0; i < bookmarks.size(); i++) {
            Bookmark bookmark = bookmarks.get(i);
            if (!bookmark.isFullInfo()) {
                new UrlDetailedTask(bookmark, i, this).execute(bookmark.getUri());
            }
        }
    }

    @Override
    public void retryDetailedTask(Bookmark bookmark, int position) {
        new UrlDetailedTask(bookmark, position, this).execute(bookmark.getUri());
    }

    @Override
    public void finishedTask(int position) {
        gridAdapter.notifyTranslatedItem(position);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode != REQUEST_CODE) {
            return;
        }
        if (resultCode == RESULT_OK) {
            int position = data.getIntExtra(EXTRA_POSITION, -1);
            Bookmark currentBookmark = Parcels.unwrap(data.getParcelableExtra(EXTRA_BOOKMARK));
            model.updateBookmark(position, currentBookmark);
            gridAdapter.notifyTranslatedItem(position);
            model.save();
        }
    }

    private void initializeAds() {
        MobileAds.initialize(getApplicationContext(), AD_CODE);
        AdView adView = findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        adView.loadAd(adRequest);
    }

    @Override
    public void onItemClicked(int edgePosition) {
        int position = model.getBookmarkForEdgePosition(edgePosition);
        if (position == -1) {
            openAddDialog(edgePosition);
        } else {
            openEditActivity(position);
        }
    }
}
