package edu.mills.cs180a.classfeedback.test;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.test.ActivityInstrumentationTestCase2;
import android.test.UiThreadTest;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import edu.mills.cs180a.classfeedback.CommentActivity;
import edu.mills.cs180a.classfeedback.CommentContentProvider;
import edu.mills.cs180a.classfeedback.MySQLiteOpenHelper;
import edu.mills.cs180a.classfeedback.Person;
import edu.mills.cs180a.classfeedback.R;

public class CommentActivityTest extends ActivityInstrumentationTestCase2<CommentActivity> {
	private static final int RECIPIENT_INDEX = 0;  // Use person 0 in Person.everyone.
	private static final Person RECIPIENT = Person.everyone[RECIPIENT_INDEX];
	private static final String COMMENT_TEXT = "lorem ipsum";
	private CommentActivity mActivity;
	private ContentResolver mResolver;
	private ImageView mImageView;
	private EditText mCommentField;
	private Button mSaveButton;
	private Button mCancelButton;
	private Button mDeleteButton;
	private Button mClearButton;
	private static final String TAG = "CommentActivityTest";

	public CommentActivityTest() {
		super(CommentActivity.class);
	}

	@Override
	protected void setUp() throws Exception {
		super.setUp();

		Intent i = new Intent();
		setActivityInitialTouchMode(true);
		i.putExtra(CommentActivity.RECIPIENT, RECIPIENT_INDEX);
		setActivityIntent(i);
		// This must occur after setting the touch mode and intent.
		mActivity = getActivity();
		mResolver = mActivity.getContentResolver();

		// Initialize references to views.
		mImageView = (ImageView) mActivity.findViewById(R.id.commentImageView);
		mCommentField = (EditText) mActivity.findViewById(R.id.commentEditText);
		mSaveButton = (Button) mActivity.findViewById(R.id.saveCommentButton);
		mCancelButton = (Button) mActivity.findViewById(R.id.cancelCommentButton);
		mDeleteButton = (Button) mActivity.findViewById(R.id.deleteCommentButton);
		mClearButton = (Button) mActivity.findViewById(R.id.clearTextButton);
	}

	@Override
	protected void tearDown() throws Exception {
		// Clear the database.
		mResolver.delete(CommentContentProvider.CONTENT_URI, null, null);

		// Note that our tear down code must go before the call to super.tearDown(),
		// which apparently nulls out our instance variables.
		super.tearDown();
	}

	// Make sure the imageView contains the picture of the right person.
	public void testImageView() {
		Drawable expectedDrawable =
				mActivity.getResources().getDrawable(RECIPIENT.getImageId());
		// Drawables cannot be compared directly.   Instead, compare their
		// constant state, which will be the same for any instances
		// created from the same resource.
		assertEquals(expectedDrawable.getConstantState(),
				mImageView.getDrawable().getConstantState());
	}

	public void testCommentField_noComment() {
		// If no comment associated with the recipient (as when the database is empty), the comment
		// field should be initially empty.
		assertEquals(0, mCommentField.getText().length());
	}

	public void testCommentField_withExistingComment() {
		// fail("Not yet implemented");
	}

	// Test comment entry twice, to make sure that the database has no comments
	// at the beginning of each test.  Note that the order in which tests run
	// is undefined within JUnit, so we cannot assume that testCommentEntry1()
	// runs before testCommentEntry2().
	@UiThreadTest
	public void testCommentEntry1() {
		internalTestCommentEntry();
	}

	@UiThreadTest
	public void testCommentEntry2() {
		internalTestCommentEntry();
	}

	@UiThreadTest
	public void testCancelButton() {
		String[] desiredColumns = { MySQLiteOpenHelper.COLUMN_CONTENT };
		assertEquals(0, getNumCommentsForRecipient(RECIPIENT));

		// Simulate adding text and cancel request.
		mCommentField.setText(COMMENT_TEXT);
		mCancelButton.performClick();

		// Verify comment was not added to database.
		Uri uri = CommentContentProvider.getContentUriForEmail(RECIPIENT.getEmail());
		Cursor cursor = mResolver.query(uri, desiredColumns, null, null, null);
		assertEquals(0, getNumCommentsForRecipient(RECIPIENT));
		assertFalse(cursor.moveToNext());
		cursor.close();
	}

	@UiThreadTest
	public void testSaveButton_insertsNewComment() {
		// Database is initially empty.
		assertEquals(0, getNumCommentsForRecipient(RECIPIENT));
		assertEquals("", mCommentField.getText().toString());

		// Simulate adding text and save request.
		mCommentField.setText(COMMENT_TEXT);
		mSaveButton.performClick();

		// Verify comment was successfully added to database.
		Uri uri = CommentContentProvider.getContentUriForEmail(RECIPIENT.getEmail());
		String[] desiredColumns = {
				MySQLiteOpenHelper.COLUMN_RECIPIENT,
				MySQLiteOpenHelper.COLUMN_CONTENT };
		Cursor cursor = mResolver.query(uri, desiredColumns, null, null, null);
		assertEquals(1, getNumCommentsForRecipient(RECIPIENT));
		cursor.moveToNext();
		assertEquals(RECIPIENT.getEmail(),
				cursor.getString(cursor.getColumnIndex(MySQLiteOpenHelper.COLUMN_RECIPIENT)));
		assertEquals(COMMENT_TEXT,
				cursor.getString(cursor.getColumnIndex(MySQLiteOpenHelper.COLUMN_CONTENT)));
		assertTrue(cursor.isLast()); // There is only one comment for the recipient.
		cursor.close();
	}

