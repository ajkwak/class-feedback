package edu.mills.cs180a.classfeedback.test;

import android.content.Context;
import edu.mills.cs180a.classfeedback.CommentsDataSource;

public class MockCommentsDataSource extends CommentsDataSource {
    private static MockCommentsDataSource instance;
    
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
    public void close() {      
    }
}
