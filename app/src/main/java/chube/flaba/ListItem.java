package chube.flaba;

/**
 * Created by chube on 24/07/2015.
 */
public class ListItem {
    private String Id;
    private String LocazioneLabel;
    private String Locazione;
    private String ProvinciaLabel;
    private String Provincia;
    private String Distanza;

    public ListItem(String id, String ll, String l, String pl, String p, String d) {
        Id = id;
        LocazioneLabel = ll;
        Locazione = l;
        ProvinciaLabel = pl;
        Provincia = p;
        Distanza = d;
    }

    public String getId() {
        return Id;
    }

    public void setId(String s) {
        Id = s;
    }
    
    public String getLocazioneLabel() {
        return LocazioneLabel;
    }

    public void setLocazioneLabel(String s) {
        LocazioneLabel = s;
    }
    
    public String getLocazione() {
        return Locazione;
    }

    public void setLocazione(String s) {
        Locazione = s;
    }

    public String getProvinciaLabel() {
        return ProvinciaLabel;
    }

    public void setProvinciaLabel(String s) {
        ProvinciaLabel = s;
    }

    public String getProvincia() {
        return Provincia;
    }

    public void setProvincia(String s) {
        Provincia = s;
    }

    public String getDistanza() {
        return Distanza;
    }

    public void setDistanza(String s) {
        Distanza = s;
    }
}

