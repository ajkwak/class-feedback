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

    private MockCommentsDataSource(Context context) {
        super(context, null);
    }

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

    @Override
    public Comment createComment(String recipient, String content) {
        Comment comment = new Comment(commentId++, recipient, content);
        commentMap.put(recipient, comment);
        return comment;
    }

    @Override
    public Cursor getCursorForCommentForRecipient(String recipient, String[] projection) {
        return new MockCursor() {


            @Override
            public void close() {
                // Do nothing.
            }

            @Override
            public int getCount() {
                throw new UnsupportedOperationException("Not Yet Implemented!");
            }

            @Override
            public String getString(int columnIndex) {
                throw new UnsupportedOperationException("Not Yet Implemented!");
            }

            @Override
            public boolean moveToFirst() {
                throw new UnsupportedOperationException("Not Yet Implemented!");
            }

            @Override
            public boolean moveToNext() {
                throw new UnsupportedOperationException("Not Yet Implemented!");
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
