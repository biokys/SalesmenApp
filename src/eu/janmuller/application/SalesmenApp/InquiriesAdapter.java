package eu.janmuller.application.SalesmenApp;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;
import eu.janmuller.application.SalesmenApp.model.Inquiry;

import java.text.SimpleDateFormat;

/**
 * Created with IntelliJ IDEA.
 * Coder: Jan Müller
 * Date: 18.10.13
 * Time: 12:04
 */
public class InquiriesAdapter extends ArrayAdapter<Inquiry> {

    private static final SimpleDateFormat sSdf = new SimpleDateFormat("dd.MM.yyyy");
    private IInquiryAdapterCallback mInquiryAdapterCallback;

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

        TextView companyName = (TextView)view.findViewById(R.id.company_name);
        TextView name = (TextView)view.findViewById(R.id.name);
        TextView attachments = (TextView)view.findViewById(R.id.attachments);
        TextView date = (TextView)view.findViewById(R.id.date);
        TextView state = (TextView)view.findViewById(R.id.state);
        Button close = (Button)view.findViewById(R.id.close);

        companyName.setText(inquiry.company);
        name.setText(inquiry.contact);
        attachments.setText(inquiry.attachments);
        state.setText(inquiry.state);
        date.setText(sSdf.format(inquiry.date));


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
