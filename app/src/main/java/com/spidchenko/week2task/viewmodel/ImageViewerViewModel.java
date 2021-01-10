package com.spidchenko.week2task.viewmodel;

import android.app.Application;
import android.content.ContentResolver;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.StringRes;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.bumptech.glide.RequestManager;
import com.spidchenko.week2task.FavouriteRepository;
import com.spidchenko.week2task.FileRepository;
import com.spidchenko.week2task.MyApplication;
import com.spidchenko.week2task.R;
import com.spidchenko.week2task.db.CurrentUser;
import com.spidchenko.week2task.db.models.Favourite;
import com.spidchenko.week2task.network.Result;

public class ImageViewerViewModel extends AndroidViewModel {
    private static final String TAG = "ImageViewerVM.LOG_TAG";

    private final FavouriteRepository mFavouriteRepository;
    private final FileRepository mFileRepository;
    private final MutableLiveData<Boolean> mInFavourites = new MutableLiveData<>();
    private final SingleLiveEvent<Integer> mSnackBarMessage = new SingleLiveEvent<>();

    public ImageViewerViewModel(@NonNull Application application, FavouriteRepository repository) {
        super(application);
        CurrentUser currentUser = CurrentUser.getInstance();

        mFavouriteRepository = repository;

//        mFavouriteRepository = new FavouriteRepository(AppDatabase.getInstance(application).favouriteDao(),
//                ((MyApplication) getApplication()).executorService,
//                currentUser.getUser().getId());
        mFileRepository = new FileRepository(application);
    }

    public SingleLiveEvent<Integer> getSnackBarMessage() {
        return mSnackBarMessage;
    }

    public LiveData<Boolean> getInFavourites(Favourite favourite) {
        checkInFavourites(favourite);
        return mInFavourites;
    }

    private void checkInFavourites(Favourite favourite) {
        mFavouriteRepository.checkInFavourites(favourite, result -> {
            if (result instanceof Result.Success) {
                mInFavourites.postValue(((Result.Success<Boolean>) result).data);
                Log.d(TAG, "checkInFavourites: Already in favourites!");
            } else {
                handleError((Result.Error<Boolean>) result);
            }
        });
    }


    public void toggleFavourite(Favourite favourite) {
        if ((mInFavourites.getValue() != null) && (mInFavourites.getValue())) {
            mFavouriteRepository.deleteFavourite(favourite, result -> {
                if (result instanceof Result.Success) {
                    setMessage(R.string.removed_from_favourites);
                } else {
                    handleError((Result.Error<Boolean>) result);
                }
                //Room will take care of auto updating from DB
                checkInFavourites(favourite);
            });
        } else {
            mFavouriteRepository.addFavorite(favourite, result -> {
                if (result instanceof Result.Success) {
                    setMessage(R.string.added_to_favourites);
                } else {
                    handleError((Result.Error<Boolean>) result);
                }
                //Room will take care of auto updating from DB
                checkInFavourites(favourite);
            });
        }
    }

    public void saveImage(RequestManager glide, ContentResolver contentResolver, Favourite favourite) {
        mFileRepository.saveImage(glide, contentResolver, favourite);
    }

    private void handleError(Result.Error<Boolean> error) {
        Log.d(TAG, "handleError: Error Returned From Repo: " + error.throwable.getMessage());
        //can use switch-case here
        setMessage(R.string.error_default_message);
    }

    private void setMessage(@StringRes int resId) {
        mSnackBarMessage.postValue(resId);
    }


    public static class Factory extends ViewModelProvider.NewInstanceFactory {

        @NonNull
        private final Application mApplication;

        private final FavouriteRepository mRepository;

        public Factory(@NonNull Application application) {
            mApplication = application;
            mRepository = ((MyApplication) application).getFavouriteRepository();
        }

        @SuppressWarnings("unchecked")
        @Override
        @NonNull
        public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
            return (T) new ImageViewerViewModel(mApplication, mRepository);
        }
    }


}
