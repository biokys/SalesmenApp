package eu.janmuller.application.salesmenapp.adapter;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;
import eu.janmuller.application.salesmenapp.Helper;
import eu.janmuller.application.salesmenapp.R;
import eu.janmuller.application.salesmenapp.model.db.Inquiry;
import roboguice.util.Ln;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created with IntelliJ IDEA.
 * Coder: Jan Müller
 * Date: 18.10.13
 * Time: 12:04
 */
public class InquiriesAdapter extends ArrayAdapter<Inquiry> {

    private IInquiryAdapterCallback mInquiryAdapterCallback;
    public static SimpleDateFormat mSdf = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");

    public InquiriesAdapter(Context context) {

        super(context, R.layout.inquirelistview);
    }

    public void setCallbackListener(IInquiryAdapterCallback callback) {

        this.mInquiryAdapterCallback = callback;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        View view = convertView;
        if (view == null) {

            LayoutInflater vi = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = vi.inflate(R.layout.inquirelistview, null);
        }

        final Inquiry inquiry = getItem(position);

        TextView companyName = (TextView) view.findViewById(R.id.company_name);
        TextView name = (TextView) view.findViewById(R.id.name);
        TextView attachments = (TextView) view.findViewById(R.id.attachments);
        TextView date = (TextView) view.findViewById(R.id.date);
        TextView state = (TextView) view.findViewById(R.id.state);
        Button close = (Button) view.findViewById(R.id.close);
        View newItemStripe = view.findViewById(R.id.new_item_stripe);

        newItemStripe.setVisibility(inquiry.state == Inquiry.State.NEW ? View.VISIBLE : View.INVISIBLE);
        companyName.setText(inquiry.company);
        name.setText(inquiry.contact);
        attachments.setText(inquiry.attachments);
        state.setText(inquiry.state.getText());
        try {

            Date created = mSdf.parse(inquiry.created);
            date.setText(Helper.formatDate(created));
        } catch (Exception e) {

            Ln.e(e);
        }

        close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (mInquiryAdapterCallback != null) {

                    mInquiryAdapterCallback.onInquiryCloseRequest(inquiry);
                }
            }
        });

        View leftRow = view.findViewById(R.id.left_row);
        leftRow.setBackgroundResource(android.R.drawable.list_selector_background);
        /*leftRow.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {

                manageHighlightingWhileTouching(getContext(), motionEvent, view);
                return true;
            }
        });*/

        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (mInquiryAdapterCallback != null) {

                    mInquiryAdapterCallback.onInquirySelect(inquiry);
                }
            }
        });


        return view;
    }


    public static void manageHighlightingWhileTouching(Context context, MotionEvent event, View v) {

        switch (event.getAction()) {

            case MotionEvent.ACTION_DOWN:

                v.setBackgroundColor(context.getResources().getColor(R.color.list_item_highlight));
                break;
            case MotionEvent.ACTION_UP:

                v.setBackgroundColor(Color.parseColor("#00000000"));
                v.performClick();

            case MotionEvent.ACTION_CANCEL:

                v.setBackgroundColor(Color.parseColor("#00000000"));
                break;
        }
    }

    public interface IInquiryAdapterCallback {

        public void onInquirySelect(Inquiry inquiry);

        public void onInquiryCloseRequest(Inquiry inquiry);

    }
}
