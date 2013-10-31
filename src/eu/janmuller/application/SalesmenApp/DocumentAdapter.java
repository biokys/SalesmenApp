package eu.janmuller.application.salesmenapp;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

/**
 * Created with IntelliJ IDEA.
 * Coder: Jan Müller
 * Date: 18.10.13
 * Time: 15:19
 */
public class DocumentAdapter extends ArrayAdapter<String> {

    public DocumentAdapter(Context context) {

        super(context, R.layout.documentlistview);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        View view = convertView;
        if (view == null) {

            LayoutInflater vi = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = vi.inflate(R.layout.documentlistview, null);
        }

        String text = getItem(position);
        TextView textView = (TextView) view.findViewById(R.id.text);

        textView.setText(text);
        return view;
    }

    public void groupIt() {


    }

    public void unGroupIt() {


    }
}
