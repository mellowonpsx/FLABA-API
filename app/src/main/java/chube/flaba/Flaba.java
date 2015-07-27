package chube.flaba;

import android.app.Application;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;
import com.squareup.okhttp.ResponseBody;
import com.squareup.picasso.Picasso;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.File;
import java.io.InputStreamReader;
import java.util.Date;
import java.text.SimpleDateFormat;
import java.util.Iterator;
import java.util.Locale;

import android.support.v4.app.NotificationCompat;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;


/**
 * Created by chube on 19/07/2015.
 */
public class Flaba extends Application {
    private OkHttpClient okHttpClient;
    public static final String JSON_TIMESTAMP_PREFERENCE = "json_location_timestamp";
    public static final String LOCALE_PREFERENCE = "locale_preference";
    public static final String APP_UPDATE_PREFERENCE = "update_preference";
    private Date jsonTimestampLocale;
    private final String JSON_FILE_NAME = "locations.json";
    private final String JSON_TEMPFILE_NAME = "temp.locations.json";
    private final String LOG_TAG = "FLABA_UPDATE_ASYNC";
    public final static String SERVER_API_BASEURL = "https://flaba-mellowonpsx.rhcloud.com";
    private final int NOTIFICATION_ID = 1;
    private SharedPreferences sharedPreferences;
    public Context context;
    public static JSONArray globalLocations = new JSONArray();

    @Override
    public void onCreate() {
        super.onCreate();
        okHttpClient = new OkHttpClient();
        Context context = getApplicationContext();

        try{
            FileInputStream in;
            try {
                in = openFileInput(JSON_FILE_NAME);
            } catch(FileNotFoundException fnfe) {
                openFileOutput(JSON_FILE_NAME, Context.MODE_PRIVATE).close();
                in = openFileInput(JSON_FILE_NAME);
            }
            InputStreamReader inputStreamReader = new InputStreamReader(in);
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                sb.append(line);
            }
            if(sb.length() == 0)
            {
                sb.append("[]");
            }
            globalLocations = new JSONArray(sb.toString());
        }
        catch(Exception e){
            //globalLocations = new JSONArray();
            Log.d(LOG_TAG, "eccezione sulla lettura/parse del file json delle location");
            e.printStackTrace();
            return;
        }

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        String localeName = sharedPreferences.getString(LOCALE_PREFERENCE, "default");
//        String localeName = "it";
        if(localeName.equals("default"))
        {
            //do nothing
//            String locale = getApplicationContext().getResources().getConfiguration().locale.getLanguage().toUpperCase();
//            sharedPreferences.edit().putString(LOCALE_PREFERENCE, locale).commit();
        }
        else
        {
            Locale locale = new Locale(localeName.toLowerCase());
            Locale.setDefault(locale);
            Configuration config = new Configuration();
            config.locale = locale;
            context.getApplicationContext().getResources().updateConfiguration(config, null);
        }

        String connectionPreference = sharedPreferences.getString(APP_UPDATE_PREFERENCE, "WIFI+3G");
        //Log.e(LOG_TAG, connectionPreference);
        ConnectivityManager connectivityManager = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = connectivityManager.getActiveNetworkInfo();

        boolean isConnected = activeNetwork != null && activeNetwork.isConnected();
        boolean okUpdate = false;

        if(connectionPreference.equals("WIFI"))
        {
            boolean isWiFi = activeNetwork.getType() == ConnectivityManager.TYPE_WIFI;
            if(isWiFi) {
                okUpdate = true;
                Log.d(LOG_TAG, "modalità WIFI only");
            }
//            else
//            {
//                //okUpdate = false;
//                //Log.d(LOG_TAG, "modalità WIFI only, connessione non disponbile");
//            }
        } else {
            okUpdate = true;
            Log.d(LOG_TAG, "modalità WIFI+3G");
        }

