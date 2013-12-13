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
import eu.janmuller.application.salesmenapp.model.db.SendQueue;
import roboguice.util.Ln;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * Coder: Jan Müller
 * Date: 18.10.13
 * Time: 12:04
 */
public class InquiriesAdapter extends ArrayAdapter<Inquiry> {

    private IInquiryAdapterCallback mInquiryAdapterCallback;

    private Map<String, SendQueue> mSendQueueMap;

    public InquiriesAdapter(Context context) {

        super(context, R.layout.inquirelistview);
        mSendQueueMap = new HashMap<String, SendQueue>();
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
        TextView title = (TextView) view.findViewById(R.id.title);
        TextView attachments = (TextView) view.findViewById(R.id.attachments);
        TextView date = (TextView) view.findViewById(R.id.date);
        TextView state = (TextView) view.findViewById(R.id.state);
        Button close = (Button) view.findViewById(R.id.close);
        View newItemStripe = view.findViewById(R.id.new_item_stripe);

        newItemStripe.setVisibility(inquiry.state == Inquiry.State.NEW ? View.VISIBLE : View.INVISIBLE);
        companyName.setText(inquiry.company);
        title.setText(inquiry.title);
        attachments.setText(inquiry.attachments);

        try {

            Date created = Helper.sSdf.parse(inquiry.created);
            date.setText(Helper.formatDate(created));
        } catch (Exception e) {

            Ln.e(e);
        }

        SendQueue sendQueue = mSendQueueMap.get(inquiry.serverId);
        if (sendQueue != null) {

            state.setTextColor(Color.parseColor("#aa0000"));
            state.setText("Neodeslaná zpráva");
        } else {

            state.setTextColor(Color.parseColor("#000000"));
            state.setText(inquiry.state.getText());
        }

        close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (mInquiryAdapterCallback != null) {

                    mInquiryAdapterCallback.onInquiryCloseRequest(inquiry);
                }
            }
        });
        return view;
    }

    public void fillSendQueueMap() {

        mSendQueueMap.clear();
        for (SendQueue sendQueue : SendQueue.getAllObjects(SendQueue.class)) {

            mSendQueueMap.put(sendQueue.inquiryServerId, sendQueue);
        }
    }

    public interface IInquiryAdapterCallback {

        public void onInquirySelect(Inquiry inquiry);

        public void onInquiryCloseRequest(Inquiry inquiry);

    }
}
