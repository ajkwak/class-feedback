package edu.mills.cs180a.classfeedback;

import android.content.Context;

/**
 * A factory for {@link CommentsDataSource}.
 */
public class CommentsDataSourceFactory implements CommentsDataSourceAbstractFactory {
    CommentsDataSourceFactory() {
    }

    public CommentsDataSource createCommentsDataSource(Context context) {
        return CommentsDataSource.create(context);    
    }
}