        if(isConnected && okUpdate)
        {
            jsonTimestampLocale = new Date(sharedPreferences.getLong(JSON_TIMESTAMP_PREFERENCE, new Long(0)));
            new DownloadFilesTask(context).execute();
        }
        else
        {
            Log.d(LOG_TAG, "connettività assente o non valida");
        }

    }

    private class DownloadFilesTask extends AsyncTask<Void, Void, Long> {
        private Context context;

        public DownloadFilesTask(Context givenContext) {
            context = givenContext;
        }

        protected Long doInBackground(Void... parameters) {
            try{
                run(context);
            }
            catch(Exception e ) {
                e.printStackTrace();
            }
            return new Long(0);
        }

        protected void onProgressUpdate(Void... progress) {
//            setProgressPercent(progress[0]);
        }

        protected void onPostExecute(Long result) {
            //
//            showDialog("Downloaded " + result + " bytes");
        }
    }

    public void run(Context context) throws Exception {
        // 1- check dell'header
        // 2- se timestamp più nuovo o uguale -> nulla
        // 3- se timestamp più vecchio
            // -> download json
            // -> download immagini
            // -> aggiorno timestamp preference

        Request headRequest = new Request.Builder()
                .url(SERVER_API_BASEURL + "/no-auth/locations")
                .head()
                .build();

        Response headResponse = okHttpClient.newCall(headRequest).execute();
        if (!headResponse.isSuccessful())
        {
            //impossibile recuperare header:
            // -> server down
            // -> perdita di connessione
            Log.d(LOG_TAG, "impossibile recuperare header");
            return;
        }

        String headerTimestamp = headResponse.headers().get("last-modified");
        if(headerTimestamp == null)
        {
            Log.d(LOG_TAG, "timestamp non presente nell\'header");
            return;
        }

        SimpleDateFormat sdf = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss zzz", Locale.ENGLISH); //formato timestamp
        Date jsonTimestampServer = sdf.parse(headerTimestamp);
        if(jsonTimestampServer.after(jsonTimestampLocale))
        {
            // 3- se timestamp più vecchio
            // -> download json
            // -> download immagini
            // -> aggiorno timestamp preference
            Request request = new Request.Builder()
                    .url(SERVER_API_BASEURL + "/no-auth/locations")
                    .build();

            Response response = okHttpClient.newCall(request).execute();
            if (!response.isSuccessful())
            {
                //impossibile recuperare locations:
                // -> server down
                // -> perdita di connessione
                Log.d(LOG_TAG, "impossibile recuperare locations");
                return;
            }

            JSONArray locations;
            try{
                ResponseBody body = response.body();
                String stringBody = body.string();
                JSONObject jsonBody = new JSONObject(stringBody);
                locations = jsonBody.getJSONObject("data").getJSONArray("locations");
            }
            catch(Exception e){
                Log.d(LOG_TAG, "eccezione sulla generazione/parse di body");
                e.printStackTrace();
                return;
            }
            try{
                FileOutputStream fos = openFileOutput(JSON_TEMPFILE_NAME, Context.MODE_PRIVATE);
                fos.write(locations.toString().getBytes());
                fos.close();
            }
            catch (Exception e) {
                e.printStackTrace();
                Log.d(LOG_TAG, "impossibile scrivere il file temporaneo delle locations");
                return;
            }

            for (int i=0; i<locations.length(); i++)
            {
                JSONObject thisLocation = locations.getJSONObject(i);
                JSONArray listaImmagini = thisLocation.getJSONObject("immagini").getJSONArray("lista");
                for(int j=0; j<listaImmagini.length(); j++) {
                    JSONObject thisImmagine = listaImmagini.getJSONObject(j);
                    String filename = thisImmagine.getString("filename");
                    String url = SERVER_API_BASEURL+"/no-auth/getfile/"+filename;
                    //chiama con picasso
                    Log.d(LOG_TAG, "Dico a Picasso di recuperare "+url);
                    Picasso.with(context).setIndicatorsEnabled(true);
                    Picasso.with(context).load(url).fetch();
                }
            }

            //copio file in quello nuovo
            try{
                FileInputStream from = openFileInput(JSON_TEMPFILE_NAME);
                FileOutputStream to = openFileOutput(JSON_FILE_NAME, Context.MODE_PRIVATE);

                byte[] buffer = new byte[4096]; // To hold file contents
                int bytes_read; // How many bytes in buffer

                while ((bytes_read = from.read(buffer)) != -1) {
                    to.write(buffer, 0, bytes_read); // write
                }

                from.close();
                to.close();
                File file = new File(JSON_TEMPFILE_NAME);
                file.delete();
            }
            catch (Exception e) {
                e.printStackTrace();
                Log.d(LOG_TAG, "impossibile copiare o cancellare il file temporaneo in quello nuovo");
                return;
            }

            //aggiorno timestamp
            sharedPreferences.edit().putLong(JSON_TIMESTAMP_PREFERENCE,  jsonTimestampServer.getTime()).commit();

            Intent intent = context.getPackageManager().getLaunchIntentForPackage(this.getPackageName());
            //intent.addCategory(Intent.CATEGORY_LAUNCHER);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP| Intent.FLAG_ACTIVITY_NEW_TASK);

            PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);

            //notifico all'utente l'avvenuto aggiornamento
            NotificationCompat.Builder notificationCompatBuilder = new NotificationCompat.Builder(this)
                    .setSmallIcon(R.drawable.ic_sync_black_48dp)
                    .setContentTitle(getString(R.string.app_name))
                    .setContentText(getString(R.string.testo_notifica))
                    .setDefaults(Notification.DEFAULT_ALL)
                    //.setContentIntent(pendingIntent)
                    //.addAction(R.drawable.ic_done_black_48dp, getString(R.string.riavvia), PendingIntent.getActivity(getBaseContext(), 0, new Intent(context, Flaba.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP| Intent.FLAG_ACTIVITY_NEW_TASK),0))
                    //.addAction(R.drawable.ic_clear_black_48dp, getString(R.string.dopo), PendingIntent.getActivity(context, 0, new Intent(), 0))
                    .setOnlyAlertOnce(true)
                    .setAutoCancel(true);
            NotificationCompat.BigTextStyle notificationCompaBigTextStyle= new NotificationCompat.BigTextStyle()
                    .setBigContentTitle(getString(R.string.app_name))
                    .bigText(getString(R.string.testo_notifica));
            notificationCompatBuilder.setStyle(notificationCompaBigTextStyle);
            NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.notify(NOTIFICATION_ID,notificationCompatBuilder.build());
        }
        else
        {
            Log.d(LOG_TAG, "timestamp locale già aggiornata");
            //solo per prove, resetta la preference!!!
            //sharedPreferences.edit().putLong(JSON_TIMESTAMP_PREFERENCE,  new Long(0)).commit(); //TO DO: DA TOGLIERE DOPO DEBUG
        }
        /*    Log.d("TIMESTAMP-valutazione", timestamp_app.toString()+" quella del server è dopo "+timestamp_server.toString());
            //iniziare download di file
            //quando finisce il download salvo la timestamp e faccio una notifica
            sp.edit().putLong(TIMESTAMP, timestamp_server.getTime()).commit();

            NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this)
                                                .setSmallIcon(R.drawable.ic_menu_check)
                                                .setContentTitle("FLABA")
                                                .setContentText("Dati aggiornati, si prega di riavviare l'applicazione") //multilingua?
                                                //.setWhen(System.currentTimeMillis())
                                                .setAutoCancel(true)
                                                .addAction(R.drawable.ic_menu_check, "Riavvia", PendingIntent.getActivity(context, 0, new Intent(), 0))
                                                .addAction(R.drawable.ic_menu_check, "Dopo", PendingIntent.getActivity(context, 0, new Intent(), 0));
                                                //.setContentIntent(PendingIntent.getActivity(context, 0, new Intent(), 0));
            NotificationCompat.BigTextStyle bigTextStyle= new NotificationCompat.BigTextStyle()
                                            .setBigContentTitle("FLABA")
                                            //.setSummaryText("Dati scaricati, si prega di...")
                                            .bigText("Dati scaricati correttamente, si prega di riavviare per aggiornare i dati dell'applicazione. " +
                                                     "Questo permetterà di utilizzare i dati più aggionrati dispobibili, in caso contrario" +
                                                     "i dati aggiornati verranno mostrati al prossimo avvio.");
            mBuilder.setStyle(bigTextStyle);
            NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            mNotificationManager.notify(0,mBuilder.build());
        }
        else
        {
            Log.d("TIMESTAMP-valutazione", timestamp_app.toString()+" quella del server è prima o uguale "+timestamp_server.toString());
            sp.edit().putLong(TIMESTAMP,  new Long(0)).commit();
        }
        //timestamp_server.getTime();
        //Log.d("TIMESTAMP", timestamp_server);
        //Log.d("TIMESTAMP-parsata", data.toString());
        //Tue, 21 Jul 2015 14:20:11 GMT
        //DateFormat.
        //DateTime.
        //Date timestamp = new Date(Date.parse(timestamp_server));
        //timestamp_server = DateFormat.parsetimestamp.toString();
        //Log.d("TIMESTAMP-aggiornata", timestamp_server);
                //sp.getString(TIMESTAMP, "");


//        Headers responseHeaders = response.headers();
//        for (int i = 0; i < responseHeaders.size(); i++) {
////            System.out.println(responseHeaders.name(i) + ": " + responseHeaders.value(i));
//            Log.e("HEADERS",responseHeaders.name(i) + ": " + responseHeaders.value(i));
//        }
//
////        System.out.println(response.body().string());
//        Log.e("BODY",response.body().string());*/
    }
}
