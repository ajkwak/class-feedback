package edu.mills.cs180a.classfeedback;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
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
	private static final String TAG = "CommentActivity";
	public static final String RECIPIENT = "COMMENT_RECIPIENT";
	public static final String ACTION = "COMMENT_ACTION";
	private int mRecipient;
	private EditText commentField;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_comment);

		// Show a picture of the recipient.
		mRecipient = getIntent().getIntExtra(RECIPIENT, -1);
		assert(mRecipient >= 0 && mRecipient < Person.everyone.length);
		Person person = Person.everyone[mRecipient];
		ImageView icon = (ImageView) findViewById(R.id.commentImageView);
		icon.setImageResource(person.getImageId());

		// Set the text of the comment EditText to the value of the current comment, if any.
		commentField = (EditText) findViewById(R.id.commentEditText);
		Comment comment = getCommentForRecipient(person.getEmail());
		if (comment != null && comment.getContent() != null) {
			commentField.setText(comment.getContent());
		}

		// Add listeners.
		Button saveButton = (Button) findViewById(R.id.saveCommentButton);
		saveButton.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View view) {
				saveCurrentComment();
			}
		});
		Button cancelButton = (Button) findViewById(R.id.cancelCommentButton);
		cancelButton.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View view) {
				setResult(RESULT_CANCELED, new Intent().putExtra(RECIPIENT, mRecipient));
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
				sendEmailTo(Person.everyone[mRecipient]);
				createDeleteCommentDialog().show();
			}
		});
	}


	private void saveCurrentComment() {
		Log.d(TAG, "saveComment()");
		Log.d(TAG, "recipient = " + mRecipient);
		String recipientEmail = Person.everyone[mRecipient].getEmail();
		Log.d(TAG, "recipient email = " + recipientEmail);
		Log.d(TAG, "commentField = " + commentField);
		Log.d(TAG, "commentField text = " + commentField.getText());
		saveComment(Person.everyone[mRecipient].getEmail(), commentField.getText().toString());
		Intent intent = new Intent()
			.putExtra(RECIPIENT, mRecipient)
			.putExtra(ACTION, R.string.added_text);
		setResult(RESULT_OK, intent);
		finish();
	}

	private void deleteComment() {
		String email = Person.everyone[mRecipient].getEmail();
		Uri uri = Uri.parse(CommentContentProvider.CONTENT_URI + "/" + email);
		getContentResolver().delete(uri, null, null);
		Intent intent = new Intent()
			.putExtra(RECIPIENT, mRecipient)
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
				saveCurrentComment();
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

	private Comment getCommentForRecipient(String recipient) {
		Uri uri = Uri.parse(CommentContentProvider.CONTENT_URI + "/" + recipient);
		Cursor cursor = getContentResolver().query(uri, null, null, null, null);
		Comment comment = null;
		if (cursor.moveToFirst()) {
			comment = new Comment(
					cursor.getLong(MySQLiteOpenHelper.COLUMN_ID_POS),
					cursor.getString(MySQLiteOpenHelper.COLUMN_RECIPIENT_POS),
					cursor.getString(MySQLiteOpenHelper.COLUMN_CONTENT_POS));
			assert cursor.isLast();
		}
		cursor.close();
		return comment;
	}

	private boolean saveComment(String recipient, String content) {
		ContentResolver resolver = getContentResolver();
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

