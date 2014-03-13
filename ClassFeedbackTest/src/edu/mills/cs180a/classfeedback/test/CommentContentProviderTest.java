package edu.mills.cs180a.classfeedback.test;

import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.test.ProviderTestCase2;
import android.test.mock.MockContentResolver;
import edu.mills.cs180a.classfeedback.CommentContentProvider;
import edu.mills.cs180a.classfeedback.MySQLiteOpenHelper;

// This creates an IsolatedContext and does not affect the production store.
public class CommentContentProviderTest extends ProviderTestCase2<CommentContentProvider> {
    private MockContentResolver mResolver;
    private CommentContentProvider mProvider;
    private static final String EMAIL_1 = "foo@bar.com";
    private static final String EMAIL_2 = "bar@foo.edu";
    private static final String COMMENT_CONTENT_1 = "Hello, world!";
    private static final String COMMENT_CONTENT_2 = "Goodbye, world!";

    public CommentContentProviderTest() {
        super(CommentContentProvider.class, CommentContentProvider.AUTHORITY);
    }

    @Override
    public void setUp() throws Exception {
        super.setUp();
        mResolver = getMockContentResolver();
        mProvider = getProvider();
    }

    public void testNoCommentsForEllenAtStart() {
        Uri uri = Uri.parse(CommentContentProvider.CONTENT_URI + "/" + EMAIL_1);
        String[] projection = { "content" };  // desired columns
        Cursor cursor = mResolver.query(uri, projection, null, null, null);
        assertNotNull(cursor);
        assertEquals(0, cursor.getCount());
        cursor.close();
    }

    public void testInsert() {
        Uri uri = Uri.parse(CommentContentProvider.CONTENT_URI + "/" + EMAIL_1);
        ContentValues values = new ContentValues();
        values.put(MySQLiteOpenHelper.COLUMN_RECIPIENT, EMAIL_1);
        values.put(MySQLiteOpenHelper.COLUMN_CONTENT, COMMENT_CONTENT_1);

        // Verify that the values are inserted into the database.
        Uri actualInsertionUri = mProvider.insert(uri, values);
        assertEquals(uri, actualInsertionUri);
        Cursor cursor = mResolver.query(uri, null, null, null, null);
        assertNotNull(cursor);
        assertEquals(1, cursor.getCount());

        // Verify that the values inserted into the database were correct.
        cursor.moveToFirst();
        String insertedRecipient = cursor.getString(cursor
                .getColumnIndex(MySQLiteOpenHelper.COLUMN_RECIPIENT));
        assertEquals(EMAIL_1, insertedRecipient);
        String insertedContent = cursor.getString(cursor
                .getColumnIndex(MySQLiteOpenHelper.COLUMN_CONTENT));
        assertEquals(COMMENT_CONTENT_1, insertedContent);
        cursor.close();

        // Verify that the values cannot be added to the database a second time (no duplicates are
        // allowed).
        mProvider.insert(uri, values);
        assertEquals(1, getNumberOfCommentsInDatabase());
    }

    public void testDelete_noCommentsInDatabase() {
        // Verify that database is initially empty.
        assertEquals(0, getNumberOfCommentsInDatabase());

        // Verify that no comments are deleted when try to delete all comments.
        assertEquals(0, mProvider.delete(CommentContentProvider.CONTENT_URI, null, null));
        assertEquals(0, getNumberOfCommentsInDatabase());
    }

    public void testDelete_allComments() {
        populateDatabase();

        // Delete all comments.
        Uri uri = CommentContentProvider.CONTENT_URI;
        assertEquals(2, mProvider.delete(uri, null, null));

        // Check that there are no comments in the database.
        Cursor cursor = mResolver.query(uri, null, null, null, null);
        assertNotNull(cursor);
        assertEquals(0, cursor.getCount());
    }

    public void testDelete_commentForSingleRecipient_existingRecipient() {
        populateDatabase();

        Uri uri = Uri.parse(CommentContentProvider.CONTENT_URI + "/" + EMAIL_2);
        assertEquals(1, mProvider.delete(uri, null, null));

        // Verify the correct comment was deleted (so only the un-deleted comment remains).
        Cursor cursor = mResolver.query(CommentContentProvider.CONTENT_URI, null, null, null, null);
        assertNotNull(cursor);
        assertEquals(1, cursor.getCount());
        cursor.moveToFirst();
        String insertedRecipient = cursor.getString(cursor
                .getColumnIndex(MySQLiteOpenHelper.COLUMN_RECIPIENT));
        assertEquals(EMAIL_1, insertedRecipient);
        String insertedContent = cursor.getString(cursor
                .getColumnIndex(MySQLiteOpenHelper.COLUMN_CONTENT));
        assertEquals(COMMENT_CONTENT_1, insertedContent);
        cursor.close();
    }

