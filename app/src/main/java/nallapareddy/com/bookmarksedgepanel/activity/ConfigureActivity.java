package nallapareddy.com.bookmarksedgepanel.activity;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ListView;

import com.crashlytics.android.Crashlytics;
import com.crashlytics.android.answers.Answers;
import com.crashlytics.android.answers.CustomEvent;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.samsung.android.sdk.look.Slook;

import org.parceler.Parcels;

import butterknife.BindView;
import butterknife.ButterKnife;
import nallapareddy.com.bookmarksedgepanel.R;
import nallapareddy.com.bookmarksedgepanel.adapters.BookmarksAdapter;
import nallapareddy.com.bookmarksedgepanel.dialogs.AddNewBookmarkDialog;
import nallapareddy.com.bookmarksedgepanel.model.Bookmark;
import nallapareddy.com.bookmarksedgepanel.model.BookmarkModel;
import nallapareddy.com.bookmarksedgepanel.model.IBookmarkModel;
import nallapareddy.com.bookmarksedgepanel.tasks.UrlDetailedTask;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;


public class ConfigureActivity extends AppCompatActivity implements AddNewBookmarkDialog.onNewBookmarkAddedListener, UrlDetailedTask.onUrlDetailedTaskFinished {

    private final int REQUEST_CODE = 1729;

    private final String AD_CODE = "ca-app-pub-3135803015555141~4955511510";

    static final String EXTRA_BOOKMARK = "extra_bookmark";
    static final String EXTRA_POSITION = "extra_position";

    private IBookmarkModel<Bookmark> model;
    private BookmarksAdapter bookmarksAdapter;

    @BindView(R.id.bookmarks_listview)
    ListView bookmarksList;
    @BindView(R.id.add_bookmark)
    FloatingActionButton fab;

    private boolean deleteMode;
    private ActionMode actionMode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_configure);
        ButterKnife.bind(this);
        initializeAds();
        model = new BookmarkModel(getApplicationContext());
        bookmarksAdapter = new BookmarksAdapter(this, model.getBookmarks());
        bookmarksList.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);
        bookmarksList.setAdapter(bookmarksAdapter);
        setupBookmarkListener();
        updateUrlInformation();
        setupFab();

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

    private void setupBookmarkListener() {
        bookmarksList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                Bookmark bookmark = model.getBookmark(position);
                Answers.getInstance().logCustom(new CustomEvent("Edit Bookmark")
                        .putCustomAttribute("Bookmark", bookmark.getUri().toString()));
                Intent intent = new Intent(ConfigureActivity.this, EditBookmarkActivity.class);
                intent.putExtra(EXTRA_BOOKMARK, Parcels.wrap(bookmark));
                intent.putExtra(EXTRA_POSITION, position);
                startActivityForResult(intent, REQUEST_CODE);
            }
        });/**/

        bookmarksList.setMultiChoiceModeListener(new AbsListView.MultiChoiceModeListener() {
            @Override
            public void onItemCheckedStateChanged(ActionMode actionMode, int position, long l, boolean b) {
                bookmarksAdapter.toggleSelection(position);
                if (bookmarksAdapter.getSelection().size() == 0) {
                    deleteMode = false;
                    setupFab();
                }
                setupFab();
            }

            @Override
            public boolean onCreateActionMode(ActionMode actionMode, Menu menu) {
                deleteMode = true;
                bookmarksAdapter.setSelectionMode(true);
                ConfigureActivity.this.actionMode = actionMode;
                return true;
            }

            @Override
            public boolean onPrepareActionMode(ActionMode actionMode, Menu menu) {
                return false;
            }

            @Override
            public boolean onActionItemClicked(ActionMode actionMode, MenuItem menuItem) {
                return false;
            }

            @Override
            public void onDestroyActionMode(ActionMode actionMode) {
                bookmarksAdapter.resetSelection();
                ConfigureActivity.this.actionMode = null;
                deleteMode = false;
                setupFab();
            }
        });
    }

    private void setupFab() {
        fab.setImageResource(deleteMode ? R.drawable.ic_delete_white : R.drawable.ic_add_pure_white);
        boolean enabled = model.size() < BookmarkModel.LIMIT || deleteMode;
        int color = enabled ? R.color.colorAccent : R.color.gray;
        fab.setBackgroundTintList(getResources().getColorStateList(color, getTheme()));
        fab.setEnabled(enabled);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (deleteMode) {
                    deleteBookmarks();
                } else {
                    openAddDialog();
                }

            }
        });
    }

    private void openAddDialog() {
        android.app.FragmentManager supportFragmentManager = getFragmentManager();
        AddNewBookmarkDialog newBookmarkDialog = new AddNewBookmarkDialog();
        newBookmarkDialog.show(supportFragmentManager, AddNewBookmarkDialog.TAG);
    }

    private void deleteBookmarks() {
        SparseBooleanArray selection = bookmarksAdapter.getSelection();
        for (int i = model.size() - 1; i >= 0; i--) {
            if (selection.get(i)) {
                Bookmark bookmark = model.getBookmark(i);
                bookmark.setCanceled(true);
                Answers.getInstance().logCustom(new CustomEvent("Delete Bookmark")
                        .putCustomAttribute("Bookmark", bookmark.getUri().toString()));
                try {
                    deleteFile(bookmark.getFileSafe());
                } catch (Exception e) {
                    Crashlytics.logException(e);
                }

                model.removeBookmark(i);
            }
        }
        actionMode.finish();
        model.save();
    }



    @Override
    public void newBookmarkAdded(String uri) {
        Uri newBookmark = Uri.parse(uri);
        if (newBookmark != null) {
            model.addBookmark(new Bookmark(newBookmark));
        }
        bookmarksAdapter.notifyDataSetChanged();
        setupFab();
        updateUrlInformation();
        model.save();
        Answers.getInstance().logCustom(new CustomEvent("New Bookmark")
                .putCustomAttribute("Bookmark", uri));
    }

    @Override
    protected void onStop() {
        super.onStop();
        model.save();
    }


    private void updateUrlInformation() {
        for (Bookmark bookmark : model.getBookmarks()) {
            if (!bookmark.isFullInfo()) {
                new UrlDetailedTask(bookmark, this).execute(bookmark.getUri());
            }
        }
    }

    @Override
    public void retryDetailedTask(Bookmark bookmark) {
        new UrlDetailedTask(bookmark, this).execute(bookmark.getUri());
    }

    @Override
    public void finishedTask() {
        bookmarksAdapter.notifyDataSetChanged();
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
            bookmarksAdapter.notifyDataSetChanged();
            model.save();
        }
    }

    private void initializeAds() {
        MobileAds.initialize(getApplicationContext(), AD_CODE);
        AdView adView = findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        adView.loadAd(adRequest);
    }
}
