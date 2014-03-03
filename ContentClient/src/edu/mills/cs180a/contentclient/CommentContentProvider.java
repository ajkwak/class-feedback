package edu.mills.cs180a.contentclient;

import android.net.Uri;

/**
 * A copy of needed constants defined in 
 * {@link edu.mills.cs180a.classfeedback.CommentContentProvider}.  This duplication
 * is to avoid the inconvenience of exporting them through a jar file.  For more
 * information, see <a href="http://stackoverflow.com/questions/21978082">How
 * to share public URIs for ContentProvider</a>.
 * 
 * @author ellen.spertus@gmail.com (Ellen Spertus)
 */
public class CommentContentProvider {
    // Copied from the content provider.
    public static final String AUTHORITY = "edu.mills.cs180a.classfeedback";
    public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY
            + "/comments");

}
