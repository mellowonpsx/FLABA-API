package chube.flaba;

import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.FileOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import android.content.Context;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import android.util.Log;




/**
 * Created by chube on 24/06/2015.
 */

public class ExampleFragment extends Fragment {
    TextView testo;
    String idi;
    public static ExampleFragment newInstance(String someInt) {
        ExampleFragment myFragment = new ExampleFragment();

        Bundle args = new Bundle();
        args.putString("someInt", someInt);
        myFragment.setArguments(args);

        return myFragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.example_fragment, container, false);
        testo = (TextView)v.findViewById(R.id.testo);

        return v;
    }
/*
    public String readJason() {
        StringBuilder builder = new StringBuilder();
        HttpClient client = new DefaultHttpClient();
        HttpGet httpGet = new HttpGet("https://bugzilla.mozilla.org/rest/bug/35");
        try {
            HttpResponse response = client.execute(httpGet);
            StatusLine statusLine = response.getStatusLine();
            int statusCode = statusLine.getStatusCode();
            if (statusCode == 200) {
                HttpEntity entity = response.getEntity();
                InputStream content = entity.getContent();
                BufferedReader reader = new BufferedReader(new InputStreamReader(content));
                String line;
                while ((line = reader.readLine()) != null) {
                    builder.append(line);
                }
            }
        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return builder.toString();
    }

    public JSONObject writeJSON() {
        JSONObject object = new JSONObject();
        try {
            object.put("player", "bobby zanna");
            object.put("team", "eagle");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        String FILENAME = "secondo.json";
        String string = object.toString();
        try{
            FileOutputStream fos = getActivity().openFileOutput(FILENAME, Context.MODE_PRIVATE);
            fos.write(string.getBytes());
            fos.close();
        }
        catch (FileNotFoundException e){
            e.printStackTrace();
        }
        catch (IOException e) {
            e.printStackTrace();
        }

        return object;
    }*/

    public JSONObject load() {
        JSONObject object=null;
        String FILENAME = "secondo.json";
        byte[] buffer = new byte[100];
        String str = new String();
        try {
            FileInputStream fis = getActivity().openFileInput(FILENAME);
            fis.read(buffer);
            fis.close();
            str = new String(buffer);
            object = new JSONObject(str);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        catch (JSONException e) {
            e.printStackTrace();
        }
        return object;
    }


//
    @Override
    public void onActivityCreated (Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
//        testo.setText("CIaooooooooooooooooooooooo");
//      try {
//          //JSONObject json = new JSONObject(readJason());
//          //JSONObject json = writeJSON();
//          JSONObject json = load();
//          Log.i("JSON_Fragment", json.toString());
//          //testo.setText(json.getString("nickname"));
//          //testo.setText(json.getString("team"));
//          testo.setText(json.toString(3));
//        }
//        catch (JSONException e) {
//            e.printStackTrace();
//        }
        idi = getArguments().getString("someInt", "0");
        testo.setText(idi);
    }
}

