package com.example.xyzreader.ui;


import android.app.ActivityOptions;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.loader.app.LoaderManager;
import androidx.loader.content.Loader;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.View;

import com.example.xyzreader.R;
import com.example.xyzreader.data.ArticleLoader;
import com.example.xyzreader.data.UpdaterService;
import com.google.android.material.appbar.CollapsingToolbarLayout;


/**
 * An activity representing a list of Articles. This activity has different presentations for
 * handset and tablet-size devices. On handsets, the activity presents a list of items, which when
 * touched, lead to a {@link ArticleDetailActivity} representing item details. On tablets, the
 * activity presents a grid of items as cards.
 */
public class ArticleListActivity extends AppCompatActivity implements
        LoaderManager.LoaderCallbacks<Cursor>, BookListAdapter.itemClicked {

    private static final String TAG = ArticleListActivity.class.toString();
    public static final String BUNDLE_KEY_ITEM_ID = "bundle_key_item_id";
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private RecyclerView mRecyclerView;


    private boolean mIsRefreshing = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_article_list);

        ((CollapsingToolbarLayout) findViewById(R.id.collapsing_toolbar_layout)).setTitle("");

        mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_refresh_layout);
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if(!mIsRefreshing ){
                    updateRefreshingUI();
                }
            }
        });

        mRecyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        LoaderManager.getInstance(this).initLoader(0, null, this);

        if (savedInstanceState == null) {
            refresh();
        }


    }

    private void refresh() {
        startService(new Intent(this, UpdaterService.class));
    }

    @Override
    protected void onStart() {
        super.onStart();
        registerReceiver(mRefreshingReceiver,
                new IntentFilter(UpdaterService.BROADCAST_ACTION_STATE_CHANGE));
    }

    @Override
    protected void onStop() {
        super.onStop();
        unregisterReceiver(mRefreshingReceiver);
    }


    private BroadcastReceiver mRefreshingReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (UpdaterService.BROADCAST_ACTION_STATE_CHANGE.equals(intent.getAction())) {
                Log.d(TAG, "::::::::::::::::::  received update from broadcast receiver");
                mIsRefreshing = intent.getBooleanExtra(UpdaterService.EXTRA_REFRESHING, false);

                Log.d(TAG, "::::::::::::::::::  mIsRefreshing = " + mIsRefreshing);
                updateRefreshingUI();
            }
        }
    };

    private void updateRefreshingUI() {
        mSwipeRefreshLayout.setRefreshing(mIsRefreshing);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        return ArticleLoader.newAllArticlesInstance(this);
    }

    @Override
    public void onLoadFinished(@NonNull Loader<Cursor> loader, Cursor data) {
        BookListAdapter adapter = new BookListAdapter(data, this, getApplicationContext());
        adapter.setHasStableIds(true);
        mRecyclerView.setAdapter(adapter);
        int columnCount = getResources().getInteger(R.integer.list_column_count);
        GridLayoutManager sglm =
                new GridLayoutManager(this,columnCount);
        mRecyclerView.setLayoutManager(sglm);
    }


    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

        mRecyclerView.setAdapter(null);
    }

    @Override
    public void itemClickedAtPosition(int position, View view, long itemId) {

        Intent intent = new Intent(this, ArticleDetailActivity.class);
        intent.putExtra(BUNDLE_KEY_ITEM_ID,  itemId);

//        startActivity(intent,
//                ActivityOptions.makeSceneTransitionAnimation(
//                this, view, view.getTransitionName()).toBundle());
        startActivity(intent);

    }

}
