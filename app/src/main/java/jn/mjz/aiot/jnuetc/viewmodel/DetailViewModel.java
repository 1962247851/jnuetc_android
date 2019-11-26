package jn.mjz.aiot.jnuetc.viewmodel;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import jn.mjz.aiot.jnuetc.greendao.entity.Data;

public class DetailViewModel extends ViewModel {

    private MutableLiveData<Data> data;

    public MutableLiveData<Data> getData() {
        if (data == null){
            data = new MutableLiveData<>();
            data.setValue(new Data());
        }
        return data;
    }
}
