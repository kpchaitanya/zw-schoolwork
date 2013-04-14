package edu.gatech.oad.rocket.findmythings;

import android.content.Intent;
import android.content.Loader;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import com.google.api.services.fmthings.EndpointUtils;
import com.google.api.services.fmthings.Fmthings;
import com.google.api.services.fmthings.model.CollectionResponseDBItem;
import com.google.api.services.fmthings.model.DBItem;
import edu.gatech.oad.rocket.findmythings.list.AlternatingTwoLineListAdapter;
import edu.gatech.oad.rocket.findmythings.list.ArrayListFragment;
import edu.gatech.oad.rocket.findmythings.list.ThrowableLoader;
import edu.gatech.oad.rocket.findmythings.model.Type;

import java.util.List;

public class ItemListFragment extends ArrayListFragment<DBItem> {

	public static final int LOAD_LIMIT = 25;

	private boolean isAll = true;
	private Type queriedType = Type.LOST;

	private String lastNextPageToken;

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		this.setEmptyText(R.string.no_items_found);
	}

	@Override
	public Loader<List<DBItem>> onCreateLoader(int id, Bundle args) {
		// TODO Auto-generated method stub
		return new ThrowableLoader<List<DBItem>>(getActivity()){

			@Override
			public List<DBItem> loadData() throws Exception {
				// limit, type, cursor
				Fmthings.Items.List query = EndpointUtils.getEndpoint().items().list();

				query.setLimit(LOAD_LIMIT);
				if (!isAll) query.setType(queriedType.toString());
				if (lastNextPageToken != null) query.setCursor(lastNextPageToken);

				CollectionResponseDBItem result = query.execute();
				lastNextPageToken = result.getNextPageToken();
				return result.getItems();
			}
		};
	}

	@Override
	protected ArrayAdapter<DBItem> onCreateAdapter() {
		return new AlternatingTwoLineListAdapter<DBItem>(getActivity());
	}

	@Override
	protected int getErrorMessage(Exception exception) {
		return R.string.error_loading_items;
	}

    public void onListItemClick(ListView l, View v, int position, long id) {
        DBItem item = ((DBItem) l.getItemAtPosition(position));
		startActivity(new Intent(getActivity(), ItemDetailActivity.class).putExtra(ItemDetailActivity.ITEM_EXTRA, item));
        getActivity().overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_left);
    }

}
