package org.happymtb.unofficial.fragment;

import java.util.List;

import org.droidparts.widget.ClearableEditText;
import org.happymtb.unofficial.KoSObjectActivity;
import org.happymtb.unofficial.MainActivity;
import org.happymtb.unofficial.R;
import org.happymtb.unofficial.adapter.ListKoSAdapter;
import org.happymtb.unofficial.analytics.GaConstants;
import org.happymtb.unofficial.analytics.HappyApplication;
import org.happymtb.unofficial.helpers.HappyUtils;
import org.happymtb.unofficial.item.KoSData;
import org.happymtb.unofficial.item.KoSListItem;
import org.happymtb.unofficial.listener.KoSListListener;
import org.happymtb.unofficial.listener.PageTextWatcher;
import org.happymtb.unofficial.task.KoSListTask;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.GestureDetectorCompat;
import android.text.Editable;
import android.text.InputType;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu;

public class KoSListFragment extends RefreshListfragment implements DialogInterface.OnCancelListener,
		MainActivity.SortListener, View.OnClickListener {
    public static String TAG = "kos_frag";

	public final static String SORT_ORDER_POS = "sort_order_pos";
	public final static String SORT_ORDER_SERVER = "sort_order";
	public final static String SORT_ATTRIBUTE_POS = "sort_attribute_pos";
	public final static String SORT_ATTRIBUTE_SERVER = "sort_attribute";

    public final static String SEARCH_TEXT = "search_text";

    public final static String SEARCH_CATEGORY = "search_category";
    public final static String SEARCH_CATEGORY_POS = "search_category_pos";

    public final static String SEARCH_PRICE = "search_price";
    public final static String SEARCH_PRICE_POS = "search_price_pos";

    public final static String SEARCH_YEAR = "search_year";
    public final static String SEARCH_YEAR_POS= "search_year_pos";

    public final static String SEARCH_REGION = "search_region";
    public final static String SEARCH_REGION_POS = "search_region_pos";

    public final static String SEARCH_TYPE_POS = "search_type";

    public final static String SEARCH_TYPE_SPINNER = "search_type_spinner";
    public final static String SEARCH_REGION_SPINNER = "search_region_spinner";
    public final static String SEARCH_CATEGORY_SPINNER = "search_category_spinner";
    public final static String SEARCH_PRICE_SPINNER = "search_price_spinner";
    public final static String SEARCH_YEAR_SPINNER = "search_year_spinner";

	private Tracker mTracker;

	private KoSListTask mKoSTask;
	private ListKoSAdapter mKoSAdapter;
	private KoSData mKoSData;
	private SharedPreferences mPreferences;
	private MainActivity mActivity;
	private FragmentManager fragmentManager;

	private ImageButton prevPageButton;
	private ImageButton nextPageImageButton;

    private ClearableEditText searchEditText;
    private Spinner searchCategory;
    private Spinner searchRegion;
    private Spinner searchType;
    private Spinner searchPrice;
    private Spinner searchYear;


    private Button mClearSearchButton;

    private SlidingMenu mSlidingMenu;

    protected GestureDetectorCompat mDetector;


    /** Called when the activity is first created. */
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

        mActivity = (MainActivity) getActivity();
		mActivity.getSupportActionBar().setTitle("Annonser");

        mPreferences = PreferenceManager.getDefaultSharedPreferences(mActivity);

        prevPageButton = (ImageButton) mActivity.findViewById(R.id.kos_prev);
        prevPageButton.setOnClickListener(this);

        nextPageImageButton = (ImageButton) mActivity.findViewById(R.id.kos_next);
        nextPageImageButton.setOnClickListener(this);

        if (savedInstanceState != null) {
			// Restore Current page.
            mKoSData = (KoSData) savedInstanceState.getSerializable(DATA);

            fillList();
            showProgress(false);
		} else {
            mKoSData = new KoSData(
                    mPreferences.getString(SORT_ATTRIBUTE_SERVER, "creationdate"), mPreferences.getInt(SORT_ATTRIBUTE_POS, 0),
                    mPreferences.getString(SORT_ORDER_SERVER, "DESC"), mPreferences.getInt(SORT_ORDER_POS, 0),
                    mPreferences.getInt(SEARCH_TYPE_POS, KoSData.ALLA),
                    mPreferences.getInt(SEARCH_REGION_POS, 0), mPreferences.getString(SEARCH_REGION, getString(R.string.all_regions)),
                    mPreferences.getInt(SEARCH_CATEGORY_POS, 0), mPreferences.getString(SEARCH_CATEGORY, getString(R.string.all_categories)),
                    mPreferences.getInt(SEARCH_PRICE_POS, 0), mPreferences.getString(SEARCH_PRICE, getString(R.string.all_prices)),
                    mPreferences.getInt(SEARCH_YEAR_POS, 0), mPreferences.getString(SEARCH_YEAR, getString(R.string.all_years)),
                    mPreferences.getString(SEARCH_TEXT, ""));
            fetchData();

        }

        mActivity.findViewById(R.id.kos_bottombar).setOnClickListener(this);

        setupSearchSlidingMenu();

	}

    private void setupSearchSlidingMenu() {
        mSlidingMenu = mActivity.getKosSlidingMenu();

        if (mSlidingMenu == null) {
            mSlidingMenu = new SlidingMenu(mActivity);
            mSlidingMenu.setMode(SlidingMenu.RIGHT);
            mSlidingMenu.setTouchModeAbove(SlidingMenu.TOUCHMODE_MARGIN);
            mSlidingMenu.setShadowWidthRes(R.dimen.slidingmenu_shadow_width);
            mSlidingMenu.setShadowDrawable(R.drawable.slidemenu_shadow);
//          mSlidingMenu.setBehindScrollScale(R.dimen.slidingmenu_behind_scroll_scale);
            mSlidingMenu.setBehindOffsetRes(R.dimen.slidingmenu_offset);
            mSlidingMenu.setFadeDegree(0.85f);
            mSlidingMenu.setBehindWidthRes(R.dimen.slidingmenu_width);
            mSlidingMenu.setMenu(R.layout.kos_search_slide);mSlidingMenu.setOnOpenedListener(new SlidingMenu.OnOpenedListener() {
                @Override
                public void onOpened() {
                    // [START Google analytics screen]
                    mTracker.setScreenName(GaConstants.Categories.KOS_SORT_MENU);
                    mTracker.send(new HitBuilders.ScreenViewBuilder().build());
                    // [END Google analytics screen]
                }
            });

            mSlidingMenu.attachToActivity(mActivity, SlidingMenu.SLIDING_CONTENT);

            mActivity.setKosSlidingMenu(mSlidingMenu);
        } else {
            // TODO remove old listeners before adding new ones?
        }

        searchEditText = (ClearableEditText) mActivity.findViewById(R.id.kos_dialog_search_text);
        searchCategory = (Spinner) mActivity.findViewById(R.id.kos_dialog_search_category);
        searchRegion = (Spinner) mActivity.findViewById(R.id.kos_dialog_search_region);
        searchType = (Spinner) mActivity.findViewById(R.id.kos_dialog_search_type);
        searchPrice = (Spinner) mActivity.findViewById(R.id.kos_dialog_search_price);
        searchYear = (Spinner) mActivity.findViewById(R.id.kos_dialog_search_year);

        mClearSearchButton = (Button) mActivity.findViewById(R.id.kos_dialog_search_clear_all);

        searchEditText.setText(mPreferences.getString(KoSListFragment.SEARCH_TEXT, ""));
        searchCategory.setSelection(mPreferences.getInt(KoSListFragment.SEARCH_CATEGORY_SPINNER, 0), false);
        searchRegion.setSelection(mPreferences.getInt(KoSListFragment.SEARCH_REGION_SPINNER, 0), false);
        searchType.setSelection(mPreferences.getInt(KoSListFragment.SEARCH_TYPE_SPINNER, 0), false);
        searchPrice.setSelection(mPreferences.getInt(KoSListFragment.SEARCH_PRICE_SPINNER, 0), false);
        searchYear.setSelection(mPreferences.getInt(KoSListFragment.SEARCH_YEAR_SPINNER, 0), false);

        updateClearAllButton();

        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                updateClearAllButton();
                updateSearchParams();
            }
        });

        AdapterView.OnItemSelectedListener selectedListener = new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                updateClearAllButton();
                updateSearchParams();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        };

        searchCategory.setOnItemSelectedListener(selectedListener);
        searchRegion.setOnItemSelectedListener(selectedListener);
        searchType.setOnItemSelectedListener(selectedListener);
        searchPrice.setOnItemSelectedListener(selectedListener);
        searchYear.setOnItemSelectedListener(selectedListener);

        mClearSearchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                searchEditText.setText("");
                searchCategory.setSelection(0, false);
                searchRegion.setSelection(0, false);
                searchType.setSelection(0, false);
                searchPrice.setSelection(0, false);
                searchYear.setSelection(0, false);

                updateClearAllButton();
            }
        });
    }

    private void updateClearAllButton() {
        if (TextUtils.isEmpty(searchEditText.getText().toString().trim())
                && searchCategory.getSelectedItemPosition() == 0
                && searchRegion.getSelectedItemPosition() == 0
                && searchType.getSelectedItemPosition() == 0
                && searchPrice.getSelectedItemPosition() == 0
                && searchYear.getSelectedItemPosition() == 0) {
            mClearSearchButton.setEnabled(false);
            mClearSearchButton.setTextColor(getResources().getColor(R.color.grey_light));
        } else {
            mClearSearchButton.setTextColor(getResources().getColor(R.color.colorPrimary));
            mClearSearchButton.setEnabled(true);
        }
    }

	public void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// Obtain the shared Tracker instance.
		HappyApplication application = (HappyApplication) getActivity().getApplication();
		mTracker = application.getDefaultTracker();

		// [START Google analytics screen]
		mTracker.setScreenName(GaConstants.Categories.KOS_LIST);
		mTracker.send(new HitBuilders.ScreenViewBuilder().build());
		// [END Google analytics screen]

	}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.kos_frame, container, false);
    }

	@Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (mKoSData != null) {
            outState.putSerializable(DATA, mKoSData);
        }
    }

    @Override
    public void onAttach(Context context) {
		super.onAttach(context);
        ((MainActivity) getActivity()).addSortListener(this);
	}

    @Override
    public void onDetach() {
        super.onDetach();
        ((MainActivity) getActivity()).removeSortListener(this);
    }

    @Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		menu.clear();		
		inflater.inflate(R.menu.kos_menu, menu);

		// Dont show left arrow for first page
