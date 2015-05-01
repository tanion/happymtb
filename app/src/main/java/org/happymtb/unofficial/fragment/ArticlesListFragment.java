package org.happymtb.unofficial.fragment;

import java.util.List;

import org.happymtb.unofficial.R;
import org.happymtb.unofficial.item.Item;
import org.happymtb.unofficial.listener.ItemListListener;
import org.happymtb.unofficial.task.ArticlesListTask;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.ExpandableListView.OnChildClickListener;

public class ArticlesListFragment extends ItemsListFragment implements DialogInterface.OnCancelListener, OnChildClickListener {
	private ArticlesListTask mGetItems;
	
	@Override
	protected void fetchItems() {
		if ((mProgressDialog == null) || (!mProgressDialog.isShowing())) {
			mProgressDialog = ProgressDialog.show(getActivity(), "", "", true, true);
			mProgressDialog.setContentView(R.layout.progress_layout);
			mProgressDialog.setOnCancelListener(this);
		}

		mGetItems = new ArticlesListTask();
		mGetItems.addItemListListener(new ItemListListener() {
            public void success(List<Item> Items) {
                if (getActivity() != null) {
                    mAllItems = Items;
                    fillList();
                    mProgressDialog.dismiss();
                }
            }

            public void fail() {
                if (getActivity() != null) {
                    mProgressDialog.dismiss();
                }
            }
        });

		mGetItems.execute();
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case R.id.article_submenu:
				return true;			
			case R.id.article_expand_all:
				ExpandeAll();
				return true;	
			case R.id.article_hide_all:
				CollapseAll();
				return true;				
		}
		return super.onOptionsItemSelected(item);
	}	
		
	
	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		menu.clear();
		inflater.inflate(R.menu.articles_menu, menu);
		super.onCreateOptionsMenu(menu, inflater);
	}	
}