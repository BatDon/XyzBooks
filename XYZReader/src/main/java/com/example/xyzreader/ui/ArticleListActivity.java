package com.example.xyzreader.ui;

import android.app.LoaderManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
//import android.support.v4.widget.SwipeRefreshLayout;
//import android.support.v7.app.ActionBarActivity;
//import android.support.v7.widget.RecyclerView;
//import android.support.v7.widget.StaggeredGridLayoutManager;
//import android.support.v7.widget.Toolbar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;
import androidx.appcompat.widget.Toolbar;
import android.text.Html;
import android.text.format.DateUtils;
import android.util.Log;
import android.util.TypedValue;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.xyzreader.BuildConfig;
import com.example.xyzreader.R;
import com.example.xyzreader.data.ArticleLoader;
import com.example.xyzreader.data.ItemsContract;
import com.example.xyzreader.data.UpdaterService;
import com.example.xyzreader.databinding.ActivityArticleListBinding;
import com.google.android.material.snackbar.Snackbar;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.GregorianCalendar;

import timber.log.Timber;

/**
 * An activity representing a list of Articles. This activity has different presentations for
 * handset and tablet-size devices. On handsets, the activity presents a list of items, which when
 * touched, lead to a {@link ArticleDetailActivity} representing item details. On tablets, the
 * activity presents a grid of items as cards.
 */
