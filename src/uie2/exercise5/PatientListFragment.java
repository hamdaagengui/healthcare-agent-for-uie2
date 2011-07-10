package uie2.exercise5;

import android.app.Activity;
import android.app.ListFragment;
import android.database.Cursor;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ContextMenu.ContextMenuInfo;
import android.widget.Adapter;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.AdapterView.AdapterContextMenuInfo;

public class PatientListFragment extends ListFragment {

	private OnPatientSelectedListener mListener;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.patient_list, container, false);

        registerForContextMenu(view.findViewById(android.R.id.list));

		return view;
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		try {
			mListener = (OnPatientSelectedListener) activity;
		} catch (ClassCastException e) {
			throw new ClassCastException(activity.toString()
					+ " must implement OnArticleSelectedListener");
		}
	}

	public interface OnPatientSelectedListener {
		public void onPatientSelected(long patientId);
	}

	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		mListener.onPatientSelected(id);
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);
		if (v.getId() == android.R.id.list) {
			MenuInflater inflater = getActivity().getMenuInflater();
			AdapterContextMenuInfo info = (AdapterContextMenuInfo) menuInfo;
			Adapter adapter = ((ListFragment) getFragmentManager()
					.findFragmentById(R.id.PatientListFragment))
					.getListAdapter();
			long id = adapter.getItemId(info.position);
			Cursor c = getActivity().getContentResolver().query(
					MyContentProvider.PATIENT_URI,
					new String[] { "lastname", "firstname" }, "_id==" + id,
					null, null);
			c.moveToFirst();
			menu.setHeaderTitle(c.getString(0) + ", " + c.getString(1));
			inflater.inflate(R.menu.context_menu, menu);
		}
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		AdapterContextMenuInfo info = (AdapterContextMenuInfo) item
				.getMenuInfo();
		switch (item.getItemId()) {
		case R.id.delete_patient:
			long id = ((ListFragment) getFragmentManager().findFragmentById(
					R.id.PatientListFragment)).getListAdapter().getItemId(
					info.position);
			getActivity().getContentResolver().delete(
					MyContentProvider.PATIENT_URI, "_id=" + id, null);
			getActivity().getContentResolver().notifyChange(MyContentProvider.PATIENT_URI, null);
			Cursor cursor = getActivity().managedQuery(MyContentProvider.PATIENT_URI,
					new String[] { "_id", "firstname", "lastname", "dateofbirth" },
					null, null, " lastname ASC, firstname ASC");
			SimpleCursorAdapter adapter = new SimpleCursorAdapter(getActivity(),
					R.layout.patient_item, cursor, new String[] { "lastname",
							"firstname", "dateofbirth" }, new int[] {
							R.id.textViewName, R.id.textViewFirstname,
							R.id.textViewDateofbirth });
			setListAdapter(adapter);
			return true;
		default:
			return super.onContextItemSelected(item);
		}
	}
}
