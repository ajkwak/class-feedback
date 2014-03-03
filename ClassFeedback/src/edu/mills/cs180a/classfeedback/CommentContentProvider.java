package edu.mills.cs180a.classfeedback;

import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;

public class CommentContentProvider extends ContentProvider {
    private static final String TAG = "CommentContentProvider";
    public static final String AUTHORITY = "edu.mills.cs180a.classfeedback";
    private static final String BASE_PATH = "comments";
    public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY
            + "/" + BASE_PATH);

    // Set up URI matching.
    private static final int COMMENTS = 1;
    private static final int COMMENTS_EMAIL = 2;
    private static final UriMatcher sURIMatcher = new UriMatcher(UriMatcher.NO_MATCH);
    static {
        sURIMatcher.addURI(AUTHORITY, BASE_PATH, COMMENTS);
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
    public int delete(Uri arg0, String arg1, String[] arg2) {
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
    public Uri insert(Uri uri, ContentValues values) {
        throw new UnsupportedOperationException("insert not supported");
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        throw new UnsupportedOperationException("update not supported");
    }

}
