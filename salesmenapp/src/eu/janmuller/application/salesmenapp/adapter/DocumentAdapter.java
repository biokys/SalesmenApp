package eu.janmuller.application.salesmenapp.adapter;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import eu.janmuller.application.salesmenapp.R;
import eu.janmuller.application.salesmenapp.activity.ViewActivityHelper;

import java.util.ArrayList;

/**
 * Created with IntelliJ IDEA.
 * Coder: Jan Müller
 * Date: 18.10.13
 * Time: 15:19
 */
public class DocumentAdapter extends ArrayAdapter<ISidebarShowable> {

    private boolean mEditMode;
    private int mSelectedItemIndex;

    public DocumentAdapter(Context context) {

        super(context, R.layout.documentlistview, new ArrayList<ISidebarShowable>());
    }

    public void setEditMode(boolean editMode) {

        mEditMode = editMode;
    }

    public void setSelectedItemIndex(int selectedItemIndex) {
        mSelectedItemIndex = selectedItemIndex;
        //notifyDataSetChanged();
    }

    public void setAllObjectsVisibility(boolean show) {

        for (int i = 0; i < getCount(); i++) {

            ISidebarShowable sidebarShowable = getItem(i);
            if (show) {
                ViewActivityHelper.showDocument(sidebarShowable, null);
            } else {
                ViewActivityHelper.hideDocument(sidebarShowable, null);
            }
        }
        notifyDataSetChanged();
    }

    @Override
    public int getItemViewType(int position) {
        return super.getItemViewType(position);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        View view = convertView;
        if (view == null) {

            LayoutInflater vi = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = vi.inflate(R.layout.documentlistview, null);
        }

        ISidebarShowable document = getItem(position);
        TextView textView = (TextView) view.findViewById(R.id.text);
        textView.setText(document.getTitle());

        //view.findViewById(R.id.selection).setVisibility(View.VISIBLE);
        /*view.findViewById(R.id.selectection).setVisibility(position == mSelectedItemIndex
                ? View.VISIBLE
                : View.INVISIBLE);*/

        ImageView imageView = ViewActivityHelper.getThumbnailImage(view, document.getDocument(), document.getImagePath());
        ViewActivityHelper.manageVisibility(mEditMode, view, imageView, document, new ViewActivityHelper.IVisibilityChangeCallback() {
            @Override
            public void onVisibilityChanged() {

                notifyDataSetChanged();
            }
        });

        return view;
    }
}
