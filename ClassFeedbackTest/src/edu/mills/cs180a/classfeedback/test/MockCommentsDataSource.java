package edu.mills.cs180a.classfeedback.test;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.content.Context;
import android.database.Cursor;
import android.test.mock.MockCursor;
import edu.mills.cs180a.classfeedback.Comment;
import edu.mills.cs180a.classfeedback.CommentsDataSource;

public class MockCommentsDataSource extends CommentsDataSource {
    private static MockCommentsDataSource instance;
    private Map<String, Comment> commentMap = new HashMap<String, Comment>();
    private long commentId = 1;
    private static final String TAG = "MockCommentsDataSource";

    private MockCommentsDataSource(Context context) {
        super(context, null);
    }

    /**
     * Provides a singleton instance of this class.
     *
     * @param context the context for the underlying database (used only on first call)
     * @return a singleton {@link MockCommentsDataSource}
     */
    public static synchronized MockCommentsDataSource create(Context context) {
        if (instance == null) {
            instance = new MockCommentsDataSource(context);
        }
        return instance;
    }

    @Override
    public void open() {
        // Do nothing (no connection to make).
    }

    @Override
    public void close() {
        // Do nothing (no connection to close).
    }

    /**
     * Resets the data source to its initial empty condition.
     */
    public void reset() {
        commentMap.clear();
    }

    @Override
    public int delete(String selection, String[] selectionArgs) {
        throw new UnsupportedOperationException("Not yet implemented!");
    }

    @Override
    public Comment createComment(String recipient, String content) {
        Comment comment = new Comment(commentId++, recipient, content);
        commentMap.put(recipient, comment);
        return comment;
    }

    @Override
    public Cursor getCursorForCommentForRecipient(String recipient, final String[] projection) {
        final Comment comment = commentMap.get(recipient);
        return new MockCursor() {

            @Override
            public void close() {
                // Do nothing.
            }

            @Override
            public int getCount() {
                return comment == null ? 0 : 1;
            }

            @Override
            public String getString(int columnIndex) {
                String propertyToGet = projection[columnIndex];
                if (propertyToGet.equals("recipient")) {
                    return comment.getRecipient();
                }
                if (propertyToGet.equals("content")) {
                    return comment.getContent();
                }
                // TODO: is this the right kind of exception to throw???
                throw new IllegalArgumentException("Desired column is not a string!");
            }

            @Override
            public boolean moveToFirst() {
                return comment == null ? false : true;
            }

            @Override
            public boolean moveToNext() {
                return false;
            }
        };
    }

    @Override
    public Cursor getCursorForAllComments(String[] projection) {
        throw new UnsupportedOperationException("Not yet implemented!");
    }

    @Override
    public List<Comment> getAllComments() {
        Collection<Comment> comments = commentMap.values();
        List<Comment> commentList = new ArrayList<Comment>(comments.size());
        commentList.addAll(comments);
        return commentList;
    }

    @Override
    public Comment getCommentForRecipient(String recipient) {
        return commentMap.get(recipient);
    }

    @Override
    public boolean deleteCommentForRecipient(String recipient) {
        return commentMap.remove(recipient) != null;
    }
}
