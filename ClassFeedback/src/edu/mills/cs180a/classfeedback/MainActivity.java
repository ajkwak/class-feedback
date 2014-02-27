
package edu.mills.cs180a.classfeedback;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

/**
 * An {@code Activity} that displays a list of the names of {@link Person people in CS 180A}.
 * If a name is clicked on, a {@link CommentActivity} is opened, soliciting a
 * comment for the selected person.
 *
 * @author ellen.spertus@gmail.com (Ellen Spertus)
 */
public class MainActivity extends Activity {
    private LayoutInflater mInflater;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Populate a list from Person.everyone.
        ArrayAdapter<Person> adapter = new PersonArrayAdapter();
        ListView listView = (ListView) findViewById(R.id.listView1);
        listView.setAdapter(adapter);

        // Initialize mInflater, which is needed in PersonArrayAdapter.getView().
        mInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // Determine whether the comment was added or canceled.
        String toastText = "";
        if (resultCode == RESULT_OK) {
            toastText = getString(R.string.comment_added_toast);
        } else if (resultCode == RESULT_CANCELED) {
            toastText = getString(R.string.comment_canceled_toast);
        }

        // Add the name of the person for whom the comment was added or canceled.
        if (data != null) {
            int recipientId = data.getIntExtra(CommentActivity.RECIPIENT, -1);
            assert (recipientId >= 0 && recipientId < Person.everyone.length);
            toastText = String.format(toastText, Person.everyone[recipientId].toString());
        } else {
            toastText = String.format(toastText, getString(R.string.unknown_name_text));
        }

        Toast.makeText(this, toastText, Toast.LENGTH_SHORT).show();
    }

    private class OnItemClickListener implements OnClickListener{
        private int mPosition;
        OnItemClickListener(int position){
            mPosition = position;
        }
        @Override
        public void onClick(View arg0) {
            Intent i = new Intent(MainActivity.this, CommentActivity.class);
            i.putExtra(CommentActivity.RECIPIENT, mPosition);
            startActivityForResult(i, mPosition);
        }
    }

    private class PersonArrayAdapter extends ArrayAdapter<Person> {
        PersonArrayAdapter() {
            super(MainActivity.this, R.layout.row, R.id.rowTextView, Person.everyone);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            // Handling click events from a row inside a ListView gets very strange.
            // Solution found at "http://stackoverflow.com/questions/1821871".
            if (null == convertView) {
                convertView = mInflater.inflate(R.layout.row, null);
            }
            Button button = (Button) convertView.findViewById(R.id.rowButtonView);
            button.setOnClickListener(new OnItemClickListener(position));
            Person person = getItem(position);
            ImageView icon = (ImageView) convertView.findViewById(R.id.rowImageView);
            icon.setImageResource(person.getImageId());
            TextView name = (TextView) convertView.findViewById(R.id.rowTextView);
            name.setText(person.getFirstName());
            return convertView;
        }
    }
}
