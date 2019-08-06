package jn.mjz.aiot.jnuetc.ViewModel;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import jn.mjz.aiot.jnuetc.Greendao.Entity.Data;

public class DetailsViewModel extends ViewModel {
    private static final String TAG = "DetailsViewModel";
    private MutableLiveData<Data> dataEvent;
    private final String mKey;

    public DetailsViewModel(String mKey) {
        this.mKey = mKey;
    }

    public MutableLiveData<Data> getDataEvent() {
        if (dataEvent == null) {
            dataEvent = new MutableLiveData<>();
            dataEvent.setValue(null);
        }
        return dataEvent;
    }

    public static class Factory implements ViewModelProvider.Factory {
        private String mKey;

        public Factory(String key) {
            mKey = key;
        }

        @Override
        public <T extends ViewModel> T create(Class<T> modelClass) {
            return (T) new DetailsViewModel(mKey);
        }
    }

    public String getKey() {
        return mKey;
    }
}
