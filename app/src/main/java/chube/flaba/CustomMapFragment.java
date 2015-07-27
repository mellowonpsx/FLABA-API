package chube.flaba;

import android.app.Activity;
import android.app.FragmentTransaction;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.maps.android.ui.IconGenerator;

import org.json.JSONArray;

import java.util.HashMap;

import chube.flaba.Model.Data;

public class CustomMapFragment extends SupportMapFragment {
    private static GoogleMap mMap;
    static final JSONArray locations = Flaba.globalLocations;
    private final String LOG_TAG = "FLABA_CUSTOM_MAP";
    private final String MAP_INSTANCE_KEY = "FLABA_CUSTOM_MAP";
    private HashMap<Marker, String> markers = new HashMap<Marker, String>();

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if(mMap == null)
        {
            mMap = super.getMap();
            if (mMap != null)
            {
                mMap.getUiSettings().setAllGesturesEnabled(true);
                mMap.getUiSettings().setZoomControlsEnabled(true);
                mMap.getUiSettings().setCompassEnabled(false);
                mMap.getUiSettings().setRotateGesturesEnabled(false);
                mMap.getUiSettings().setTiltGesturesEnabled(false);
                mMap.getUiSettings().setIndoorLevelPickerEnabled(false);
                //mMap.getUiSettings().setMapToolbarEnabled(false);
                mMap.setMyLocationEnabled(true);
                mMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
                    @Override
                    public void onInfoWindowClick(Marker marker) {
                        String id = markers.get(marker);
                        //Toast.makeText(getActivity(), "Marker clicked: " + marker.getTitle()+", con id: "+id, Toast.LENGTH_SHORT).show();
                        android.support.v4.app.FragmentTransaction trans = getActivity().getSupportFragmentManager().beginTransaction();
                        DetailFragment detailfrag = DetailFragment.newInstance(id);
                        trans.replace(R.id.container, detailfrag);
                        trans.addToBackStack(null);
                        trans.commit();
                    }
                });
                init();
            }
        }
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        mMap = null;
        markers.clear();
    }

    private MainActivity mainActivity;
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

    }

    public void init(){
        try{
            for(int i=0; i<locations.length();i++)
            {
                String nome = locations.getJSONObject(i).getJSONObject("localita").getString("valore");
                String id = locations.getJSONObject(i).getString("_id");
                double latitude = locations.getJSONObject(i).getJSONObject("coordinate").getJSONObject("latitudine").getDouble("valore");
                double longitude = locations.getJSONObject(i).getJSONObject("coordinate").getJSONObject("longitudine").getDouble("valore");

                String locale = getActivity().getApplicationContext().getResources().getConfiguration().locale.getLanguage().toUpperCase();
                String classificazione = Data.getStringLocaleOrDefault(locations.getJSONObject(i).getJSONObject("qualita_acque").getJSONObject("classificazione"), "valore", locale);
                String nomeCampoClassificazione = Data.getStringLocaleOrDefault(locations.getJSONObject(i).getJSONObject("qualita_acque"), "nome_campo", locale);
                int stelle = locations.getJSONObject(i).getJSONObject("qualita_acque").getJSONObject("stelle").getInt("valore");
                IconGenerator iconGenerator = new IconGenerator(getActivity().getApplicationContext());
                ImageView markerImageView = new ImageView(getActivity().getApplicationContext());
                int markerDimension = (int) getResources().getDimension(R.dimen.marker_dimension);
                markerImageView.setLayoutParams(new ViewGroup.LayoutParams(markerDimension, markerDimension));
                int markerPadding = (int) getResources().getDimension(R.dimen.marker_padding);
                markerImageView.setPadding(markerPadding, markerPadding, markerPadding, markerPadding);

                switch(stelle)
                {
                    case 0:
                        iconGenerator.setColor(Color.RED);
                        markerImageView.setImageResource(R.drawable.stelle0);
                        break;
                    case 1:
                        iconGenerator.setColor(Color.YELLOW);
                        markerImageView.setImageResource(R.drawable.stelle1);
                        break;
                    case 2:
                        iconGenerator.setColor(Color.GREEN);
                        markerImageView.setImageResource(R.drawable.stelle2);
                        break;
                    case 3:
                        iconGenerator.setColor(Color.BLUE);
                        markerImageView.setImageResource(R.drawable.stelle3);
                        break;
                    default:
                        iconGenerator.setColor(Color.GRAY);
                        markerImageView.setImageResource(R.drawable.divieto);
                }

                iconGenerator.setContentView(markerImageView);
                Bitmap generatedIcon = iconGenerator.makeIcon();

                Marker tempMarker =  mMap.addMarker(new MarkerOptions()
                        .position(new LatLng(latitude, longitude))
                        .title(nome)
                        .snippet(nomeCampoClassificazione + ": " + classificazione)
                        .icon(BitmapDescriptorFactory.fromBitmap(generatedIcon)));
                markers.put(tempMarker, id);
            }
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(45.74, 10.04), 11));
        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
    }
}