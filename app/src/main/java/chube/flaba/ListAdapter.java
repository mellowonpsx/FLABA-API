package chube.flaba;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.app.Activity;
import java.util.List;

/**
 * Created by chube on 24/07/2015.
 */
public class ListAdapter  extends ArrayAdapter {

    private Context context;

    public ListAdapter(Context context, List items) {
        super(context, android.R.layout.simple_list_item_1, items);
        this.context = context;
    }

    /**
     * Holder for the list items.
     */
    private class ViewHolder{
        TextView LocLab;
        TextView Loc;
        TextView ProvLab;
        TextView Prov;
        TextView Dist;
    }

    /**
     *
     * @param position
     * @param convertView
     * @param parent
     * @return
     */
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder = null;
        ListItem item = (ListItem)getItem(position);
        View viewToUse = null;

        // This block exists to inflate the settings list item conditionally based on whether
        // we want to support a grid or list view.
        LayoutInflater mInflater = (LayoutInflater) context
                .getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
        if (convertView == null) {
            viewToUse = mInflater.inflate(R.layout.row_list_fragment, null);
            holder = new ViewHolder();
            holder.LocLab = (TextView)viewToUse.findViewById(R.id.locazione_label);
            holder.Loc = (TextView)viewToUse.findViewById(R.id.locazione);
            holder.ProvLab = (TextView)viewToUse.findViewById(R.id.provincia_label);
            holder.Prov = (TextView)viewToUse.findViewById(R.id.provincia);
            holder.Dist = (TextView)viewToUse.findViewById(R.id.distanza);
            viewToUse.setTag(holder);
        } else {
            viewToUse = convertView;
            holder = (ViewHolder) viewToUse.getTag();
        }

        holder.LocLab.setText(item.getLocazioneLabel());
        holder.Loc.setText(item.getLocazione());
        holder.ProvLab.setText(item.getProvinciaLabel());
        holder.Prov.setText(item.getProvincia());
        holder.Dist.setText(item.getDistanza());
        return viewToUse;
    }
}