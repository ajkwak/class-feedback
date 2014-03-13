package edu.mills.cs180a.classfeedback;

import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.util.Log;

/**
 * A content provider for comments meant for specified individuals,
 * backed by {@link CommentsDataSource}.
 *
 * @author ellen.spertus@gmail.com (Ellen Spertus)
 */
public class CommentContentProvider extends ContentProvider {
    private static final String TAG = "CommentContentProvider";
    /**
     * The authority for this content provider.  This must appear in this
     * application's AndroidManifest.xml and in request URLs from clients.
     */
    public static final String AUTHORITY = "edu.mills.cs180a.classfeedback";
    private static final String BASE_PATH = "comments";
    public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY
            + "/" + BASE_PATH);

    // Set up URI matching.
    private static final int COMMENTS = 1;
    private static final int COMMENTS_EMAIL = 2;
    private static final UriMatcher sURIMatcher = new UriMatcher(UriMatcher.NO_MATCH);
    static {
        // Get all comments.
        sURIMatcher.addURI(AUTHORITY, BASE_PATH, COMMENTS);
        // Get all comments for a specific email address.
        sURIMatcher.addURI(AUTHORITY, BASE_PATH + "/*", COMMENTS_EMAIL);
    }

    public static Uri getContentUriForEmail(String email) { // TODO: TEST
        return Uri.parse(CONTENT_URI + "/" + email);
    }

    @Override
    public boolean onCreate() {
        return false;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
            String sortOrder) {
        MySQLiteOpenHelper dbHelper = new MySQLiteOpenHelper(getContext());
        SQLiteDatabase database = dbHelper.getWritableDatabase();
        Cursor cursor = null;
        switch (sURIMatcher.match(uri)) {
        case COMMENTS:
            Log.d(TAG, "In CommentContentProvider.query(), uri is COMMENTS");
            cursor = database.query(MySQLiteOpenHelper.TABLE_COMMENTS, projection, selection,
                    selectionArgs, null, null, sortOrder);
            break;
        case COMMENTS_EMAIL:
            Log.d(TAG, "In CommentContentProvider.query(), uri is COMMENTS_EMAIL");
            selection = updateSelectionWithRecipientConstraint(uri.getLastPathSegment(), selection);
            cursor = database.query(MySQLiteOpenHelper.TABLE_COMMENTS, projection, selection,
                    selectionArgs, null, null, sortOrder);
            break;
        default:
            Log.d(TAG, "In CommentContentProvider.query(), uri is not matched: " + uri);
            throw new IllegalArgumentException("Illegal uri: " + uri);
        }
        // Notify anyone listening on the URI.
        cursor.setNotificationUri(getContext().getContentResolver(), uri);

        return cursor;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        if (sURIMatcher.match(uri) == COMMENTS_EMAIL) {
            Log.d(TAG, "In CommentContentProvider.insert(), uri is COMMENTS_EMAIL");
            MySQLiteOpenHelper dbHelper = new MySQLiteOpenHelper(getContext());
            SQLiteDatabase database = dbHelper.getWritableDatabase();
            long insertId = database.insert(MySQLiteOpenHelper.TABLE_COMMENTS, null, values);
            if (insertId == -1) { // Then error occurred during insertion.
                return null;
            }
            // Notify anyone listening on the URI.
            getContext().getContentResolver().notifyChange(uri, null);
            return uri;
        }
        Log.d(TAG, "In CommentContentProvider.insert(), uri is not matched: " + uri);
        throw new IllegalArgumentException("Illegal uri: " + uri);
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        MySQLiteOpenHelper dbHelper = new MySQLiteOpenHelper(getContext());
        SQLiteDatabase database = dbHelper.getWritableDatabase();

        int rowsDeleted = 0;
        switch (sURIMatcher.match(uri)) {
        case COMMENTS:
            Log.d(TAG, "In CommentContentProvider.query(), uri is COMMENTS");
            rowsDeleted = database.delete(MySQLiteOpenHelper.TABLE_COMMENTS, selection,
                    selectionArgs);
            break;
        case COMMENTS_EMAIL:
            Log.d(TAG, "In CommentContentProvider.query(), uri is COMMENTS_EMAIL");
            selection = updateSelectionWithRecipientConstraint(uri.getLastPathSegment(), selection);
            rowsDeleted = database.delete(MySQLiteOpenHelper.TABLE_COMMENTS, selection,
                    selectionArgs);
            break;
        default:
            Log.d(TAG, "In CommentContentProvider.query(), uri is not matched: " + uri);
            throw new IllegalArgumentException("Illegal uri: " + uri);
        }
        // Notify anyone listening on the URI.
        getContext().getContentResolver().notifyChange(uri, null);
        return rowsDeleted;
    }

    @Override
    public String getType(Uri uri) {
        switch (sURIMatcher.match(uri)) {
            case COMMENTS_EMAIL:
            case COMMENTS:
                return ContentResolver.CURSOR_DIR_BASE_TYPE;
            default:
                Log.e(TAG, "Unrecognized uri: " + uri);
                return null;
        }
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
    	MySQLiteOpenHelper dbHelper = new MySQLiteOpenHelper(getContext());
        SQLiteDatabase database = dbHelper.getWritableDatabase();
        int rowsUpdated = 0;
        switch (sURIMatcher.match(uri)) {
        case COMMENTS:
            Log.d(TAG, "In CommentContentProvider.update(), uri is COMMENTS");
            rowsUpdated = database.update(MySQLiteOpenHelper.TABLE_COMMENTS, values, selection, selectionArgs);
            break;
        case COMMENTS_EMAIL:
            Log.d(TAG, "In CommentContentProvider.update(), uri is COMMENTS_EMAIL");
            selection = updateSelectionWithRecipientConstraint(uri.getLastPathSegment(), selection);
            rowsUpdated = database.update(MySQLiteOpenHelper.TABLE_COMMENTS, values, selection, selectionArgs);
            break;
        default:
            Log.d(TAG, "In CommentContentProvider.update(), uri is not matched: " + uri);
            throw new IllegalArgumentException("Illegal uri: " + uri);
        }
        // Notify anyone listening on the URI.
        getContext().getContentResolver().notifyChange(uri, null);
        return rowsUpdated;
    }

    private String updateSelectionWithRecipientConstraint(String recipient, String selection) {
        String recipientSelection = MySQLiteOpenHelper.COLUMN_RECIPIENT + " = '" + recipient + "'";
        return (selection == null || selection.trim().length() == 0) ? recipientSelection
                : selection + " and " + recipientSelection;
    }
}
