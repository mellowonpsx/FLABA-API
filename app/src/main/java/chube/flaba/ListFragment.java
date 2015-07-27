package chube.flaba;

import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONObject;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

import chube.flaba.Model.Data;

/**
 * Created by chube on 23/07/2015.
 */
public class ListFragment extends android.support.v4.app.ListFragment {

    private List itemList;
    private ListAdapter mAdapter;
    public boolean sortByDistance = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        JSONArray locations = Flaba.globalLocations;
        itemList = new ArrayList();
        final String locale = getActivity().getApplicationContext().getResources().getConfiguration().locale.getLanguage().toUpperCase();
        try {
            for (int i = 0; i < locations.length(); i++) {
                JSONObject thisLocation = locations.getJSONObject(i);
                String id = thisLocation.getString("_id");
                String labelLocalita = Data.getStringLocaleOrDefault(thisLocation.getJSONObject("localita"),"nome_campo",locale);
                String valoreLocalita = thisLocation.getJSONObject("localita").getString("valore");
                String labelProvincia = Data.getStringLocaleOrDefault(thisLocation.getJSONObject("comune"),"nome_campo",locale);
                String valoreComune = thisLocation.getJSONObject("comune").getString("valore");
                String valoreProvincia = thisLocation.getJSONObject("provincia").getString("valore");
                valoreProvincia = valoreComune+" ("+valoreProvincia+")";
                double lat = thisLocation.getJSONObject("coordinate").getJSONObject("latitudine").getDouble("valore");
                double lon = thisLocation.getJSONObject("coordinate").getJSONObject("longitudine").getDouble("valore");

                Location location = new Location(LocationManager.PASSIVE_PROVIDER);
                location.setLatitude(lat);
                location.setLongitude(lon);

                LocationManager lm = (LocationManager) getActivity().getApplicationContext().getSystemService(getActivity().getApplicationContext().LOCATION_SERVICE);
                Location myLocation = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);

                double distanzaNumerica = myLocation.distanceTo(location)/1000;
                String distanza = String.format("%.2f", distanzaNumerica)+" KM";

                itemList.add(new ListItem(id, labelLocalita + ":", valoreLocalita, labelProvincia + ":", valoreProvincia, distanza));
            }
            //sort by location
            if(!sortByDistance) {
                Collections.sort(itemList, new Comparator() {
                    public int compare(Object a, Object b) {
                        ListItem listItemA = (ListItem) a;
                        ListItem listItemB = (ListItem) b;
                        return listItemA.getLocazione().compareTo(listItemB.getLocazione());
                    }
                });
            } else { //sort by distance
                Collections.sort(itemList, new Comparator() {
                    public int compare(Object a, Object b) {
                        ListItem listItemA = (ListItem) a;
                        ListItem listItemB = (ListItem) b;
                        String stringA = listItemA.getDistanza();
                        String stringB = listItemB.getDistanza();
                        stringA = stringA.substring(0, stringA.length() - 2);
                        stringB = stringB.substring(0, stringB.length() - 2);
                        NumberFormat nf = NumberFormat.getInstance(new Locale(locale));
                        try {
                            double distanceA = nf.parse(stringA).doubleValue();
                            double distanceB = nf.parse(stringB).doubleValue();
                            return Double.compare(distanceA, distanceB);
                        } catch (Exception e) {
                            return 0;
                        }
                    }
                });
                itemList = itemList.subList(0,3);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        mAdapter = new ListAdapter(getActivity(), itemList);
        setListAdapter(mAdapter);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.list_fragment, container, false);
        return v;
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);
        ListItem item = (ListItem)this.itemList.get(position);
        //Toast.makeText(getActivity(), "Clicked, id: "+ item.getId(), Toast.LENGTH_LONG).show();

        android.support.v4.app.FragmentTransaction trans = getActivity().getSupportFragmentManager().beginTransaction();
        DetailFragment detailfrag = DetailFragment.newInstance(item.getId());
        trans.replace(R.id.container, detailfrag);
        trans.addToBackStack(null);
        trans.commit();
    }
}
