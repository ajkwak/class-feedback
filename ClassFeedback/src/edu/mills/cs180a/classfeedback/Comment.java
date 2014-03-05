/**
 *
 */
package edu.mills.cs180a.classfeedback;

/**
 * A comment meant for a classmate.
 *
 * @author ellen.spertus@gmail.com (Ellen Spertus)
 */
public class Comment {
    private long mId;
    private String mRecipient;
    private String mContent;

    public Comment(long id, String recipient, String content) {
        mId = id;
        mRecipient = recipient;
        mContent = content;
    }

    /**
     * Gets the email address of the recipient.
     *
     * @return the email address of the recipient
     */
    String getRecipient() {
        return mRecipient;
    }

    /**
     * Sets the email address of the recipient.
     *
     * @param recipient the email address of the recipient
     */
    void setRecipient(String recipient) {
        mRecipient = recipient;
    }

    /**
     * Gets the content of the comment.
     *
     * @return the content
     */
    String getContent() {
        return mContent;
    }

    /**
     * Sets the content of the comment.
     *
     * @param content the content
     */
    void setContent(String content) {
        mContent = content;
    }

    /**
     * Gets the unique id of this comment.
     *
     * @return the id
     */
    long getId() {
        return mId;
    }
}
