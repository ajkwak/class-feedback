
package edu.mills.cs180a.classfeedback;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

/**
 * An {@code Activity} that solicits a {@link Comment} about the specified {@link Person}.
 * The recipient of the comment is specified as an index into {@link Person#everyone}
 * and is communicated via the key {@link #RECIPIENT} in the {@link android.intent.Intent}.
 *
 * <P>The user is given the choice of saving or canceling the comment.  If saved,
 * it is added to the database, and the result code {@link Activity#RESULT_OK} is
 * provided to the parent activity.  Otherwise, the database is not modified, and
 * the result code {@link Activity#RESULT_CANCELED} is provided.
 *
 * @author ellen.spertus@gmail.com (Ellen Spertus)
 */
public class CommentActivity extends Activity {
    static final String RECIPIENT = "COMMENT_RECIPIENT";
    private int recipient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comment);

        // Show a picture of the recipient.
        recipient = getIntent().getIntExtra(RECIPIENT, -1);
        assert(recipient >= 0 && recipient < Person.everyone.length);
        Person person = Person.everyone[recipient];
        ImageView icon = (ImageView) findViewById(R.id.commentImageView);
        icon.setImageResource(person.getImageId());

        // Get a connection to the database.
        final CommentsDataSource cds = new CommentsDataSource(this);
        cds.open();

        // Add listeners.
        Button saveButton = (Button) findViewById(R.id.saveCommentButton);
        saveButton.setOnClickListener(new OnClickListener(){
            @Override
            public void onClick(View view) {
                EditText commentField = (EditText) findViewById(R.id.commentEditText);
                cds.createComment(Person.everyone[recipient].getEmail(),
                        commentField.getText().toString());
                setResult(RESULT_OK);
                finish();
            }
        });
        Button cancelButton = (Button) findViewById(R.id.cancelCommentButton);
        cancelButton.setOnClickListener(new OnClickListener(){
            @Override
            public void onClick(View view) {
                setResult(RESULT_CANCELED);
                finish();
            }
        });
        Button clearTextButton = (Button) findViewById(R.id.clearTextButton);
        clearTextButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {
                EditText commentEditText = (EditText) findViewById(R.id.commentEditText);
                commentEditText.setText("");
            }
        });
    }
}
