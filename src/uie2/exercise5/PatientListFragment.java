package uie2.exercise5;

import android.app.Activity;
import android.app.ListFragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

public class PatientListFragment extends ListFragment {

	private OnPatientSelectedListener mListener;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		return inflater.inflate(R.layout.patient_list, container, false);
	}

	@Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (OnPatientSelectedListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement OnArticleSelectedListener");
        }
    }

    public interface OnPatientSelectedListener {
        public void onPatientSelected(long patientId);
    }
	
    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        mListener.onPatientSelected(id);
    }
}
