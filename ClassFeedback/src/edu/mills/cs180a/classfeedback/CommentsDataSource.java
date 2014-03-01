
package edu.mills.cs180a.classfeedback;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

/**
 * Persistent data storage for {@link Comment} using a database defined in
 * {@link MySQLiteOpenHelper}.  This reuses code from
 * <a href="http://www.vogella.com/tutorials/AndroidSQLite/article.html">
 * Android SQLite database and content provider - Tutorial</a> by Lars Vogella.
 *
 * @author ellen.spertus@gmail.com (Ellen Spertus)
 */
public class CommentsDataSource {
    private static final String TAG = "CommentsDataSource";
    private SQLiteDatabase database;
    private MySQLiteOpenHelper dbHelper;

    /**
     * Constructs a {@code CommentsDataSource}.  The {@link #open()} method must be
     * called before retrieving data from this.
     *
     * @param context required context for the associated {@link SQLiteDatabase}
     */
    public CommentsDataSource(Context context) {
        dbHelper = new MySQLiteOpenHelper(context);
    }

    /**
     * Opens a connection to the database, creating it if necessary.
     * This should be called before any of the other methods.
     * When the connection is no longer needed, {@link #close()} should be called.
     *
     * @throws SQLException if the database could not be opened
     */
    public void open() throws SQLException {
        database = dbHelper.getWritableDatabase();
    }

    /**
     * Closes the connection to the database, opened with {@link #open()}.
     */
    public void close() {
        dbHelper.close();
    }

    /**
     * Creates a comment with the specified content for the specified recipient.
     * This both adds the comment to the database and constructs a {@link Comment}
     * instance.
     *
     * @param recipient the email address of the recipient
     * @param content the content of the comment
     * @return a new {@link Comment} instance
     */
    Comment createComment(String recipient, String content) {
        if (database == null) {
            open();
        }

        ContentValues values = new ContentValues();
        values.put(MySQLiteOpenHelper.COLUMN_RECIPIENT, recipient);
        values.put(MySQLiteOpenHelper.COLUMN_CONTENT, content);
        long insertId = database.insert(MySQLiteOpenHelper.TABLE_COMMENTS, null,
                values);
        if (insertId == -1) { // If insert fails.
            insertId = database.update(MySQLiteOpenHelper.TABLE_COMMENTS, values,
                    MySQLiteOpenHelper.COLUMN_RECIPIENT + "='" + recipient + "'", null);
            Log.d(TAG, "Updated comment " + insertId + " in database.");
        } else {
            Log.d(TAG, "Inserted comment " + insertId + " into database.");
        }
        return new Comment(insertId, recipient, content);
    }

    /**
     * Queries the database for all comments for the specified recipient.
     *
     * @param recipient the email address of the target of the comment
     * @param projection the names of the columns to retrieve
     * @return a {@code Cursor} pointing to all comments for the recipient
     */
    Cursor getCursorForCommentsForRecipient(String recipient, String[] projection) {
        if (database == null) {
            open();
        }
        return database.query(MySQLiteOpenHelper.TABLE_COMMENTS,
                projection, "recipient = \"" + recipient + "\"", null, null, null, null);
    }

    /**
     * Queries database for all comments.
     *
     * @param projection the names of the columns to retrieve
     * @return a {@code Cursor} referencing all comments in the database
     */
    Cursor getCursorForAllComments(String[] projection) {
        if (database == null) {
            open();
        }
        return database.query(MySQLiteOpenHelper.TABLE_COMMENTS,
                projection, null, null, null, null, null);
    }

    /**
     * Retrieve all comments from the database.
     *
     * @return all comments in the database
     */
    List<Comment> getAllComments(String[] projection) {
        List<Comment> comments = new ArrayList<Comment>();

        Cursor cursor = getCursorForAllComments(projection);
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            Comment comment = cursorToComment(cursor);
            comments.add(comment);
            cursor.moveToNext();
        }
        cursor.close();

        return comments;
    }

    /**
     * Retrieves the comment associated with the given recipient in the database.
     *
     * @param recipient the email address of the target of the comment
     * @param projection the names of the columns to retrieve
     * @return the comment associated with the given recipient in the database
     */
    Comment getCommentForRecipient(String recipient, String[] projection) {
        Cursor cursor = getCursorForCommentsForRecipient(recipient, projection);
        cursor.moveToFirst();
        Comment comment = null;
        if (!cursor.isAfterLast()) { // Then the given recipient is associated with a comment.
            comment = cursorToComment(cursor);
            cursor.moveToNext();
            assert cursor.isAfterLast(); // Should only have 1 comment associated with a recipient.
        }
        cursor.close();
        return comment;
    }

    private Comment cursorToComment(Cursor cursor) {
        Comment comment = new Comment(
                cursor.getLong(MySQLiteOpenHelper.COLUMN_ID_POS),
                cursor.getString(MySQLiteOpenHelper.COLUMN_RECIPIENT_POS),
                cursor.getString(MySQLiteOpenHelper.COLUMN_CONTENT_POS));
        return comment;
    }
}