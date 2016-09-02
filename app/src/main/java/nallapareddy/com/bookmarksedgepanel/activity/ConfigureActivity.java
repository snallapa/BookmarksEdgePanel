package nallapareddy.com.bookmarksedgepanel.activity;

import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.SparseBooleanArray;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.AbsListView;
import android.widget.ListView;

import com.samsung.android.sdk.look.Slook;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import nallapareddy.com.bookmarksedgepanel.R;
import nallapareddy.com.bookmarksedgepanel.adapters.BookmarksAdapter;
import nallapareddy.com.bookmarksedgepanel.data.Bookmark;
import nallapareddy.com.bookmarksedgepanel.dialogs.AddNewBookmarkDialog;
import nallapareddy.com.bookmarksedgepanel.tasks.UrlDetailedTask;
import nallapareddy.com.bookmarksedgepanel.utils.PreferenceUtils;


public class ConfigureActivity extends AppCompatActivity implements AddNewBookmarkDialog.onNewBookmarkAddedListener, UrlDetailedTask.RetryDetailedTask {

    private List<Bookmark> bookmarks = new ArrayList<>();
    private BookmarksAdapter bookmarksAdapter;

    @BindView(R.id.bookmarks_listview)
    ListView bookmarksList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_configure);
        ButterKnife.bind(this);
        bookmarks = PreferenceUtils.getBookmarks(this);
        bookmarksAdapter = new BookmarksAdapter(this, bookmarks);
        bookmarksList.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);
        bookmarksList.setAdapter(bookmarksAdapter);

        bookmarksList.setMultiChoiceModeListener(new AbsListView.MultiChoiceModeListener() {
            @Override
            public void onItemCheckedStateChanged(ActionMode actionMode, int position, long l, boolean b) {
                bookmarksAdapter.toggleSelection(position);
            }

            @Override
            public boolean onCreateActionMode(ActionMode actionMode, Menu menu) {
                getMenuInflater().inflate(R.menu.action_bar_multi_choice, menu);
                bookmarksAdapter.setSelectionMode(true);
                return true;
            }

            @Override
            public boolean onPrepareActionMode(ActionMode actionMode, Menu menu) {
                return false;
            }

            @Override
            public boolean onActionItemClicked(ActionMode actionMode, MenuItem menuItem) {
                switch (menuItem.getItemId()) {
                    case R.id.action_delete_bookmark:
                        SparseBooleanArray selection = bookmarksAdapter.getSelection();
                        for (int i = bookmarks.size()-1; i >= 0;i--) {
                            if (selection.get(i)) {
                                bookmarks.get(i).setCanceled(true);
                                bookmarks.remove(i);
                            }
                        }
                        invalidateOptionsMenu();
                        actionMode.finish();
                        PreferenceUtils.saveBookmarks(getApplicationContext(), bookmarks);
                }
                return false;
            }

            @Override
            public void onDestroyActionMode(ActionMode actionMode) {
                bookmarksAdapter.resetSelection();
            }
        });
        updateUrlInformation();

        Slook slook = new Slook();
        try {
            slook.initialize(this);
        } catch (Exception e) {

        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.action_bar_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_add_bookmark:
                android.app.FragmentManager supportFragmentManager = getFragmentManager();
                AddNewBookmarkDialog newBookmarkDialog = new AddNewBookmarkDialog();
                newBookmarkDialog.show(supportFragmentManager, AddNewBookmarkDialog.TAG);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void newBookmarkAdded(String uri) {
        Uri newBookmark = Uri.parse(uri);
        if (newBookmark != null) {
            bookmarks.add(new Bookmark(newBookmark));
        }
        bookmarksAdapter.notifyDataSetChanged();
        invalidateOptionsMenu();
        updateUrlInformation();
        PreferenceUtils.saveBookmarks(this, bookmarks);
    }

    @Override
    protected void onStop() {
        super.onStop();
        PreferenceUtils.saveBookmarks(this, bookmarks);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        if (bookmarks.size() > 5) {
            menu.findItem(R.id.action_add_bookmark).setEnabled(false);
        }
        return true;
    }

    private void updateUrlInformation() {
        for (Bookmark bookmark : bookmarks) {
            if (!bookmark.isFullInfo()) {
                new UrlDetailedTask(bookmarksAdapter, bookmark, false, this).execute(bookmark.getUri());
            }
        }
    }

    @Override
    public void retryDetailedTask(Bookmark bookmark) {
        new UrlDetailedTask(bookmarksAdapter, bookmark, bookmark.isTryHttp(), this).execute(bookmark.getUri());
    }
}
