package eu.janmuller.application.SalesmenApp;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import eu.janmuller.application.SalesmenApp.model.Template;

/**
 * Created with IntelliJ IDEA.
 * Coder: Jan Müller
 * Date: 18.10.13
 * Time: 11:33
 */
public class TemplatesAdapter extends ArrayAdapter<Template> {

    public TemplatesAdapter(Context context) {

        super(context, R.layout.templatelistview);
    }

    @Override
    public View getDropDownView(int position, View convertView, ViewGroup parent) {

        return getView(position, convertView, parent);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        View view = convertView;
        if (view == null) {

            LayoutInflater vi = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = vi.inflate(R.layout.templatelistview, null);
        }

        Template template = getItem(position);

        TextView text = (TextView)view.findViewById(R.id.text);
        text.setText(template.toString());

        return view;

    }
}
