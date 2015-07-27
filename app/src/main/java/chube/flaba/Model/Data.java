package chube.flaba.Model;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import chube.flaba.Flaba;

/**
 * Created by chube on 14/07/2015.
 */
public class Data {

    final static String defaultResponse = "-";

    public static String getStringLocaleOrDefault(JSONObject obj, String field, String locale)
    {
        String result = defaultResponse;
        try {
            result = obj.getJSONObject(field).getString(locale);
        } catch (JSONException e)
        {
            try {
                String defaultLang = obj.getJSONObject(field).getString("default");
                result = obj.getJSONObject(field).getString(defaultLang);
            }
            catch (JSONException e2)
            {
                e.printStackTrace();
            }
        }
        return result;
    }

    public static String getStringLocaleOrDefault(JSONObject obj, String locale)
    {
        String result = defaultResponse;
        try {
            result = obj.getString(locale);
        } catch (JSONException e)
        {
            try {
                String defaultLang = obj.getString("default");
                result = obj.getString(defaultLang);
            }
            catch (JSONException e2)
            {
                e.printStackTrace();
            }
        }
        return result;
    }

    public static JSONObject getLocation(String _id) {
        JSONArray locations = Flaba.globalLocations;
        try {
            for (int i = 0; i < locations.length(); i++) {
                JSONObject thisLocation = locations.getJSONObject(i);
                String id = thisLocation.getString("_id");
                if(id.equals(_id)) {
                    return thisLocation;
                }
            }
        } catch (JSONException e)
        {
            e.printStackTrace();
        }
        return null;
    }
}

