
package edu.mills.cs180a.classfeedback;
import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.widget.Toast;

/**
 * An {@code Activity} that displays a list of the names of {@link Person people in CS 180A}. If a
 * name is clicked, then the activity displays a {@link CommentFragment}, which solicits a
 * {@link Comment} about the specified {@link Person}. In the {@link CommentFragment}, the user is
 * given the choice of saving, canceling, deleting, or mailing the comment. The user can also clear
 * the comment field with or without saving their changes.
 *
 * @author ellen.spertus@gmail.com (Ellen Spertus)
 * @author ajkwak@users.noreply.github.com (AJ Parmidge)
 */
public class MainActivity extends Activity implements ClassListFragment.OnPersonSelectedListener {
    private static final String TAG = "MainActivity";
    private static final int MIN_MULTIPANE_WIDTH = 700;
    private static final String KEY_PERSON_ID = "person id";
    private FragmentManager fragmentManager;
    private Fragment classListFragment, commentFragment;
    private boolean multiPane;
    private int selectedPersonId = -1; // Initialize to invalid value.

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Get references to fragment manager and fragments.
        fragmentManager = getFragmentManager();
        classListFragment = fragmentManager.findFragmentById(R.id.classListFragment);
        commentFragment = fragmentManager.findFragmentById(R.id.commentFragment);

        // Determine whether to use single or multiple panes.
        DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
        float screenWidthDp = displayMetrics.widthPixels / displayMetrics.density;
        float screenHeightDp = displayMetrics.heightPixels / displayMetrics.density;
        Log.d(TAG, "screenWidthDp: " + screenWidthDp);
        Log.d(TAG, "screenHeightDp: " + screenHeightDp);
        multiPane = screenWidthDp >= MIN_MULTIPANE_WIDTH;

        // Determine whether the user has selected a person to display.
        if (savedInstanceState != null) {
            selectedPersonId = savedInstanceState.getInt(KEY_PERSON_ID, -1);
        }

        if (selectedPersonId > -1) {
            // Then display the selected person.
            onPersonSelected(selectedPersonId);
        } else {
            fragmentManager.beginTransaction().hide(commentFragment).commit();
        }
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
        if (!commentFragment.isHidden()) {
            savedInstanceState.putInt(KEY_PERSON_ID, selectedPersonId);
        }
    }

    @Override
    public void onPersonSelected(int personId) {
        selectedPersonId = personId;

        // If we're in multi-pane mode, show the comment pane if it isn't already visible.
        if (multiPane && commentFragment.isHidden()) {
            fragmentManager
                    .beginTransaction()
                    .show(commentFragment)
                    .addToBackStack(null)
                    .commit();
        }
        // If we're in single-pane mode, show the comment panel and hide the class list.
        else if (!multiPane) {
            fragmentManager
                    .beginTransaction()
                    .show(commentFragment)
                    .hide(classListFragment)
                    .addToBackStack(null)
                    .commit();
        }
        // Show the current person.
        ((CommentFragment) commentFragment).setRecipient(personId);
    }

    /**
     * Hides the {@link CommentFragment} when the user is finished saving, deleting, or mailing the
     * comment. Upon exiting the comment fragment, displays the given text as a {@link Toast} and
     * pops the last item off the back stack.
     *
     * @param message the message to display upon hiding (returning from) the
     *        {@link CommentFragment}
     */
    void hideCommentFragment(String message) {
        fragmentManager.beginTransaction().show(classListFragment).hide(commentFragment).commit();
        fragmentManager.popBackStack();
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}
