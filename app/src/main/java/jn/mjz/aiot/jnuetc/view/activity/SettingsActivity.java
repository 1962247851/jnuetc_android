package jn.mjz.aiot.jnuetc.view.activity;

import android.os.Bundle;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.preference.PreferenceFragmentCompat;

import com.youth.xframe.XFrame;
import com.youth.xframe.utils.statusbar.XStatusBar;

import jn.mjz.aiot.jnuetc.R;
import jn.mjz.aiot.jnuetc.application.App;

/**
 * @author 19622
 */
public class SettingsActivity extends AppCompatActivity {

    public static String sharedPreferencesName = "settings";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings_activity);
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.settings, new SettingsFragment())
                .commit();
        Toolbar toolbar = findViewById(R.id.toolbar_settings);
        setSupportActionBar(toolbar);
        XStatusBar.setColorNoTranslucent(this, XFrame.getColor(R.color.colorPrimary));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        App.initToolbar(toolbar, this);
    }

    public static class SettingsFragment extends PreferenceFragmentCompat {

        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            getPreferenceManager().setSharedPreferencesName(sharedPreferencesName);
            setPreferencesFromResource(R.xml.root_preferences, rootKey);
        }

    }


    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}