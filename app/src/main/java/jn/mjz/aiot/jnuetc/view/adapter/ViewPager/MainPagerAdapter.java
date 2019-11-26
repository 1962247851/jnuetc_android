package jn.mjz.aiot.jnuetc.view.adapter.ViewPager;

import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import java.util.List;

public class MainPagerAdapter extends FragmentPagerAdapter {
  private List<Fragment> fragments;
  
  public MainPagerAdapter(FragmentManager fragmentManager, List<Fragment> fragments) {
    super(fragmentManager);
    this.fragments = fragments;
  }
  
  @Override
  public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {}
  
  @Override
  public int getCount() { return 3; }
  
  @Override
  public Fragment getItem(int position) { return (Fragment)this.fragments.get(position); }
}