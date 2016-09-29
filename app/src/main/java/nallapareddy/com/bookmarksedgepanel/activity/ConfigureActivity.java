package nallapareddy.com.bookmarksedgepanel.activity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.SparseBooleanArray;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ListView;

import com.samsung.android.sdk.look.Slook;

import org.parceler.Parcels;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import nallapareddy.com.bookmarksedgepanel.R;
import nallapareddy.com.bookmarksedgepanel.adapters.BookmarksAdapter;
import nallapareddy.com.bookmarksedgepanel.model.Bookmark;
import nallapareddy.com.bookmarksedgepanel.dialogs.AddNewBookmarkDialog;
import nallapareddy.com.bookmarksedgepanel.tasks.UrlDetailedTask;
import nallapareddy.com.bookmarksedgepanel.utils.ModelUtils;


public class ConfigureActivity extends AppCompatActivity implements AddNewBookmarkDialog.onNewBookmarkAddedListener, UrlDetailedTask.onUrlDetailedTaskFinished {

    private final int REQUEST_CODE = 1729;

    static final String EXTRA_BOOKMARK = "extra_bookmark";
    static final String EXTRA_POSITION = "extra_position";

    private List<Bookmark> bookmarks = new ArrayList<>();
    private BookmarksAdapter bookmarksAdapter;

    @BindView(R.id.bookmarks_listview)
    ListView bookmarksList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_configure);
        ButterKnife.bind(this);
        ModelUtils.convertPreferences(getApplicationContext());
        bookmarks = ModelUtils.readItems(this);
        bookmarksAdapter = new BookmarksAdapter(this, bookmarks);
        bookmarksList.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);
        bookmarksList.setAdapter(bookmarksAdapter);

        bookmarksList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                Intent intent = new Intent(ConfigureActivity.this, EditBookmarkActivity.class);
                intent.putExtra(EXTRA_BOOKMARK, Parcels.wrap(bookmarks.get(position)));
                intent.putExtra(EXTRA_POSITION, position);
                startActivityForResult(intent, REQUEST_CODE);
            }
        });

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
                        ModelUtils.writeItems(getApplicationContext(), bookmarks);
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
        ModelUtils.writeItems(getApplicationContext(), bookmarks);
    }

    @Override
    protected void onStop() {
        super.onStop();
        ModelUtils.writeItems(getApplicationContext(), bookmarks);
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
            bookmarks.set(position, currentBookmark);
            bookmarksAdapter.notifyDataSetChanged();
            ModelUtils.writeItems(getApplicationContext(), bookmarks);
        }
    }
}