	@UiThreadTest
	public void testSaveButton_updatesExistingComment() {
		populateDatabase();

		// Verify that recipient is associated with a comment.
		assertEquals(1, getNumCommentsForRecipient(RECIPIENT));

		// Simulate adding text and save request.
		String newCommentText = "Bacillus mycoides";
		assertFalse(newCommentText.equals(COMMENT_TEXT)); // Don't want to update text to same value.
		mCommentField.setText(newCommentText);
		mSaveButton.performClick();

		// Verify comment was successfully added to database.
		Uri uri = CommentContentProvider.getContentUriForEmail(RECIPIENT.getEmail());
		String[] desiredColumns = {
				MySQLiteOpenHelper.COLUMN_RECIPIENT,
				MySQLiteOpenHelper.COLUMN_CONTENT };
		Cursor cursor = mResolver.query(uri, desiredColumns, null, null, null);
		assertEquals(1, getNumCommentsForRecipient(RECIPIENT));
		cursor.moveToNext();
		assertEquals(RECIPIENT.getEmail(),
				cursor.getString(cursor.getColumnIndex(MySQLiteOpenHelper.COLUMN_RECIPIENT)));
		assertEquals(newCommentText,
				cursor.getString(cursor.getColumnIndex(MySQLiteOpenHelper.COLUMN_CONTENT)));
		assertTrue(cursor.isLast()); // There is only one comment for the recipient.
		cursor.close();
	}

	// Try deleting the comment when there is no comment to delete.
	@UiThreadTest
	public void testDeleteButton_emptyDatabase() {
		// Database is initially empty.
		assertEquals(0, getNumCommentsForRecipient(RECIPIENT));
		assertEquals("", mCommentField.getText().toString());

		// Simulate a delete request.
		mDeleteButton.performClick();

		// Verify there is still no comment associated with the recipient.
		assertEquals(0, getNumCommentsForRecipient(RECIPIENT));
	}

	@UiThreadTest
	public void testDeleteButton_withExistingComment() {
		populateDatabase();

		// Verify that recipient is associated with a comment.
		assertEquals(1, getNumCommentsForRecipient(RECIPIENT));

		// Simulate a delete request.
		mDeleteButton.performClick();

		// Verify that comment has been deleted.
		assertEquals(0, getNumCommentsForRecipient(RECIPIENT));
	}

	@UiThreadTest
	public void testClearButton() {
		// Simulate typing in a comment.
		assertFalse(COMMENT_TEXT.length() == 0);
		mCommentField.setText(COMMENT_TEXT);

		// Simulate a clear request.
		mClearButton.performClick();

		// Verify that the comment field is now empty
		assertEquals("", mCommentField.getText().toString());
	}

	private int getNumCommentsForRecipient(Person recipient) {
		Uri uri = CommentContentProvider.getContentUriForEmail(recipient.getEmail());
		Cursor cursor = mResolver.query(uri, new String[] { MySQLiteOpenHelper.COLUMN_CONTENT },
				null, null, null);
		int count = cursor.getCount();
		cursor.close();
		return count;
	}

	private void internalTestCommentEntry() {
		String[] desiredColumns = { MySQLiteOpenHelper.COLUMN_CONTENT };
		assertEquals("Database is not empty at beginning of test.", 0,
				getNumCommentsForRecipient(RECIPIENT));

		// Simulate entering a comment.
		mCommentField.setText(COMMENT_TEXT);
		mSaveButton.performClick();
		Uri uri = CommentContentProvider.getContentUriForEmail(RECIPIENT.getEmail());
		Cursor cursor = mResolver.query(uri, desiredColumns, null, null, null);

		assertEquals(1, cursor.getCount());
		assertTrue(cursor.moveToFirst());
		assertEquals(COMMENT_TEXT, cursor.getString(0));
		assertFalse(cursor.moveToNext());
		cursor.close();
	}

	private void populateDatabase() {
		String email = RECIPIENT.getEmail();
		Uri uri = CommentContentProvider.getContentUriForEmail(email);
		ContentValues values = new ContentValues();
		values.put(MySQLiteOpenHelper.COLUMN_RECIPIENT, email);
		values.put(MySQLiteOpenHelper.COLUMN_CONTENT, COMMENT_TEXT);
		mResolver.insert(uri, values);
	}
}