    public void testDelete_commentForSingleRecipient_nonExistentRecipient() {
        populateDatabase();

        // Delete comment for non-existent recipient.
        Uri uri = Uri.parse(CommentContentProvider.CONTENT_URI + "/" + "idontexist@android.com");
        assertEquals(0, mProvider.delete(uri, null, null));
        assertEquals(2, getNumberOfCommentsInDatabase()); // No comments deleted.
    }

    public void testDelete_withSelection() {
        populateDatabase();

        Uri uri = CommentContentProvider.CONTENT_URI;
        assertEquals(1, mProvider.delete(uri, MySQLiteOpenHelper.COLUMN_RECIPIENT + " = '"
                + EMAIL_1 + "'", null));

        // Verify the correct comment was deleted (so only the un-deleted comment remains).
        Cursor cursor = mResolver.query(CommentContentProvider.CONTENT_URI, null, null, null, null);
        assertNotNull(cursor);
        assertEquals(1, cursor.getCount());
        cursor.moveToFirst();
        String insertedRecipient = cursor.getString(cursor
                .getColumnIndex(MySQLiteOpenHelper.COLUMN_RECIPIENT));
        assertEquals(EMAIL_2, insertedRecipient);
        String insertedContent = cursor.getString(cursor
                .getColumnIndex(MySQLiteOpenHelper.COLUMN_CONTENT));
        assertEquals(COMMENT_CONTENT_2, insertedContent);
        cursor.close();
    }

    public void testDelete_withSelectionAndSelectionArgs() {
        populateDatabase();

        Uri uri = CommentContentProvider.CONTENT_URI;
        assertEquals(1, mProvider.delete(uri, MySQLiteOpenHelper.COLUMN_CONTENT + "=?",
                new String[] { "Hello, world!" }));

        // Verify the correct comment was deleted (so only the un-deleted comment remains).
        Cursor cursor = mResolver.query(CommentContentProvider.CONTENT_URI, null, null, null, null);
        assertNotNull(cursor);
        assertEquals(1, cursor.getCount());
        cursor.moveToFirst();
        String insertedRecipient = cursor.getString(cursor
                .getColumnIndex(MySQLiteOpenHelper.COLUMN_RECIPIENT));
        assertEquals(EMAIL_2, insertedRecipient);
        String insertedContent = cursor.getString(cursor
                .getColumnIndex(MySQLiteOpenHelper.COLUMN_CONTENT));
        assertEquals(COMMENT_CONTENT_2, insertedContent);
        cursor.close();
    }

    private void populateDatabase() {
        // Insert the first comment into the database.
        Uri uri = Uri.parse(CommentContentProvider.CONTENT_URI + "/" + EMAIL_1);
        ContentValues values = new ContentValues();
        values.put(MySQLiteOpenHelper.COLUMN_RECIPIENT, EMAIL_1);
        values.put(MySQLiteOpenHelper.COLUMN_CONTENT, COMMENT_CONTENT_1);
        assertNotNull(mProvider.insert(uri, values));

        // Insert the second comment into the database.
        uri = Uri.parse(CommentContentProvider.CONTENT_URI + "/" + EMAIL_2);
        values = new ContentValues();
        values.put(MySQLiteOpenHelper.COLUMN_RECIPIENT, EMAIL_2);
        values.put(MySQLiteOpenHelper.COLUMN_CONTENT, COMMENT_CONTENT_2);
        assertNotNull(mProvider.insert(uri, values));

        // Verify that the comments have been added to the database.
        assertEquals(2, getNumberOfCommentsInDatabase());
    }

    private int getNumberOfCommentsInDatabase() {
        Cursor cursor = mResolver.query(CommentContentProvider.CONTENT_URI, null, null, null, null);
        assertNotNull(cursor);
        int numComments = cursor.getCount();
        cursor.close();
        return numComments;
    }
}
