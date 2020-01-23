package jn.mjz.aiot.jnuetc.viewmodel;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;

import jn.mjz.aiot.jnuetc.greendao.entity.Data;
import jn.mjz.aiot.jnuetc.view.fragment.DataChangeLogFragment;
import jn.mjz.aiot.jnuetc.view.fragment.DetailsFragment;
import jn.mjz.aiot.jnuetc.view.fragment.FeedbackFragment;
import jn.mjz.aiot.jnuetc.view.fragment.ProgressFragment;

/**
 * @author qq1962247851
 * @date 2020/1/18 21:54
 */
public class DetailsViewModel extends ViewModel implements Serializable {

    private String dataStringBackup;
    private MutableLiveData<Data> data;
    private MutableLiveData<Boolean> modifyMode;
    private List<Fragment> fragmentList;
    private List<String> pageTitleList;

    public DetailsViewModel() {
    }

    public List<String> getPageTitleList() {
        if (pageTitleList == null) {
            pageTitleList = new LinkedList<>();
            pageTitleList.add("详情");
            pageTitleList.add("进度");
            pageTitleList.add("日志");
            pageTitleList.add("维修反馈");
        }
        return pageTitleList;
    }

    public List<Fragment> getFragmentList() {
        if (fragmentList == null) {
            fragmentList = new LinkedList<>();
            fragmentList.add(DetailsFragment.newInstance(getDataStringBackup()));
            fragmentList.add(ProgressFragment.newInstance());
            fragmentList.add(DataChangeLogFragment.newInstance());
            fragmentList.add(FeedbackFragment.newInstance(getDataStringBackup()));
        }
        return fragmentList;
    }

    public String getDataStringBackup() {
        if (dataStringBackup == null) {
            dataStringBackup = "";
        }
        return dataStringBackup;
    }

    public void setDataStringBackup(String dataStringBackup) {
        this.dataStringBackup = dataStringBackup;
    }

    public MutableLiveData<Data> getData() {
        if (data == null) {
            data = new MutableLiveData<>();
        }
        return data;
    }

    public MutableLiveData<Boolean> getModifyMode() {
        if (modifyMode == null) {
            modifyMode = new MutableLiveData<>();
            modifyMode.setValue(Boolean.FALSE);
        }
        return modifyMode;
    }

}
