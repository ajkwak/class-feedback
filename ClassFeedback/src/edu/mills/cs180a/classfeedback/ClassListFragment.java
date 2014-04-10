package edu.mills.cs180a.classfeedback;

import android.app.ListFragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * An {@code ListFragment} that displays a list of the names of {@link Person people in CS 180A}.
 * When the 'Comment' button beside a person's name is clicked, calls the
 * {@link MainActivity#onPersonSelected(int)} method.
 *
 * @author ajkwak@users.noreply.github.com (AJ Parmidge)
 */
public class ClassListFragment extends ListFragment {
    private LayoutInflater mInflater;

    /**
     * Interface definition for a callback to be invoked when a {@link Person} is selected in the
     * list view.
     *
     * @author ajkwak@users.noreply.github.com (AJ Parmidge)
     */
    interface OnPersonSelectedListener {

        /**
         * Called when a {@code Person} is selected
         *
         * @param personId the ID of the selected person
         */
        void onPersonSelected(int personId);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        // Save mInflater, which is needed in PersonArrayAdapter.getView().
        mInflater = inflater;

        // Set up the adapter.
        ArrayAdapter<Person> adapter = new PersonArrayAdapter();
        setListAdapter(adapter);
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    private class OnItemClickListener implements OnClickListener {
        private int mPosition;

        private OnItemClickListener(int position) {
            mPosition = position;
        }

        @Override
        public void onClick(View arg0) {
            OnPersonSelectedListener listener = (OnPersonSelectedListener) getActivity();
            listener.onPersonSelected(mPosition);
        }
    }

    private class PersonArrayAdapter extends ArrayAdapter<Person> {

        private PersonArrayAdapter() {
            super(getActivity(), R.layout.fragment_my_list_row, R.id.rowTextView, Person.everyone);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            // Handling click events from a row inside a ListView gets very strange.
            // Solution found at "http://stackoverflow.com/questions/1821871".
            if (null == convertView) {
                convertView = mInflater.inflate(R.layout.fragment_my_list_row, null);
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
