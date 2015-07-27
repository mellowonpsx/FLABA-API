package chube.flaba;

import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.LightingColorFilter;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridLayout;
import android.widget.GridView;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONObject;

import chube.flaba.Model.Data;

/**
 * Created by chube on 23/07/2015.
 */
public class DetailFragment extends Fragment {
    private String id;

    public static DetailFragment newInstance(String str) {
        DetailFragment myFragment = new DetailFragment();
        Bundle args = new Bundle();
        args.putString("str", str);
        myFragment.setArguments(args);
        return myFragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.detail_fragment, container, false);
        id = getArguments().getString("str", "0");
        JSONObject location = Data.getLocation(id);
        final String locale = getActivity().getApplicationContext().getResources().getConfiguration().locale.getLanguage().toUpperCase();
        TextView tv;
        ImageView iv;
        boolean servizio_attivo;
        try{
            // qualita_acque
            tv = (TextView) v.findViewById(R.id.class_label);
            tv.setText(Data.getStringLocaleOrDefault(location.getJSONObject("qualita_acque").getJSONObject("classificazione"),"nome_campo",locale)+": ");
            tv = (TextView) v.findViewById(R.id.class_value);
            tv.setText(Data.getStringLocaleOrDefault(location.getJSONObject("qualita_acque").getJSONObject("classificazione"),"valore",locale));
            iv = (ImageView) v.findViewById(R.id.class_img);
            int stelle = location.getJSONObject("qualita_acque").getJSONObject("stelle").getInt("valore");
            switch(stelle) {
                case 3:
                    iv.setBackgroundResource(R.drawable.stelle3);
                    break;
                case 2:
                    iv.setBackgroundResource(R.drawable.stelle2);
                    break;
                case 1:
                    iv.setBackgroundResource(R.drawable.stelle1);
                    break;
                case 0:
                    iv.setBackgroundResource(R.drawable.stelle0);
                    break;
                default:
                    iv.setBackgroundResource(R.drawable.divieto);
                    break;
            }
            // id
            tv = (TextView) v.findViewById(R.id.id_label);
            tv.setText(Data.getStringLocaleOrDefault(location.getJSONObject("id"),"nome_campo",locale)+": ");
            tv = (TextView) v.findViewById(R.id.id_value);
            tv.setText(location.getJSONObject("id").getString("valore"));
            // localita
            tv = (TextView) v.findViewById(R.id.loc_label);
            tv.setText(Data.getStringLocaleOrDefault(location.getJSONObject("localita"),"nome_campo",locale)+": ");
            tv = (TextView) v.findViewById(R.id.loc_value);
            tv.setText(location.getJSONObject("localita").getString("valore"));
            // comune
            tv = (TextView) v.findViewById(R.id.com_label);
            tv.setText(Data.getStringLocaleOrDefault(location.getJSONObject("comune"),"nome_campo",locale)+": ");
            tv = (TextView) v.findViewById(R.id.com_value);
            tv.setText(location.getJSONObject("comune").getString("valore"));
            // provincia
            tv = (TextView) v.findViewById(R.id.prov_value);
            tv.setText(Data.getStringLocaleOrDefault(location.getJSONObject("provincia"),"nome_campo",locale)+": ");
            tv = (TextView) v.findViewById(R.id.prov_label);
            tv.setText(location.getJSONObject("provincia").getString("valore"));
            // coordinate
            tv = (TextView) v.findViewById(R.id.lat_label);
            tv.setText(Data.getStringLocaleOrDefault(location.getJSONObject("coordinate").getJSONObject("latitudine"),"nome_campo",locale)+": ");
            tv = (TextView) v.findViewById(R.id.lat_value);
            double latitudine = location.getJSONObject("coordinate").getJSONObject("latitudine").getDouble("valore");
            tv.setText(String.valueOf(latitudine));
            tv = (TextView) v.findViewById(R.id.long_label);
            tv.setText(Data.getStringLocaleOrDefault(location.getJSONObject("coordinate").getJSONObject("longitudine"),"nome_campo",locale)+": ");
            tv = (TextView) v.findViewById(R.id.long_value);
            double longitudine = location.getJSONObject("coordinate").getJSONObject("longitudine").getDouble("valore");
            tv.setText(String.valueOf(longitudine));
            // descrizione_area
            tv = (TextView) v.findViewById(R.id.desc_label);
            tv.setText(Data.getStringLocaleOrDefault(location.getJSONObject("descrizione_area"),"nome_campo",locale)+": ");
            // accesso_pubblico
            viewServizio(v, R.id.s1_label, R.id.s1_value, R.id.s1_img,
                    Data.getStringLocaleOrDefault(location.getJSONObject("descrizione_area").getJSONObject("accesso_pubblico"), "nome_campo", locale)+": ",
                    Data.getStringLocaleOrDefault(location.getJSONObject("descrizione_area").getJSONObject("accesso_pubblico").getJSONObject("descrizione"), "valore",locale),
                    location.getJSONObject("descrizione_area").getJSONObject("accesso_pubblico").getBoolean("servizio_attivo"));
            // handicap
            viewServizio(v, R.id.s2_label, R.id.s2_value, R.id.s2_img,
                    Data.getStringLocaleOrDefault(location.getJSONObject("descrizione_area").getJSONObject("handicap"), "nome_campo", locale)+": ",
                    Data.getStringLocaleOrDefault(location.getJSONObject("descrizione_area").getJSONObject("handicap").getJSONObject("descrizione"), "valore",locale),
                    location.getJSONObject("descrizione_area").getJSONObject("handicap").getBoolean("servizio_attivo"));
            // parcheggio
            viewServizio(v, R.id.s3_label, R.id.s3_value, R.id.s3_img,
                    Data.getStringLocaleOrDefault(location.getJSONObject("descrizione_area").getJSONObject("parcheggio"), "nome_campo", locale)+": ",
                    Data.getStringLocaleOrDefault(location.getJSONObject("descrizione_area").getJSONObject("parcheggio").getJSONObject("descrizione"), "valore",locale),
                    location.getJSONObject("descrizione_area").getJSONObject("parcheggio").getBoolean("servizio_attivo"));
            // servizi_igienici
            viewServizio(v, R.id.s4_label, R.id.s4_value, R.id.s4_img,
                    Data.getStringLocaleOrDefault(location.getJSONObject("descrizione_area").getJSONObject("servizi_igienici"), "nome_campo", locale)+": ",
                    Data.getStringLocaleOrDefault(location.getJSONObject("descrizione_area").getJSONObject("servizi_igienici").getJSONObject("descrizione"), "valore",locale),
                    location.getJSONObject("descrizione_area").getJSONObject("servizi_igienici").getBoolean("servizio_attivo"));
            // area_pubblica
            viewServizio(v, R.id.s5_label, R.id.s5_value, R.id.s5_img,
                    Data.getStringLocaleOrDefault(location.getJSONObject("descrizione_area").getJSONObject("area_pubblica"), "nome_campo", locale)+": ",
                    Data.getStringLocaleOrDefault(location.getJSONObject("descrizione_area").getJSONObject("area_pubblica").getJSONObject("descrizione"), "valore",locale),
                    location.getJSONObject("descrizione_area").getJSONObject("area_pubblica").getBoolean("servizio_attivo"));
            // area_verde
            viewServizio(v, R.id.s6_label, R.id.s6_value, R.id.s6_img,
                    Data.getStringLocaleOrDefault(location.getJSONObject("descrizione_area").getJSONObject("area_verde"), "nome_campo", locale)+": ",
                    Data.getStringLocaleOrDefault(location.getJSONObject("descrizione_area").getJSONObject("area_verde").getJSONObject("descrizione"), "valore",locale),
                    location.getJSONObject("descrizione_area").getJSONObject("area_verde").getBoolean("servizio_attivo"));
            // area_privata
            viewServizio(v, R.id.s7_label, R.id.s7_value, R.id.s7_img,
                    Data.getStringLocaleOrDefault(location.getJSONObject("descrizione_area").getJSONObject("area_privata"), "nome_campo", locale)+": ",
                    Data.getStringLocaleOrDefault(location.getJSONObject("descrizione_area").getJSONObject("area_privata").getJSONObject("descrizione"), "valore",locale),
                    location.getJSONObject("descrizione_area").getJSONObject("area_privata").getBoolean("servizio_attivo"));
            // area_pic_nic
            viewServizio(v, R.id.s8_label, R.id.s8_value, R.id.s8_img,
                    Data.getStringLocaleOrDefault(location.getJSONObject("descrizione_area").getJSONObject("area_pic_nic"), "nome_campo", locale)+": ",
                    Data.getStringLocaleOrDefault(location.getJSONObject("descrizione_area").getJSONObject("area_pic_nic").getJSONObject("descrizione"), "valore",locale),
                    location.getJSONObject("descrizione_area").getJSONObject("area_pic_nic").getBoolean("servizio_attivo"));
            // area_giochi
            viewServizio(v, R.id.s9_label, R.id.s9_value, R.id.s9_img,
                    Data.getStringLocaleOrDefault(location.getJSONObject("descrizione_area").getJSONObject("area_giochi"), "nome_campo", locale)+": ",
                    Data.getStringLocaleOrDefault(location.getJSONObject("descrizione_area").getJSONObject("area_giochi").getJSONObject("descrizione"), "valore",locale),
                    location.getJSONObject("descrizione_area").getJSONObject("area_giochi").getBoolean("servizio_attivo"));
            // bar_ristorante
            viewServizio(v, R.id.s10_label, R.id.s10_value, R.id.s10_img,
                    Data.getStringLocaleOrDefault(location.getJSONObject("descrizione_area").getJSONObject("bar_ristorante"), "nome_campo", locale)+": ",
                    Data.getStringLocaleOrDefault(location.getJSONObject("descrizione_area").getJSONObject("bar_ristorante").getJSONObject("descrizione"), "valore",locale),
                    location.getJSONObject("descrizione_area").getJSONObject("bar_ristorante").getBoolean("servizio_attivo"));
            // attracco_barche
            viewServizio(v, R.id.s11_label, R.id.s11_value, R.id.s11_img,
                    Data.getStringLocaleOrDefault(location.getJSONObject("descrizione_area").getJSONObject("attracco_barche"), "nome_campo", locale)+": ",
                    Data.getStringLocaleOrDefault(location.getJSONObject("descrizione_area").getJSONObject("attracco_barche").getJSONObject("descrizione"), "valore",locale),
                    location.getJSONObject("descrizione_area").getJSONObject("attracco_barche").getBoolean("servizio_attivo"));
            // piste_ciclopedonali
            viewServizio(v, R.id.s12_label, R.id.s12_value, R.id.s12_img,
                    Data.getStringLocaleOrDefault(location.getJSONObject("descrizione_area").getJSONObject("piste_ciclopedonali"), "nome_campo", locale)+": ",
                    Data.getStringLocaleOrDefault(location.getJSONObject("descrizione_area").getJSONObject("piste_ciclopedonali").getJSONObject("descrizione"), "valore",locale),
                    location.getJSONObject("descrizione_area").getJSONObject("piste_ciclopedonali").getBoolean("servizio_attivo"));
            // strutture_sportive
            viewServizio(v, R.id.s13_label, R.id.s13_value, R.id.s13_img,
                    Data.getStringLocaleOrDefault(location.getJSONObject("descrizione_area").getJSONObject("strutture_sportive"), "nome_campo", locale)+": ",
                    Data.getStringLocaleOrDefault(location.getJSONObject("descrizione_area").getJSONObject("strutture_sportive").getJSONObject("descrizione"), "valore",locale),
                    location.getJSONObject("descrizione_area").getJSONObject("strutture_sportive").getBoolean("servizio_attivo"));
            // sport_praticabili
            tv = (TextView) v.findViewById(R.id.sport_label);
            tv.setText(Data.getStringLocaleOrDefault(location.getJSONObject("descrizione_area").getJSONObject("sport_praticabili"),"nome_campo",locale)+": ");
            tv = (TextView) v.findViewById(R.id.sport_value);
            JSONArray sportPraticabili = location.getJSONObject("descrizione_area").getJSONObject("sport_praticabili").getJSONArray("lista");
            String sportValore = "";
            for( int j=0; j< sportPraticabili.length(); j++) {
                JSONObject thisSport = sportPraticabili.getJSONObject(j);
                String nomeSport = Data.getStringLocaleOrDefault(thisSport, locale);
                sportValore = sportValore+nomeSport+" ,";
            }
            sportValore = sportValore.substring(0, sportValore.length()-2);
            tv.setText(sportValore);
            // informazioni_utili
            tv = (TextView) v.findViewById(R.id.info_label);
            tv.setText(Data.getStringLocaleOrDefault(location.getJSONObject("informazioni_utili"),"nome_campo",locale)+": ");
            // guardia_medica
            viewInformazione(v, R.id.u1_label, -1, R.id.u1_ind_label, R.id.u1_tel_label, -1, R.id.u1_ind_value, R.id.u1_tel_value,
                    Data.getStringLocaleOrDefault(location.getJSONObject("informazioni_utili").getJSONObject("guardia_medica"), "nome_campo", locale),
                    null,
                    Data.getStringLocaleOrDefault(location.getJSONObject("informazioni_utili").getJSONObject("guardia_medica").getJSONObject("indirizzo"), "nome_campo", locale)+": ",
                    Data.getStringLocaleOrDefault(location.getJSONObject("informazioni_utili").getJSONObject("guardia_medica").getJSONObject("telefono"), "nome_campo", locale)+": ",
                    null,
                    location.getJSONObject("informazioni_utili").getJSONObject("guardia_medica").getJSONObject("indirizzo").getString("valore"),
                    location.getJSONObject("informazioni_utili").getJSONObject("guardia_medica").getJSONObject("telefono").getString("valore"));
            // pronto_soccorso
            viewInformazione(v, R.id.u2_label, R.id.u2_nome_label, R.id.u2_ind_label, R.id.u2_tel_label, R.id.u2_nome_value, R.id.u2_ind_value, R.id.u2_tel_value,
                    Data.getStringLocaleOrDefault(location.getJSONObject("informazioni_utili").getJSONObject("pronto_soccorso"), "nome_campo", locale),
                    Data.getStringLocaleOrDefault(location.getJSONObject("informazioni_utili").getJSONObject("pronto_soccorso").getJSONObject("nome"), "nome_campo", locale)+": ",
                    Data.getStringLocaleOrDefault(location.getJSONObject("informazioni_utili").getJSONObject("pronto_soccorso").getJSONObject("indirizzo"), "nome_campo", locale)+": ",
                    Data.getStringLocaleOrDefault(location.getJSONObject("informazioni_utili").getJSONObject("pronto_soccorso").getJSONObject("telefono"), "nome_campo", locale)+": ",
                    location.getJSONObject("informazioni_utili").getJSONObject("pronto_soccorso").getJSONObject("nome").getString("valore"),
                    location.getJSONObject("informazioni_utili").getJSONObject("pronto_soccorso").getJSONObject("indirizzo").getString("valore"),
                    location.getJSONObject("informazioni_utili").getJSONObject("pronto_soccorso").getJSONObject("telefono").getString("valore"));
            // farmacia
            viewInformazione(v, R.id.u3_label, R.id.u3_nome_label, R.id.u3_ind_label, R.id.u3_tel_label, R.id.u3_nome_value, R.id.u3_ind_value, R.id.u3_tel_value,
                    Data.getStringLocaleOrDefault(location.getJSONObject("informazioni_utili").getJSONObject("farmacia"), "nome_campo", locale),
                    Data.getStringLocaleOrDefault(location.getJSONObject("informazioni_utili").getJSONObject("farmacia").getJSONObject("nome"), "nome_campo", locale)+": ",
                    Data.getStringLocaleOrDefault(location.getJSONObject("informazioni_utili").getJSONObject("farmacia").getJSONObject("indirizzo"), "nome_campo", locale)+": ",
                    Data.getStringLocaleOrDefault(location.getJSONObject("informazioni_utili").getJSONObject("farmacia").getJSONObject("telefono"), "nome_campo", locale)+": ",
                    location.getJSONObject("informazioni_utili").getJSONObject("farmacia").getJSONObject("nome").getString("valore"),
                    location.getJSONObject("informazioni_utili").getJSONObject("farmacia").getJSONObject("indirizzo").getString("valore"),
                    location.getJSONObject("informazioni_utili").getJSONObject("farmacia").getJSONObject("telefono").getString("valore"));
            // polizia_provinciale
            viewInformazione(v, R.id.u5_label, -1, R.id.u5_ind_label, R.id.u5_tel_label, -1, R.id.u5_ind_value, R.id.u5_tel_value,
                    Data.getStringLocaleOrDefault(location.getJSONObject("informazioni_utili").getJSONObject("polizia_provinciale"), "nome_campo", locale),
                    null,
                    Data.getStringLocaleOrDefault(location.getJSONObject("informazioni_utili").getJSONObject("polizia_provinciale").getJSONObject("indirizzo"), "nome_campo", locale)+": ",
                    Data.getStringLocaleOrDefault(location.getJSONObject("informazioni_utili").getJSONObject("polizia_provinciale").getJSONObject("telefono"), "nome_campo", locale)+": ",
                    null,
                    location.getJSONObject("informazioni_utili").getJSONObject("polizia_provinciale").getJSONObject("indirizzo").getString("valore"),
                    location.getJSONObject("informazioni_utili").getJSONObject("polizia_provinciale").getJSONObject("telefono").getString("valore"));
            // carabinieri
            viewInformazione(v, R.id.u6_label, -1, R.id.u6_ind_label, R.id.u6_tel_label, -1, R.id.u6_ind_value, R.id.u6_tel_value,
                    Data.getStringLocaleOrDefault(location.getJSONObject("informazioni_utili").getJSONObject("carabinieri"), "nome_campo", locale),
                    null,
                    Data.getStringLocaleOrDefault(location.getJSONObject("informazioni_utili").getJSONObject("carabinieri").getJSONObject("indirizzo"), "nome_campo", locale)+": ",
                    Data.getStringLocaleOrDefault(location.getJSONObject("informazioni_utili").getJSONObject("carabinieri").getJSONObject("telefono"), "nome_campo", locale)+": ",
                    null,
                    location.getJSONObject("informazioni_utili").getJSONObject("carabinieri").getJSONObject("indirizzo").getString("valore"),
                    location.getJSONObject("informazioni_utili").getJSONObject("carabinieri").getJSONObject("telefono").getString("valore"));
            // polizia_locale
            viewInformazione(v, R.id.u4_label, -1, R.id.u4_ind_label, R.id.u4_tel_label, -1, R.id.u4_ind_value, R.id.u4_tel_value,
                    Data.getStringLocaleOrDefault(location.getJSONObject("informazioni_utili").getJSONObject("polizia_locale"), "nome_campo", locale),
                    null,
                    Data.getStringLocaleOrDefault(location.getJSONObject("informazioni_utili").getJSONObject("polizia_locale").getJSONObject("indirizzo"), "nome_campo", locale)+": ",
                    Data.getStringLocaleOrDefault(location.getJSONObject("informazioni_utili").getJSONObject("polizia_locale").getJSONObject("telefono"), "nome_campo", locale)+": ",
                    null,
                    location.getJSONObject("informazioni_utili").getJSONObject("polizia_locale").getJSONObject("indirizzo").getString("valore"),
                    location.getJSONObject("informazioni_utili").getJSONObject("polizia_locale").getJSONObject("telefono").getString("valore"));
            // informazioni_utili
            tv = (TextView) v.findViewById(R.id.image_label);
            tv.setText(Data.getStringLocaleOrDefault(location.getJSONObject("immagini"),"nome_campo",locale)+": ");
            JSONArray listaImmagini = location.getJSONObject("immagini").getJSONArray("lista");
            for(int j=0; j<listaImmagini.length(); j++) {
                JSONObject thisImmagine = listaImmagini.getJSONObject(j);
                String filename = thisImmagine.getString("filename");
                String url = Flaba.SERVER_API_BASEURL+"/no-auth/getfile/"+filename;
                //chiama con picasso
                ImageView tempIv = new ImageView(getActivity().getApplicationContext());
                LinearLayout linearLayout = (LinearLayout) v.findViewById(R.id.images_layout);
                linearLayout.addView(tempIv);
                Picasso.with(getActivity().getApplicationContext()).setIndicatorsEnabled(false);
                Picasso.with(getActivity().getApplicationContext()).load(url).into(tempIv);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return v;
    }

    public static void viewServizio(View v, int id_label, int id_valore, int id_img, String label, String valore, boolean servizio_attivo)
    {
        TextView tv;
        ImageView iv;
        tv = (TextView) v.findViewById(id_label);
        tv.setText(label);
        tv = (TextView) v.findViewById(id_valore);
        tv.setText(valore);
        iv = (ImageView) v.findViewById(id_img);
        if(servizio_attivo == false)
        {
            iv.getBackground().setColorFilter(Color.LTGRAY, PorterDuff.Mode.LIGHTEN);
        }
    }

    public static void viewInformazione(View v, int id_label, int id_label_nome, int id_label_indirizzo, int id_label_telefono, int id_valore_nome, int id_valore_indirizzo, int id_valore_telefono, String label, String label_nome, String label_indirizzo, String label_telefono, String valore_nome, String valore_indirizzo, String valore_telefono)
    {
        TextView tv;
        ImageView iv;
        tv = (TextView) v.findViewById(id_label);
        tv.setText(label);
        if(label_nome != null && valore_nome != null) {
            tv = (TextView) v.findViewById(id_label_nome);
            tv.setText(label_nome);
            tv = (TextView) v.findViewById(id_valore_nome);
            tv.setText(valore_nome);
        }
        tv = (TextView) v.findViewById(id_label_indirizzo);
        tv.setText(label_indirizzo);
        tv = (TextView) v.findViewById(id_valore_indirizzo);
        tv.setText(valore_indirizzo);
        tv = (TextView) v.findViewById(id_label_telefono);
        tv.setText(label_telefono);
        tv = (TextView) v.findViewById(id_valore_telefono);
        tv.setText(valore_telefono);
    }
}
