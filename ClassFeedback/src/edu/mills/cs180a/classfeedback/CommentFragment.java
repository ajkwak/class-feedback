package edu.mills.cs180a.classfeedback;

import android.app.AlertDialog;
import android.app.Fragment;
import android.content.ActivityNotFoundException;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

/**
 * A fragment that solicits a {@link Comment} for a specific {@link Person}. The user is given the
 * choice of saving, canceling, deleting, or mailing the comment. The user can also clear the
 * comment field with or without saving their changes.
 *
 * @author ellen.spertus@gmail.com (Ellen Spertus)
 * @author ajkwak@users.noreply.github.com (AJ Parmidge)
 * @author cyu@mills.edu (Ching Yu)
 */
public class CommentFragment extends Fragment {
    private static final String TAG = "DetailFragment";
    private MainActivity mActivity;
    private Person mRecipient;
    private EditText commentField;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_comment, container, false);
        mActivity = (MainActivity) getActivity();
        // Log.d(TAG, "mActivity = " + mActivity + ", commentField = " + commentField);
        return view;
    }

    /**
     * Sets the recipient whose comment is displayed in this {@code CommentFragment}.
     *
     * @param personId the ID of the recipient
     */
    void setRecipient(int personId) {
        mRecipient = Person.everyone[personId];

        // Show a picture of the recipient.
        ImageView icon = (ImageView) mActivity.findViewById(R.id.commentImageView);
        icon.setImageResource(mRecipient.getImageId());

        // Set the text of the comment EditText to the value of the current comment, if any.
        Comment comment = getCommentForRecipient(mRecipient.getEmail());
        commentField = (EditText) mActivity.findViewById(R.id.commentEditText);
        if (comment != null && comment.getContent() != null) {
            commentField.setText(comment.getContent());
        } else {
            commentField.setText("");
        }

        // Add listeners.
        Button saveButton = (Button) mActivity.findViewById(R.id.saveCommentButton);
        saveButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                saveCurrentComment();
            }
        });
        Button cancelButton = (Button) mActivity.findViewById(R.id.cancelCommentButton);
        cancelButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                String cancelMessage = getString(R.string.comment_canceled_toast, mRecipient);
                mActivity.hideCommentFragment(cancelMessage);
            }
        });
        Button clearTextButton = (Button) mActivity.findViewById(R.id.clearTextButton);
        clearTextButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {
                commentField.setText("");
            }
        });
        Button deleteButton = (Button) mActivity.findViewById(R.id.deleteCommentButton);
        OnClickListener deleteHandler = new OnClickListener() {
            @Override
            public void onClick(View arg0) {
                deleteComment();
            }
        };
        deleteButton.setOnClickListener(deleteHandler);
        Button mailButton = (Button) mActivity.findViewById(R.id.mailCommentButton);
        mailButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {
                sendEmailTo(mRecipient);
                createDeleteCommentDialog().show();
            }
        });
    }

    private void saveCurrentComment() {
        String recipientEmail = mRecipient.getEmail();
        saveComment(recipientEmail, commentField.getText().toString());
        String commentSavedMessage = getString(R.string.comment_altered_toast,
                getString(R.string.added_text),
                mRecipient);
        mActivity.hideCommentFragment(commentSavedMessage);
    }

    private void deleteComment() {
        String email = mRecipient.getEmail();
        Uri uri = Uri.parse(CommentContentProvider.CONTENT_URI + "/" + email);
        int rowsDeleted = mActivity.getContentResolver().delete(uri, null, null);
        Log.d(TAG, "num rows deleted = " + rowsDeleted);
        String commentDeletedMessage = getString(R.string.comment_altered_toast,
                getString(R.string.deleted_text), mRecipient);
        mActivity.hideCommentFragment(commentDeletedMessage);
    }

    private AlertDialog createDeleteCommentDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(mActivity);
        builder.setMessage(R.string.verify_delete_comment_text).setTitle(R.string.delete_button);
        builder.setPositiveButton(R.string.yes_button, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Delete the comment that was just mailed.
                deleteComment();
            }
        });
        builder.setNegativeButton(R.string.no_button, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Save the comment that was just mailed.
                saveCurrentComment();
            }
        });
        return builder.create();
    }

    private void sendEmailTo(Person person) {
        Intent emailIntent = new Intent(Intent.ACTION_SENDTO, Uri.fromParts("mailto", person
                .getEmail(), null));
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Hello from " + getString(R.string.app_name));
        emailIntent.putExtra(Intent.EXTRA_TEXT, commentField.getText());
        try {
            startActivity(Intent.createChooser(emailIntent, "Send Email"));
        } catch (ActivityNotFoundException e) {
            Toast.makeText(mActivity, "There are no email clients installed.",
                    Toast.LENGTH_SHORT).show();
        }
    }

    private Comment getCommentForRecipient(String recipient) {
        Uri uri = Uri.parse(CommentContentProvider.CONTENT_URI + "/" + recipient);
        Cursor cursor = mActivity.getContentResolver().query(uri, null, null, null, null);
        Comment comment = null;
        if (cursor.moveToFirst()) {
            comment = new Comment(cursor.getLong(MySQLiteOpenHelper.COLUMN_ID_POS), cursor
                    .getString(MySQLiteOpenHelper.COLUMN_RECIPIENT_POS), cursor
                    .getString(MySQLiteOpenHelper.COLUMN_CONTENT_POS));
            assert cursor.isLast();
        }
        cursor.close();
        return comment;
    }

    private boolean saveComment(String recipient, String content) {
        ContentResolver resolver = mActivity.getContentResolver();
        Uri uri = Uri.parse(CommentContentProvider.CONTENT_URI + "/" + recipient);
        ContentValues values = new ContentValues();
        values.put(MySQLiteOpenHelper.COLUMN_RECIPIENT, recipient);
        values.put(MySQLiteOpenHelper.COLUMN_CONTENT, content);
        Uri insertedUri = resolver.insert(uri, values);
        if (insertedUri == null || !insertedUri.equals(uri)) {
            // Comment already exists in the database and needs to be updated.
            int updatedRows = resolver.update(uri, values, null, null);
            return updatedRows > 0;
        }
        return true;
    }
}
