package edu.mills.cs180a.classfeedback;

import java.io.Serializable;

import android.content.Context;

/**
 * An abstract factory for constructing instances of
 * {@link CommentsDataSource} or any subclasses of it, 
 * such as mocks for testing.
 */
public interface CommentsDataSourceAbstractFactory extends Serializable {
    abstract CommentsDataSource createCommentsDataSource(Context context);
}
