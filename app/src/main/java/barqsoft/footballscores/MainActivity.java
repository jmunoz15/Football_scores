package barqsoft.footballscores;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;

public class MainActivity extends ActionBarActivity {
    public static int selected_match_id;
    public static int current_fragment = 2;
    private PagerFragment my_main;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (savedInstanceState == null) {
            my_main = new PagerFragment();
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, my_main)
                    .commit();
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_about) {
            Intent start_about = new Intent(this, AboutActivity.class);
            startActivity(start_about);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putInt(getString(R.string.pager_current), my_main.mPagerHandler.getCurrentItem());
        outState.putInt(getString(R.string.selected_match), selected_match_id);
        getSupportFragmentManager().putFragment(outState, getString(R.string.my_main), my_main);
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        current_fragment = savedInstanceState.getInt(getString(R.string.pager_current));
        selected_match_id = savedInstanceState.getInt(getString(R.string.selected_match));
        my_main = (PagerFragment) getSupportFragmentManager().getFragment(savedInstanceState, getString(R.string.my_main));
        super.onRestoreInstanceState(savedInstanceState);
    }
}
