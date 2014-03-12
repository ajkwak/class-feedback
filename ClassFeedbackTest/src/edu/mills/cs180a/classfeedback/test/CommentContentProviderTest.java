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
    private static final String EMAIL = "foo@bar.com";
    private static final String COMMENT_CONTENT = "Hello, world!";

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
        Uri uri = Uri.parse(CommentContentProvider.CONTENT_URI + "/" + EMAIL);
        String[] projection = { "content" };  // desired columns
        Cursor cursor = mResolver.query(uri, projection, null, null, null);
        assertNotNull(cursor);
        assertEquals(0, cursor.getCount());
        cursor.close();
    }

    public void testInsert() {
        Uri uri = Uri.parse(CommentContentProvider.CONTENT_URI + "/" + EMAIL);
        ContentValues values = new ContentValues();
        values.put(MySQLiteOpenHelper.COLUMN_RECIPIENT, EMAIL);
        values.put(MySQLiteOpenHelper.COLUMN_CONTENT, COMMENT_CONTENT);

        // Verify that the values are inserted into the database.
        Uri actualInsertionUri = mProvider.insert(uri, values);
        assertEquals(uri, actualInsertionUri);
        Cursor cursor = mResolver.query(uri, null, null, null, null);
        assertNotNull(cursor);
        assertEquals(1, cursor.getCount());
        cursor.close();

        // Verify that the values cannot be added to the database a second time (no duplicates are
        // allowed).
        mProvider.insert(uri, values);
        cursor = mResolver.query(uri, null, null, null, null);
        assertNotNull(cursor);
        assertEquals(1, cursor.getCount());
        cursor.close();
    }
}
