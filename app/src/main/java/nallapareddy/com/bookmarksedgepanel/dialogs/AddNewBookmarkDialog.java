package nallapareddy.com.bookmarksedgepanel.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.Toast;

import nallapareddy.com.bookmarksedgepanel.R;
import nallapareddy.com.bookmarksedgepanel.model.Position;

public class AddNewBookmarkDialog extends DialogFragment {
    public static String TAG = "bookmark_dialog_tag";

    private EditText editText;
    private Position pos;

    public static AddNewBookmarkDialog newInstance(Position pos) {
        AddNewBookmarkDialog f = new AddNewBookmarkDialog();

        // Supply num input as an argument.
        Bundle args = new Bundle();
        args.putInt("row", pos.getRow());
        args.putInt("col", pos.getCol());
        f.setArguments(args);

        return f;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        int row = getArguments().getInt("row");
        int col = getArguments().getInt("col");
        this.pos = new Position(row, col);
        builder.setTitle(R.string.add_bookmark_title);
        builder.setNegativeButton(android.R.string.cancel, null);
        builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                commit();
            }
        });
        View view = LayoutInflater.from(getContext()).inflate(R.layout.dialog_add_bookmark, null);
        editText = view.findViewById(R.id.add_bookmark_edittext);
        builder.setView(view);
        AlertDialog alertDialog = builder.create();
        alertDialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
        alertDialog.show();
        return alertDialog;
    }

    public interface onNewBookmarkAddedListener {
        void newBookmarkAdded(String uri, Position pos);
    }

    private void commit() {
        String urlText = editText.getText().toString();
        if (!urlText.replace("www.", "").contains(".")) {
            Toast.makeText(getContext(), R.string.error_url, Toast.LENGTH_SHORT).show();
            return;
        }

        if (getActivity() instanceof onNewBookmarkAddedListener) {
            ((onNewBookmarkAddedListener) getActivity()).newBookmarkAdded(urlText, this.pos);
        } else {
            Log.e("AddNewBookmarkDialog", "Activity does not implement bookmark added listener");
        }
    }
}