//		menu.findItem(R.id.kos_left).setVisible(mKoSData.getCurrentPage() > 1);
//		menu.findItem(R.id.kos_right).setVisible(mKoSData.getCurrentPage() < mKoSData.getMaxPages());
		super.onCreateOptionsMenu(menu, inflater);
	}		

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.kos_submenu:
			return true;		
		case R.id.kos_left:
			previousPage();
            sendGaEvent(GaConstants.Actions.PREV_PAGE, GaConstants.Labels.EMPTY);
			return true;
		case R.id.kos_right:
			nextPage();
            sendGaEvent(GaConstants.Actions.NEXT_PAGE, GaConstants.Labels.EMPTY);
			return true;
		case R.id.kos_sort:
			fragmentManager = mActivity.getSupportFragmentManager();
	        KoSSortDialogFragment koSSortDialog = new KoSSortDialogFragment();
	        koSSortDialog.show(fragmentManager, "kos_sort_dialog");
			return true;						
		case R.id.kos_search_option:
            mSlidingMenu.toggle();
			return true;
		case R.id.kos_go_to_page:
            openGoToPage();
			return true;	
		case R.id.kos_new_item:
			String url = "http://happyride.se/annonser/add.php";
            sendGaEvent(GaConstants.Actions.NEW_ADD, GaConstants.Labels.EMPTY);
			Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
			startActivity(browserIntent);							
			return true;			
		}
		return super.onOptionsItemSelected(item);
	}

    private void openGoToPage() {
        AlertDialog.Builder alert = new AlertDialog.Builder(mActivity);
        alert.setTitle(R.string.goto_page);
        alert.setMessage(getString(R.string.enter_page_number, mKoSData.getMaxPages()));

        // Set an EditText view to get user input
        final EditText input = new EditText(mActivity);
        input.setInputType(InputType.TYPE_CLASS_NUMBER);
        alert.setView(input);
        alert.setPositiveButton(R.string.open, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                // Do something with value!
                if (HappyUtils.isInteger(input.getText().toString())) {
                    // [START Google analytics screen]
                    mTracker.setScreenName(GaConstants.Categories.KOS_GOTO_DIALOG);
                    mTracker.send(new HitBuilders.ScreenViewBuilder().build());
                    // [END Google analytics screen]
                    mKoSData.setCurrentPage(Integer.parseInt(input.getText().toString()));
                    fetchData();
                }
            }
        });

        alert.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                // Canceled.
            }
        });
        final AlertDialog dialog = alert.create();

        input.addTextChangedListener(new PageTextWatcher(dialog, mKoSData.getMaxPages()));

        dialog.show();
    }

    public void reloadCleanList() {
        mKoSData.setCurrentPage(1);
        mKoSAdapter = null;
		fetchData();
	}

    @Override
    protected void fetchData() {
		if (hasNetworkConnection()) {
            mKoSTask = new KoSListTask();
            mKoSTask.addKoSListListener(new KoSListListener() {
                public void success(List<KoSListItem> koSListItems) {
                    if (getActivity() != null && !getActivity().isFinishing()) {
                        mKoSData.setKoSItems(koSListItems);
                        fillList();
                    }
                    showList(true);
                    showProgress(false);
                }

                public void fail() {
                    if (getActivity() != null && !getActivity().isFinishing()) {
                        Toast.makeText(mActivity, mActivity.getString(R.string.kos_no_items_found), Toast.LENGTH_LONG).show();

                        showProgress(false);
                    }
                }
            });
            String year =  mKoSData.getYear() == 0? "0" :  mKoSData.getYearStr();
            mKoSTask.execute(mKoSData.getSearchString(), mKoSData.getCategory(), mKoSData.getRegion(), mKoSData.getType(),
                    "" /*category2*/, ""/*county2*/, ""/*type2*/, mKoSData.getPrice(), year, mKoSData.getCurrentPage(),
                    mKoSData.getSortAttributeServer(), mKoSData.getSortOrderServer());

            //?search=&category=1&county=&type=1&category2=&county2=&type2=&price=3&year=2013&p=1&sortattribute=creationdate&sortorder=DESC
        }
	}

	private void fillList() {
        if (mKoSData.getKoSItems() == null || mKoSData.getKoSItems().isEmpty()) {
            // Workaround for orientation changes before finish loading
            showProgress(true);
            fetchData();

            return;
        } else if (mKoSAdapter == null) {
            mKoSAdapter = new ListKoSAdapter(mActivity, mKoSData.getKoSItems());
			setListAdapter(mKoSAdapter);
		} else {
            mKoSAdapter.setItems(mKoSData.getKoSItems());
			mKoSAdapter.notifyDataSetChanged();
		}

        if (mActivity.findViewById(R.id.kos_bottombar) != null) {
            TextView currentPage = (TextView) mActivity.findViewById(R.id.kos_current_page);
            currentPage.setText(Integer.toString(mKoSData.getCurrentPage()));

            TextView maxPages = (TextView) mActivity.findViewById(R.id.kos_no_of_pages);
            maxPages.setText(Integer.toString(mKoSData.getKoSItems().get(0).getNumberOfKoSPages()));
            mKoSData.setMaxPages(mKoSData.getKoSItems().get(0).getNumberOfKoSPages());

            TextView category = (TextView) mActivity.findViewById(R.id.kos_category);
            category.setText("Kategori: " + mKoSData.getCategoryStr());

            TextView region = (TextView) mActivity.findViewById(R.id.kos_region);
            region.setText("Region: " + mKoSData.getRegionStr());

            TextView search = (TextView) mActivity.findViewById(R.id.kos_search);

            String searchString = mKoSData.getSearchString();

            if (searchString.length() > 0) {
                search.setVisibility(View.VISIBLE);
                search.setText("Sökord: " + searchString);
            } else {
                search.setVisibility(View.GONE);
                search.setText("");
            }

//		TextView sortView = (TextView) mActivity.findViewById(R.id.kos_sort);
//		sortView.setText(" (Sortering: "
//				+ HappyUtils.getSortAttrNameLocal(mActivity, mKoSData.getSortAttributePosition()) + ", "
//				+ HappyUtils.getSortOrderNameLocal(mActivity, mKoSData.getSortOrderPosition()) + ")");

            prevPageButton.setVisibility(mKoSData.getCurrentPage() > 1 ? View.VISIBLE : View.INVISIBLE);
            nextPageImageButton.setVisibility(mKoSData.getCurrentPage() < mKoSData.getMaxPages() ? View.VISIBLE : View.INVISIBLE);
        }
	}

	public void nextPage() {
		if (mKoSData.getCurrentPage() < mKoSData.getMaxPages())
		{			
			mKoSAdapter = null;
			mKoSData.setCurrentPage(mKoSData.getCurrentPage() + 1);
			fetchData();
		}
	}

	public void previousPage() {
    	if (mKoSData.getCurrentPage() > 1)
    	{
    		mKoSAdapter = null;
			mKoSData.setCurrentPage(mKoSData.getCurrentPage() - 1);
    		fetchData();
    	}
	}

	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		Intent koSObject = new Intent(mActivity, KoSObjectActivity.class);
		KoSListItem item = mKoSData.getKoSItems().get(position);
		koSObject.putExtra(KoSObjectActivity.URL, item.getLink());
		koSObject.putExtra(KoSObjectActivity.AREA, item.getArea());
		koSObject.putExtra(KoSObjectActivity.TYPE, item.getType());
		koSObject.putExtra(KoSObjectActivity.TITLE, item.getTitle());
		koSObject.putExtra(KoSObjectActivity.DATE, item.getTime());
		koSObject.putExtra(KoSObjectActivity.PRICE, item.getPrice());
		koSObject.putExtra(KoSObjectActivity.CATEGORY, item.getCategory());
		startActivity(koSObject);
	}



	@Override
	public void onCancel(DialogInterface dialog) {
		if (mKoSTask != null) {
			mKoSTask.cancel(true);
        }
    }

    @Override
    public void onSortParamChanged(int sortAttributePos, int sortOrderPos) {

        String sortAttrNameServer = HappyUtils.getSortAttrNameServer(getActivity(), sortAttributePos);
        String sortOrderNameServer = HappyUtils.getSortOrderNameServer(getActivity(), sortOrderPos);

        Editor edit = mPreferences.edit();
        edit.putInt(SORT_ATTRIBUTE_POS, sortAttributePos);
        edit.putString(SORT_ATTRIBUTE_SERVER, sortAttrNameServer);
        edit.putInt(SORT_ORDER_POS, sortOrderPos);
        edit.putString(SORT_ORDER_SERVER, sortOrderNameServer);
        edit.apply();

        mKoSData.setSortAttributePosition(sortAttributePos);
        mKoSData.setSortOrderPosition(sortOrderPos);

        mKoSData.setSortAttributeServer(sortAttrNameServer);
        mKoSData.setSortOrderServer(sortOrderNameServer);

        mKoSData.setCurrentPage(1);
        mKoSAdapter = null;

		fetchData();
    }

    public void updateSearchParams() {
        String text = searchEditText.getText().toString().trim();
        int categoryPos = searchCategory.getSelectedItemPosition();
        int regionPos = searchRegion.getSelectedItemPosition();
        int typePos = searchType.getSelectedItemPosition();
        int pricePos = searchPrice.getSelectedItemPosition();
        int yearPos = searchYear.getSelectedItemPosition();

        // Sökord
         mKoSData.setSearch(text);

        // Cyklar, Ramar, Komponenter...
        String categoryArrayPosition[] = getResources().getStringArray(R.array.kos_dialog_search_category_position);
        String categoryArrayName[] = getResources().getStringArray(R.array.kos_dialog_search_category_name);
        mKoSData.setCategoryPos(Integer.parseInt(categoryArrayPosition[categoryPos]));
        mKoSData.setCategoryName(categoryArrayName[categoryPos]);

        // Skåne, Blekinge, Halland...
        String regionArrayPosition[] = getResources().getStringArray(R.array.dialog_search_region_position);
        String regionArrayName[] = getResources().getStringArray(R.array.dialog_search_region_name);
        mKoSData.setRegionPos(Integer.parseInt(regionArrayPosition[regionPos]));
        mKoSData.setRegionName(regionArrayName[regionPos]);

        // Alla, Säljes, Köpes
        String typeArrayPosition[] = getResources().getStringArray(R.array.kos_dialog_search_type_position);
        mKoSData.setTypePosServer(Integer.parseInt(typeArrayPosition[typePos]));

        // Priser...
        String priceArrayPosition[] = getResources().getStringArray(R.array.kos_dialog_search_price_position);
        String priceArrayName[] = getResources().getStringArray(R.array.kos_dialog_search_price);
        mKoSData.setPricePos(Integer.parseInt(priceArrayPosition[pricePos]));
        mKoSData.setPriceName(priceArrayName[pricePos]);

        // Årsmodell
        String yearArrayPosition[] = getResources().getStringArray(R.array.kos_dialog_search_year_position);
        String yearArrayName[] = getResources().getStringArray(R.array.kos_dialog_search_year);
        mKoSData.setYearPos(Integer.parseInt(yearArrayPosition[yearPos]));
        mKoSData.setYearName(yearArrayName[yearPos]);

        Editor edit = mPreferences.edit();
        edit.putString(SEARCH_TEXT, text);

        edit.putInt(SEARCH_CATEGORY_POS, Integer.parseInt(categoryArrayPosition[categoryPos]));
        edit.putString(SEARCH_CATEGORY, categoryArrayName[categoryPos]);

        edit.putInt(SEARCH_REGION_POS, Integer.parseInt(regionArrayPosition[regionPos]));
        edit.putString(SEARCH_REGION, regionArrayName[regionPos]);

        edit.putInt(SEARCH_TYPE_POS, Integer.parseInt(typeArrayPosition[typePos]));

        edit.putInt(SEARCH_PRICE_POS, Integer.parseInt(priceArrayPosition[pricePos]));
        edit.putString(SEARCH_PRICE, priceArrayName[pricePos]);

        edit.putInt(SEARCH_YEAR_POS, Integer.parseInt(yearArrayPosition[yearPos]));
        edit.putString(SEARCH_YEAR, yearArrayName[yearPos]);

        // Dialog Spinner selection
        edit.putInt(SEARCH_TYPE_SPINNER, typePos);
        edit.putInt(SEARCH_REGION_SPINNER, regionPos);
        edit.putInt(SEARCH_CATEGORY_SPINNER, categoryPos);
        edit.putInt(SEARCH_PRICE_SPINNER, pricePos);
        edit.putInt(SEARCH_YEAR_SPINNER, yearPos);

        edit.apply();

        mKoSData.setCurrentPage(1);
        mKoSAdapter = null;
        fetchData();
    }

	@Override
	public void onClick(View v) {
		if (v.getId() == R.id.kos_prev) {
			previousPage();
			sendGaEvent(GaConstants.Actions.PREV_PAGE, GaConstants.Labels.EMPTY);
		} else if (v.getId() == R.id.kos_next) {
			nextPage();
			sendGaEvent(GaConstants.Actions.NEXT_PAGE, GaConstants.Labels.EMPTY);
		} else if (v.getId() == R.id.kos_bottombar) {
			openGoToPage();
		}
	}

	private void sendGaEvent(String action, String label) {
		mActivity.getTracker().send(new HitBuilders.EventBuilder()
				.setCategory(GaConstants.Categories.KOS_LIST)
				.setAction(action)
				.setLabel(label)
				.build());
	}
}