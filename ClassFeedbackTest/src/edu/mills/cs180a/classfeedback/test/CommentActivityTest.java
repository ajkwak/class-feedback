package edu.mills.cs180a.classfeedback.test;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.test.ActivityInstrumentationTestCase2;
import android.test.UiThreadTest;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import edu.mills.cs180a.classfeedback.CommentActivity;
import edu.mills.cs180a.classfeedback.CommentsDataSource;
import edu.mills.cs180a.classfeedback.MySQLiteOpenHelper;
import edu.mills.cs180a.classfeedback.Person;
import edu.mills.cs180a.classfeedback.R;

public class CommentActivityTest extends ActivityInstrumentationTestCase2<CommentActivity> {
	private static final int RECIPIENT_INDEX = 0;  // Use person 0 in Person.everyone.
	private static final Person RECIPIENT = Person.everyone[RECIPIENT_INDEX];
	private static final String COMMENT_TEXT = "lorem ipsum";
	private CommentActivity mActivity;
	private ImageView mImageView;
	private EditText mCommentField;
	private Button mSaveButton;
	private Button mCancelButton;
	private CommentsDataSource mCds;
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
		i.putExtra(CommentActivity.CDS_FACTORY, new MockCommentsDataSourceFactory());
		setActivityIntent(i);
		// This must occur after setting the touch mode and intent.
		mActivity = getActivity();

		// Initialize references to views.
		mImageView = (ImageView) mActivity.findViewById(R.id.commentImageView);
		mCommentField = (EditText) mActivity.findViewById(R.id.commentEditText);
		mSaveButton = (Button) mActivity.findViewById(R.id.saveCommentButton);
		mCancelButton = (Button) mActivity.findViewById(R.id.cancelCommentButton);
	}

	@Override
	protected void tearDown() throws Exception {
		// Do not close mCds here.  CommentActivity will have closed it.
		// mCds.close();
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

	private int getNumCommentsForRecipient(Person recipient) {
		Cursor cursor = mCds.getCursorForCommentForRecipient(
				recipient.getEmail(), null);
		int count = cursor.getCount();
		cursor.close();
		return count;
	}

	@UiThreadTest
	public void testCommentEntry() {
		mCds = MockCommentsDataSource.create(null);  // context argument ignored
		String[] desiredColumns = { MySQLiteOpenHelper.COLUMN_CONTENT };
		assertEquals(0, getNumCommentsForRecipient(RECIPIENT));

		// Simulate entering a comment.
		mCommentField.setText(COMMENT_TEXT);
		mSaveButton.performClick();

		Cursor cursor = mCds.getCursorForCommentForRecipient(
				RECIPIENT.getEmail(), desiredColumns);
		assertEquals(1, cursor.getCount());
		assertTrue(cursor.moveToFirst());
		assertEquals(COMMENT_TEXT, cursor.getString(0));
		assertFalse(cursor.moveToNext());
		cursor.close();
	}

	@UiThreadTest
	public void testCancelButton() {
		mCds = MockCommentsDataSource.create(null);  // context argument ignored
		String[] desiredColumns = { MySQLiteOpenHelper.COLUMN_CONTENT };
		assertEquals(0, getNumCommentsForRecipient(RECIPIENT));
		
		// Simulate adding text and cancel request.
		mCommentField.setText(COMMENT_TEXT);
		mCancelButton.performClick();

		// Verify comment was not added to database.
		Cursor cursor = mCds.getCursorForCommentForRecipient(
				RECIPIENT.getEmail(), desiredColumns);
		assertEquals(0, getNumCommentsForRecipient(RECIPIENT));
		assertFalse(cursor.moveToNext());
		cursor.close();
	}
}