public class ArticleListActivity extends AppCompatActivity implements
        LoaderManager.LoaderCallbacks<Cursor> {

    private static final String TAG = ArticleListActivity.class.toString();
    private Toolbar mToolbar;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private RecyclerView mRecyclerView;

    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.sss");
    // Use default locale format
    private SimpleDateFormat outputFormat = new SimpleDateFormat();
    // Most time functions can only handle 1902 - 2037
    private GregorianCalendar START_OF_EPOCH = new GregorianCalendar(2,1,1);
    ActivityArticleListBinding activityArticleListBinding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Timber.i("onCreate called");
        //setContentView(R.layout.activity_article_list);
        activityArticleListBinding=ActivityArticleListBinding.inflate(getLayoutInflater());
        View rootView=activityArticleListBinding.getRoot();
        setContentView(rootView);
        if(BuildConfig.DEBUG){
            Timber.plant(new Timber.DebugTree());
        }


//        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        mToolbar=activityArticleListBinding.toolbar;

        final View toolbarContainerView=activityArticleListBinding.toolbarContainer;
//        final View toolbarContainerView = findViewById(R.id.toolbar_container);

        mSwipeRefreshLayout=activityArticleListBinding.swipeRefreshLayout;
//        mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_refresh_layout);

        mRecyclerView = activityArticleListBinding.recyclerView;
//        mRecyclerView.setVisibility(View.INVISIBLE);
//        mRecyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        Timber.i("onCreate before loader called");

        getLoaderManager().initLoader(0, null, this);

        Timber.i("onCreate after loader called");

        if (savedInstanceState == null) {
            refresh();
        }

        Timber.i("end of onCreate method");




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

    private boolean mIsRefreshing = false;

    private BroadcastReceiver mRefreshingReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (UpdaterService.BROADCAST_ACTION_STATE_CHANGE.equals(intent.getAction())) {
                mIsRefreshing = intent.getBooleanExtra(UpdaterService.EXTRA_REFRESHING, false);
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
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
        if(cursor==null){
            Log.i("ArticleListActivity","cursor equals null");
        }
        Log.i("ArticleListActivity","onLoadFinished called");

        cursor.moveToFirst();
        for (int i = 0; i < cursor.getCount(); i++) {
            long id= cursor.getLong(ArticleLoader.Query._ID);
            Timber.i("Adapter constructor id= "+id);
            long photoUrl= cursor.getLong(ArticleLoader.Query.PHOTO_URL);
            String photoUrlString= cursor.getString(ArticleLoader.Query.PHOTO_URL);
            Timber.i("Adapter constructor photo_url= "+photoUrlString);
            String thumbUrlString= cursor.getString(ArticleLoader.Query.THUMB_URL);
            Timber.i("Adapter constructor thumb_url= "+thumbUrlString);
            String title=cursor.getString(ArticleLoader.Query.TITLE);
            Timber.i("Adapter title= "+title);
            String date=cursor.getString(ArticleLoader.Query.PUBLISHED_DATE);
            Timber.i("Adapter date= "+date);
            cursor.moveToNext();
        }

        Adapter adapter = new Adapter(cursor);
        if(adapter==null) {
            Timber.i("onLoadFinished adapter equals null");
        }
        else{
            Timber.i("onLoadFinished adapter does not equal null");
        }
        if(mRecyclerView==null){
            Timber.i("mRecyclerView equals null");
        }
        else{
            Timber.i("mRecyclerView does not equal null");
        }
        adapter.setHasStableIds(true);
        mRecyclerView.setAdapter(adapter);
        int columnCount = getResources().getInteger(R.integer.list_column_count);
        StaggeredGridLayoutManager sglm =
                new StaggeredGridLayoutManager(columnCount, StaggeredGridLayoutManager.VERTICAL);
        mRecyclerView.setLayoutManager(sglm);
        mRecyclerView.setVisibility(View.VISIBLE);

        Snackbar.make(activityArticleListBinding.getRoot(), R.string.books_loaded, Snackbar.LENGTH_LONG).show();
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mRecyclerView.setAdapter(null);
    }

    private class Adapter extends RecyclerView.Adapter<ViewHolder> {
        private Cursor mCursor;

        public Adapter(Cursor cursor) {
            mCursor = cursor;
            if(mCursor==null){
                Timber.i("cursor equals null in onLoaderReset");
            }
            else{
                Timber.i("cursor does not equal null");
                Timber.i("Adapter cursor size= %s", mCursor.getCount());
//                mCursor.moveToPosition(0);
//               long id= mCursor.getLong(ArticleLoader.Query._ID);
//                Timber.i("Adapter constructor id= "+id);
//               long photoUrl= mCursor.getLong(ArticleLoader.Query.PHOTO_URL);
//                Timber.i("Adapter constructor photo_url= "+photoUrl);
            }
        }

        @Override
        public long getItemId(int position) {
            mCursor.moveToPosition(position);
            Timber.i("Adapter mCursor position= %s", position);
            return mCursor.getLong(ArticleLoader.Query._ID);
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            Timber.i("Adapter onCreateViewHolder called");
            View view = getLayoutInflater().inflate(R.layout.list_item_article, parent, false);
            final ViewHolder vh = new ViewHolder(view);
            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Timber.i("in onCreateViewHolder clicked");
                    startActivity(new Intent(Intent.ACTION_VIEW,
                            ItemsContract.Items.buildItemUri(getItemId(vh.getAdapterPosition()))));
                }
            });
            return vh;
        }

        private Date parsePublishedDate() {
            Timber.i("Adapter parsePublishedDate");
            try {
                String date = mCursor.getString(ArticleLoader.Query.PUBLISHED_DATE);
                return dateFormat.parse(date);
            } catch (ParseException ex) {
                Log.e(TAG, ex.getMessage());
                Log.i(TAG, "passing today's date");
                return new Date();
            }
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            Timber.i("Adapter onBindViewHolder called position= %s", position);
            mCursor.moveToPosition(position);
            holder.titleView.setText(mCursor.getString(ArticleLoader.Query.TITLE));
            Date publishedDate = parsePublishedDate();
            if (!publishedDate.before(START_OF_EPOCH.getTime())) {

                holder.subtitleView.setText(Html.fromHtml(
                        DateUtils.getRelativeTimeSpanString(
                                publishedDate.getTime(),
                                System.currentTimeMillis(), DateUtils.HOUR_IN_MILLIS,
                                DateUtils.FORMAT_ABBREV_ALL).toString()
                                + "<br/>" + " by "
                                + mCursor.getString(ArticleLoader.Query.AUTHOR)));
            } else {
                holder.subtitleView.setText(Html.fromHtml(
                        outputFormat.format(publishedDate)
                        + "<br/>" + " by "
                        + mCursor.getString(ArticleLoader.Query.AUTHOR)));
            }
            Timber.i("adapter before setting thumbnailView");
            holder.thumbnailView.setImageUrl(
                    mCursor.getString(ArticleLoader.Query.THUMB_URL),
                    ImageLoaderHelper.getInstance(ArticleListActivity.this).getImageLoader());
            holder.thumbnailView.setAspectRatio(mCursor.getFloat(ArticleLoader.Query.ASPECT_RATIO));
        }

        @Override
        public int getItemCount() {
            Timber.i("Adapter getItemCount called mCursor count= %s", mCursor.getCount());
            return mCursor.getCount();
        }
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public DynamicHeightNetworkImageView thumbnailView;
        public TextView titleView;
        public TextView subtitleView;

        public ViewHolder(View view) {
            super(view);
            Timber.i("Adapter ViewHolder being called");
            thumbnailView = (DynamicHeightNetworkImageView) view.findViewById(R.id.thumbnail);
            titleView = (TextView) view.findViewById(R.id.article_title);
            subtitleView = (TextView) view.findViewById(R.id.article_subtitle);
        }
    }

//    @Override
//    protected void onDestroy() {
//        super.onDestroy();
//        if(activityArticleListBinding!=null){
//            activityArticleListBinding=null;
//        }
//
//    }
}



