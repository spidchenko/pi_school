package com.spidchenko.week2task.ui;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.spidchenko.week2task.R;
import com.spidchenko.week2task.adapter.ImageListAdapter;
import com.spidchenko.week2task.models.Image;
import com.spidchenko.week2task.viewmodel.MainActivityViewModel;

import static com.spidchenko.week2task.ui.MapsActivity.EXTRA_LATITUDE;
import static com.spidchenko.week2task.ui.MapsActivity.EXTRA_LONGITUDE;

public class MainActivity extends AppCompatActivity implements ImageListAdapter.OnCardListener {
    private static final String TAG = "MainActivity.LOG_TAG";
    public static final String EXTRA_URL = "com.spidchenko.week2task.extras.EXTRA_URL";
    public static final String EXTRA_SEARCH_STRING = "com.spidchenko.week2task.extras.EXTRA_SEARCH_STRING";
    private static final String BUNDLE_SEARCH_STRING = "com.spidchenko.week2task.extras.BUNDLE_SEARCH_STRING";

    private String mCurrentSearchString;
    private ImageListAdapter mRecyclerAdapter;
    private MainActivityViewModel mMainActivityViewModel;

    //UI
    private EditText mEtSearchQuery;
    private Button mBtnSearch;
    private RecyclerView mRvImages;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mRvImages = findViewById(R.id.rv_images);
        mEtSearchQuery = findViewById(R.id.et_search_query);
        mBtnSearch = findViewById(R.id.btn_search);

//        if (savedInstanceState != null) {
//            if (!savedInstanceState.getString(BUNDLE_SEARCH_STRING).isEmpty()) {
//                mCurrentSearchString = savedInstanceState.getString(BUNDLE_SEARCH_STRING);
//            }
//        }

        initRecyclerView();

        mMainActivityViewModel = new ViewModelProvider(this).get(MainActivityViewModel.class);

        mMainActivityViewModel.getSearchString().observe(this, lastSearch -> {
            mCurrentSearchString = lastSearch;
            mEtSearchQuery.setText(lastSearch);
            mRecyclerAdapter.setSearchString(lastSearch);
            mRecyclerAdapter.notifyDataSetChanged();
        });

        mMainActivityViewModel.getAllImages().observe(this, images -> {
            mRecyclerAdapter.setImages(images);
            mRecyclerAdapter.notifyDataSetChanged();
        });

        Intent intent = getIntent();
        if (intent.getStringExtra(EXTRA_LATITUDE) != null) {
            String lat = intent.getStringExtra(EXTRA_LATITUDE);
            String lon = intent.getStringExtra(EXTRA_LONGITUDE);
            Log.d(TAG, "onReceiveGeoIntent: lat= " + lat + ". lon = " + lon);
            mMainActivityViewModel.searchImagesByCoordinates(lat, lon);
            hideKeyboard(this);
        }
    }

//    @Override
//    protected void onSaveInstanceState(@NonNull Bundle outState) {
//        super.onSaveInstanceState(outState);
//        outState.putString(BUNDLE_SEARCH_STRING, mCurrentSearchString);
//    }

    public void actionSearch(View view) {

        hideKeyboard(this);

        String searchString = mEtSearchQuery.getText().toString().trim();
        if (searchString.isEmpty()) {
            Toast.makeText(this, R.string.error_empty_search, Toast.LENGTH_LONG).show();
        } else {
            //TODO set spinning wheel here
            mMainActivityViewModel.searchImages(searchString);
//            mBtnSearch.setClickable(false);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.action_bar_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    public void startFavouritesActivity(MenuItem item) {
        Intent intent = new Intent(this, FavouritesActivity.class);
        startActivity(intent);
    }

    public void startSearchHistoryActivity(MenuItem item) {
        Intent intent = new Intent(this, SearchHistoryActivity.class);
        startActivity(intent);
    }

    public void startMapsActivity(MenuItem item) {
        Intent intent = new Intent(this, MapsActivity.class);
        startActivity(intent);
    }

    private void initRecyclerView() {
        mRecyclerAdapter = new ImageListAdapter(null, this);
        mRvImages.setAdapter(mRecyclerAdapter);

        int orientation = this.getResources().getConfiguration().orientation;
        if (orientation == Configuration.ORIENTATION_PORTRAIT) {
            mRvImages.setLayoutManager(new LinearLayoutManager(this));
        } else {
            mRvImages.setLayoutManager(new GridLayoutManager(this, 3));
        }

        ItemTouchHelper helper = getSwipeToDismissTouchHelper();
        helper.attachToRecyclerView(mRvImages);
    }

    @Override
    public void onCardClick(int position) {
        Log.d(TAG, "ViewHolder clicked! Position = " + position);

        Image image = mRecyclerAdapter.getImageAtPosition(position);

        Intent intent = new Intent(this, ImageViewerActivity.class);
        intent.putExtra(EXTRA_URL, image.getUrl(Image.PIC_SIZE_MEDIUM));
        intent.putExtra(EXTRA_SEARCH_STRING, mCurrentSearchString);
        this.startActivity(intent);
    }

    ItemTouchHelper getSwipeToDismissTouchHelper() {
        return new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0,
                ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(RecyclerView recyclerView,
                                  RecyclerView.ViewHolder viewHolder,
                                  RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
                int position = viewHolder.getAdapterPosition();
                Log.d(TAG, "ViewHolder Swiped! Position= " + position);
                mMainActivityViewModel.deleteImageAtPosition(position);
            }
        });
    }

    public static void hideKeyboard(Activity activity) {
        View view = activity.findViewById(android.R.id.content);
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }
}