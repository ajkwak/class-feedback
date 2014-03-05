package edu.mills.cs180a.classfeedback.test;

import android.content.Context;
import edu.mills.cs180a.classfeedback.CommentsDataSource;
import edu.mills.cs180a.classfeedback.CommentsDataSourceAbstractFactory;

/**
 * A factory for {@link MockCommentsDataSource}.  This supports mocking of
 * {@link CommentsDataSource}.
 */
public class MockCommentsDataSourceFactory implements CommentsDataSourceAbstractFactory {
    MockCommentsDataSourceFactory() {
    }

    public CommentsDataSource createCommentsDataSource(Context context) {
        return MockCommentsDataSource.create(context);    
    }
}
