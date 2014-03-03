
package edu.mills.cs180a.classfeedback;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

/**
 * An {@code Activity} that solicits a {@link Comment} about the specified {@link Person}.
 * The recipient of the comment is specified as an index into {@link Person#everyone}
 * and is communicated via the key {@link #RECIPIENT} in the {@link android.intent.Intent}.
 *
 * <P>The user is given the choice of saving or canceling the comment.  If saved,
 * it is added to the database, and the result code {@link Activity#RESULT_OK} is
 * provided to the parent activity.  Otherwise, the database is not modified, and
 * the result code {@link Activity#RESULT_CANCELED} is provided.
 *
 * @author ellen.spertus@gmail.com (Ellen Spertus)
 */
public class CommentActivity extends Activity {
    static final String RECIPIENT = "COMMENT_RECIPIENT";
    static final String ACTION = "COMMENT_ACTION";
    private int recipient;
    private CommentsDataSource cds;
    private EditText commentField;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comment);

        // Show a picture of the recipient.
        recipient = getIntent().getIntExtra(RECIPIENT, -1);
        assert(recipient >= 0 && recipient < Person.everyone.length);
        Person person = Person.everyone[recipient];
        ImageView icon = (ImageView) findViewById(R.id.commentImageView);
        icon.setImageResource(person.getImageId());

        // Get a connection to the database.
        cds = new CommentsDataSource(this);
        cds.open();

        // Set the text of the comment EditText to the value of the current comment, if any.
        commentField = (EditText) findViewById(R.id.commentEditText);
        Comment comment = cds.getCommentForRecipient(person.getEmail(), null);
        if (comment != null && comment.getContent() != null) {
            commentField.setText(comment.getContent());
        }

        // Add listeners.
        Button saveButton = (Button) findViewById(R.id.saveCommentButton);
        saveButton.setOnClickListener(new OnClickListener(){
            @Override
            public void onClick(View view) {
                saveComment();
            }
        });
        Button cancelButton = (Button) findViewById(R.id.cancelCommentButton);
        cancelButton.setOnClickListener(new OnClickListener(){
            @Override
            public void onClick(View view) {
                setResult(RESULT_CANCELED, new Intent().putExtra(RECIPIENT, recipient));
                finish();
            }
        });
        Button clearTextButton = (Button) findViewById(R.id.clearTextButton);
        clearTextButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {
                commentField.setText("");
            }
        });
        Button deleteButton = (Button) findViewById(R.id.deleteCommentButton);
        OnClickListener deleteHandler = new OnClickListener() {
            @Override
            public void onClick(View arg0) {
                deleteComment();
            }
        };
        deleteButton.setOnClickListener(deleteHandler);
        Button mailButton = (Button) findViewById(R.id.mailCommentButton);
        mailButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {
                sendEmailTo(Person.everyone[recipient]);
                createDeleteCommentDialog().show();
            }
        });
    }

    private void saveComment() {
        cds.createComment(Person.everyone[recipient].getEmail(), commentField.getText().toString());
        Intent intent = new Intent()
                .putExtra(RECIPIENT, recipient)
                .putExtra(ACTION, R.string.added_text);
        setResult(RESULT_OK, intent);
        finish();
    }

    private void deleteComment() {
        cds.deleteCommentForRecipient(Person.everyone[recipient].getEmail());
        Intent intent = new Intent()
                .putExtra(RECIPIENT, recipient)
                .putExtra(ACTION, R.string.deleted_text);
        setResult(RESULT_OK, intent);
        finish();
    }

    private AlertDialog createDeleteCommentDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
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
                saveComment();
            }
        });
        return builder.create();
    }

    private void sendEmailTo(Person person) {
        Intent emailIntent = new Intent(Intent.ACTION_SENDTO,
                Uri.fromParts("mailto", person.getEmail(), null));
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Hello from " + getString(R.string.app_name));
        emailIntent.putExtra(Intent.EXTRA_TEXT, commentField.getText());
        try {
            startActivity(Intent.createChooser(emailIntent, "Send Email"));
        } catch (ActivityNotFoundException e) {
            Toast.makeText(this, "There are no email clients installed.", Toast.LENGTH_SHORT)
                    .show();
        }
    }
}
