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

    @Override
    public boolean onCreate() {
        return false;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
            String sortOrder) {
        Log.d(TAG, "In CommentContentProvider.query()");
        Log.d(TAG, "In CommentContentProvider, getContext().toString(): " + getContext().toString());
        CommentsDataSource cds = new CommentsDataSource(getContext());
        cds.open();
        Cursor cursor = null;
        switch (sURIMatcher.match(uri)) {
            case COMMENTS:
                Log.d(TAG, "In CommentContentProvider.query(), uri is COMMENTS");
                cursor = cds.getCursorForAllComments(projection);
                break;
            case COMMENTS_EMAIL:
                Log.d(TAG, "In CommentContentProvider.query(), uri is COMMENTS_EMAIL");
                cursor = cds.getCursorForCommentForRecipient(uri.getLastPathSegment(), projection);
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
        throw new UnsupportedOperationException("delete not supported");
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
        throw new UnsupportedOperationException("update not supported");
    }
}
