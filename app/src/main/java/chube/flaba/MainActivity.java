package chube.flaba;

import android.app.NotificationManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.Locale;


public class MainActivity extends ActionBarActivity //AppCompatActivity
        implements NavigationDrawerCallbacks {

    /**
     * Fragment managing the behaviors, interactions and presentation of the navigation drawer.
     */
    private NavigationDrawerFragment mNavigationDrawerFragment;

    private Toolbar mToolbar;

    private Menu mMenu;

    private CustomMapFragment custommapfrag;
    private ExampleFragment examplefrag;
    private ListFragment listfrag;
    private DetailFragment detailfrag;

    FragmentManager mng;
    //private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mToolbar = (Toolbar) findViewById(R.id.toolbar_actionbar);
        setSupportActionBar(mToolbar);
        mNavigationDrawerFragment = (NavigationDrawerFragment)
                getSupportFragmentManager().findFragmentById(R.id.fragment_drawer);

        // Set up the drawer.
        mNavigationDrawerFragment.setup(R.id.fragment_drawer, (DrawerLayout) findViewById(R.id.drawer), mToolbar);
        // populate the navigation drawer
        mNavigationDrawerFragment.setUserData("John Doe", "johndoe@doe.com", BitmapFactory.decodeResource(getResources(), R.drawable.avatar));

        examplefrag = new ExampleFragment();
        custommapfrag = new CustomMapFragment();
        listfrag = new ListFragment();
        detailfrag = new DetailFragment();

        mng = getSupportFragmentManager();

        FragmentTransaction trans = getSupportFragmentManager().beginTransaction();
        trans.add(R.id.container, custommapfrag).commit();
    }

    @Override
    public void onNavigationDrawerItemSelected(int position) {
        // update the main content by replacing fragments
        FragmentTransaction trans;
        if(mng != null) {
            trans = mng.beginTransaction();
            switch (position) {
                default: //map
                    trans.replace(R.id.container, custommapfrag);
                    break;
                case 1: //ricerca
		            listfrag = new ListFragment();
                    listfrag.sortByDistance = false;
                    trans.replace(R.id.container, listfrag);
                    break;
                case 2: //+ vicino
                    listfrag = new ListFragment();
                    listfrag.sortByDistance = true;
                    trans.replace(R.id.container, listfrag);
                    break;
                case 3: //info
                    /*detailfrag = DetailFragment.newInstance("info");
                    trans.replace(R.id.container, detailfrag);*/
                    break;
            }
            trans.addToBackStack(null);
            trans.commit();
        }
    }


    @Override
    public void onBackPressed() {
        if (mNavigationDrawerFragment.isDrawerOpen())
            mNavigationDrawerFragment.closeDrawer();
        else
            super.onBackPressed();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (!mNavigationDrawerFragment.isDrawerOpen()) {
            // Only show items in the action bar relevant to this screen
            // if the drawer is not showing. Otherwise, let the drawer
            // decide what to show in the action bar.
            getMenuInflater().inflate(R.menu.main, menu);
            mMenu = menu;
            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
            String connectionPreference = sharedPreferences.getString(Flaba.APP_UPDATE_PREFERENCE, "WIFI+3G");
            MenuItem itemWifi = menu.findItem(R.id.update_wifi);
            if(connectionPreference.equals("WIFI")){
                itemWifi.setChecked(true);
            }
            else {
                itemWifi.setChecked(false);
            }
            String langPreference = sharedPreferences.getString(Flaba.LOCALE_PREFERENCE, "default");
            MenuItem itemDef = menu.findItem(R.id.default_lang);
            MenuItem itemIt = menu.findItem(R.id.italian);
            MenuItem itemEn = menu.findItem(R.id.english);
            MenuItem itemDe = menu.findItem(R.id.deutch);
            switch(langPreference){
                case "IT":
//                    itemDef.setChecked(false);
                    itemIt.setChecked(true);
//                    itemEn.setChecked(false);
//                    itemDe.setChecked(false);
                    break;
                case "EN":
//                    itemDef.setChecked(false);
//                    itemIt.setChecked(false);
                    itemEn.setChecked(true);
//                    itemDe.setChecked(false);
                    break;
                case "DE":
//                    itemDef.setChecked(false);
//                    itemIt.setChecked(false);
//                    itemEn.setChecked(false);
                    itemDe.setChecked(true);
                    break;
                default:
                    itemDef.setChecked(true);
//                    itemIt.setChecked(false);
//                    itemEn.setChecked(false);
//                    itemDe.setChecked(false);
                    break;
            }
            return true;
        }
        return super.onCreateOptionsMenu(menu);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        //String connectionPreference = sharedPreferences.getString(Flaba.APP_UPDATE_PREFERENCE, "WIFI+3G");

        if (id == R.id.update_wifi) {
            if(item.isChecked()){
                item.setChecked(false);
                sharedPreferences.edit().putString(Flaba.APP_UPDATE_PREFERENCE, "WIFI+3G").commit();

            }
            else{
                item.setChecked(true);
                sharedPreferences.edit().putString(Flaba.APP_UPDATE_PREFERENCE, "WIFI").commit();
            }
            return true;
        }

        if (id == R.id.force_update) {
            sharedPreferences.edit().putLong(Flaba.JSON_TIMESTAMP_PREFERENCE,  new Long(0)).commit();
            //Toast.makeText(this, "Update at next application run", Toast.LENGTH_LONG).show();
            Toast.makeText(this, getString(R.string.notifica_aggiornamento_prossimo_riavvio), Toast.LENGTH_LONG).show();
            return true;
        }

        if (id == R.id.default_lang) {
            if(!item.isChecked()){
                item.setChecked(true);
                sharedPreferences.edit().remove(Flaba.LOCALE_PREFERENCE).commit();
                Toast.makeText(this, getString(R.string.cambio_lingua), Toast.LENGTH_LONG).show();
                //mMenu.getItem(R.id.italian).setChecked(false);
                //mMenu.getItem(R.id.english).setChecked(false);
                //mMenu.getItem(R.id.deutch).setChecked(false);
            }
            return true;
        }
        if (id == R.id.italian) {
            if(!item.isChecked()){
                item.setChecked(true);
                sharedPreferences.edit().putString(Flaba.LOCALE_PREFERENCE, "IT").commit();
                Toast.makeText(this, getString(R.string.cambio_lingua), Toast.LENGTH_LONG).show();
//                mMenu.getItem(R.id.default_lang).setChecked(false);
//                mMenu.getItem(R.id.english).setChecked(false);
//                mMenu.getItem(R.id.deutch).setChecked(false);
            }
            return true;
        }
        if (id == R.id.english) {
            if(!item.isChecked()){
                item.setChecked(true);
                sharedPreferences.edit().putString(Flaba.LOCALE_PREFERENCE, "EN").commit();
                Toast.makeText(this, getString(R.string.cambio_lingua), Toast.LENGTH_LONG).show();
//                mMenu.getItem(R.id.default_lang).setChecked(false);
//                mMenu.getItem(R.id.italian).setChecked(false);
//                mMenu.getItem(R.id.deutch).setChecked(false);
            }
            return true;
        }
        if (id == R.id.deutch) {
            if(!item.isChecked()){
                item.setChecked(true);
                sharedPreferences.edit().putString(Flaba.LOCALE_PREFERENCE, "DE").commit();
                Toast.makeText(this, getString(R.string.cambio_lingua), Toast.LENGTH_LONG).show();
//                mMenu.getItem(R.id.default_lang).setChecked(false);
//                mMenu.getItem(R.id.italian).setChecked(false);
//                mMenu.getItem(R.id.english).setChecked(false);
            }
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


}
