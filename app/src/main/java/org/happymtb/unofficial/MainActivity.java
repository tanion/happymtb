package org.happymtb.unofficial;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.MenuItem;
import android.view.View;

import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;

import com.google.android.material.navigation.NavigationView;
import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu;

import org.happymtb.unofficial.fragment.ArticlesListFragment;
import org.happymtb.unofficial.fragment.CalendarListFragment;
import org.happymtb.unofficial.fragment.ForumListFragment;
import org.happymtb.unofficial.fragment.HomesListFragment;
import org.happymtb.unofficial.fragment.KoSListFragment;
import org.happymtb.unofficial.fragment.SavedListFragment;
import org.happymtb.unofficial.fragment.SettingsFragment;
import org.happymtb.unofficial.fragment.VideoListFragment;


public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private static final int HOME = 0;
    private static final int FORUM = 1;
    private static final int ARTICLES = 2;
    private static final int KOP_OCH_SALJ = 3;
    private static final int SAVED = 4;
    private static final int VIDEO = 5;
    private static final int CALENDAR = 6;
    private static final int SETTINGS = 7;

    private static final String STATE_SELECTED_NAVIGATION_ITEM = "selected_navigation_item";
    private static final String CURRENT_FRAGMENT_TAG = "current_fragment_tag";
    private static final String OPEN_DRAWER = "open_drawer";

    private Fragment mCurrentFragment;
    private SharedPreferences mPreferences;

    private String mCurrentFragmentTag = null;
    private boolean mLogin;
    private NavigationView mNavigationView;
    DrawerLayout mDrawer;

    private SlidingMenu mKosSlidingMenu;

    private int mCheckedNavigationItem;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main_drawer);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mDrawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, mDrawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        mDrawer.addDrawerListener(toggle);
        toggle.syncState();
        mDrawer.addDrawerListener(addDrawerListenerForSlidingMenu());

        mNavigationView = findViewById(R.id.nav_view);
        mNavigationView.setNavigationItemSelectedListener(this);

        mPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        // Restore fragments
        if (savedInstanceState != null) {
            mCurrentFragmentTag = savedInstanceState.getString(CURRENT_FRAGMENT_TAG);
            mCurrentFragment = getSupportFragmentManager().findFragmentByTag(mCurrentFragmentTag);
        } else {
            //TODO Enable after list is fixed
//            mCheckedNavigationItem = mPreferences.getInt(SettingsActivity.START_PAGE, KOP_OCH_SALJ);
            mCheckedNavigationItem = KOP_OCH_SALJ;
            switchContent(mCheckedNavigationItem);

            mNavigationView.getMenu().getItem(mCheckedNavigationItem).setChecked(true);
        }

        //Open the drawer the first time the app is started
        if (mPreferences.getBoolean(OPEN_DRAWER, true)) {
            mDrawer.openDrawer(GravityCompat.START);
            mPreferences.edit().putBoolean(OPEN_DRAWER, false).apply();
        }
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        // Restore the previously serialized current dropdown position.
        if (savedInstanceState.containsKey(STATE_SELECTED_NAVIGATION_ITEM)) {
            //Restore the fragment's instance
            mCheckedNavigationItem =  savedInstanceState.getInt(STATE_SELECTED_NAVIGATION_ITEM);
            mNavigationView.getMenu().getItem(mCheckedNavigationItem).setChecked(true);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        // Serialize the current dropdown position.
        outState.putInt(STATE_SELECTED_NAVIGATION_ITEM, mCheckedNavigationItem);
    }

    private void switchContent(int position) {
        Fragment frag = null;
        String newFragmentTag = "";
        switch (position) {
            case HOME:
                frag = new HomesListFragment();
                newFragmentTag = HomesListFragment.TAG;
                break;
            case FORUM:
                frag = new ForumListFragment();
                newFragmentTag = ForumListFragment.TAG;
                break;
            case ARTICLES:
                frag = new ArticlesListFragment();
                newFragmentTag = ArticlesListFragment.TAG;
                break;
            case KOP_OCH_SALJ:
                frag = new KoSListFragment();
                newFragmentTag = KoSListFragment.TAG;
                break;
            case SAVED:
                frag = new SavedListFragment();
                newFragmentTag = SavedListFragment.TAG;
                break;
            case VIDEO:
                frag = new VideoListFragment();
                newFragmentTag = VideoListFragment.TAG;
                break;
            case CALENDAR:
                frag = new CalendarListFragment();
                newFragmentTag = CalendarListFragment.TAG;
                break;
            case SETTINGS:
                frag = new SettingsFragment();
                newFragmentTag = SettingsFragment.TAG;
                break;
        }

        if (mKosSlidingMenu != null) {
            if (newFragmentTag.equals(KoSListFragment.TAG)) {
                mKosSlidingMenu.setSlidingEnabled(true);
            } else {
                mKosSlidingMenu.setSlidingEnabled(false);
            }
        }

        if (frag != null && !newFragmentTag.equals(mCurrentFragmentTag)) {
            mCurrentFragmentTag = newFragmentTag;
            mCurrentFragment = frag;
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.content_frame, frag, newFragmentTag)
                    .addToBackStack(null)
                    .commit();
        }
    }

    @Override
    public void onBackPressed() {
        if (mDrawer.isDrawerOpen(GravityCompat.START)) {
            mDrawer.closeDrawer(GravityCompat.START);
        } else if (mKosSlidingMenu.isMenuShowing()) {
            mKosSlidingMenu.toggle();
        } else {
            finish();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void setLoggedIn(boolean loggedIn) {
        mLogin = loggedIn;
    }

    public boolean isLoggedIn() {
        return mLogin;
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        item.setChecked(true);

        int id = item.getItemId();
        int pos = 0;

        if (id == R.id.nav_home) {
            pos = HOME;
        } else if (id == R.id.nav_forum) {
            pos = FORUM;
        } else if (id == R.id.nav_articles) {
            pos = ARTICLES;
        } else if (id == R.id.nav_kos) {
            pos = KOP_OCH_SALJ;
        } else if (id == R.id.nav_saved) {
            pos = SAVED;
        } else if (id == R.id.nav_videos) {
            pos = VIDEO;
        } else if (id == R.id.nav_calendar) {
            pos = CALENDAR;
        } else if (id == R.id.nav_settings) {
            pos = SETTINGS;
        }
        if (pos == SETTINGS) {
            Intent settingsIntent = new Intent(this, SettingsActivity.class);
            startActivity(settingsIntent);
            return true;
        }

        mCheckedNavigationItem = pos;
        switchContent(pos);

        mDrawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private DrawerLayout.DrawerListener addDrawerListenerForSlidingMenu() {
        return new DrawerLayout.DrawerListener() {
            @Override
            public void onDrawerSlide(View drawerView, float slideOffset) {}

            @Override
            public void onDrawerOpened(View drawerView) {
                if (mKosSlidingMenu != null) {
                    mKosSlidingMenu.setSlidingEnabled(false);
                }
            }

            @Override
            public void onDrawerClosed(View drawerView) {
                if (mKosSlidingMenu != null && KoSListFragment.TAG.equals(mCurrentFragmentTag)) {
                    mKosSlidingMenu.setSlidingEnabled(true);
                }
            }

            @Override
            public void onDrawerStateChanged(int newState) {}
        };
    }

    public SlidingMenu getKosSlidingMenu() {
        return mKosSlidingMenu;
    }

    public void setKosSlidingMenu(SlidingMenu slidingMenu) {
        this.mKosSlidingMenu = slidingMenu;
        if (mDrawer.isDrawerOpen(GravityCompat.START)) {
            slidingMenu.setSlidingEnabled(false);
        }
    }
}
