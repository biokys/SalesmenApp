package eu.janmuller.application.salesmenapp.activity;

import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import com.google.inject.Inject;
import eu.janmuller.application.salesmenapp.Config;
import eu.janmuller.application.salesmenapp.Helper;
import eu.janmuller.application.salesmenapp.NewInquiriesService;
import eu.janmuller.application.salesmenapp.R;
import eu.janmuller.application.salesmenapp.adapter.InquiriesAdapter;
import eu.janmuller.application.salesmenapp.model.db.Inquiry;
import eu.janmuller.application.salesmenapp.server.DownloadData;
import eu.janmuller.application.salesmenapp.server.ServerService;
import roboguice.inject.ContentView;
import roboguice.inject.InjectView;

import java.util.List;

/**
 * Aktivita zobrazujici seznam poptavek
 */
@ContentView(R.layout.main)
public class InquiryListActivity extends BaseActivity {

    private InquiriesAdapter mInquiriesAdapter;

    @InjectView(R.id.list)
    private ListView mListView;

    @InjectView(R.id.no_inquiries)
    private TextView mNoItems;

    @InjectView(R.id.device_id)
    private TextView mDeviceId;

    @Inject
    private ServerService mServerService;

    @Inject
    private DownloadData mDownloadData;

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        if (getResources().getBoolean(R.bool.has_inquiries)) {

            NewInquiriesService.scheduleInquiryDownloadService(this);
            prepareListAdapter();
        }

        mDeviceId.setText(Helper.getUniqueId(this));
    }

    @Override
    protected void onStart() {

        super.onStart();

        if (getResources().getBoolean(R.bool.has_inquiries)) {

            fillInquiriesTable();
        }
        resendMessage();
    }

    @Override
    protected void onResume() {

        super.onResume();
        IntentFilter filter = new IntentFilter();
        filter.addAction("android.net.conn.CONNECTIVITY_CHANGE");
        filter.addAction("android.net.wifi.WIFI_STATE_CHANGED");
        registerReceiver(mConnectivityReceiver, filter);
    }

    public void onPause() {

        super.onPause();
        unregisterReceiver(mConnectivityReceiver);
    }

    private void prepareListAdapter() {

        mInquiriesAdapter = new InquiriesAdapter(this);
        mInquiriesAdapter.setCallbackListener(new InquiriesAdapter.IInquiryAdapterCallback() {

            @Override
            public void onInquirySelect(Inquiry inquiry) {

                InquiryActivityHelper.openViewActivity(InquiryListActivity.this, inquiry);
            }

            @Override
            public void onInquiryCloseRequest(Inquiry inquiry) {

                InquiryActivityHelper.closeInquiry(InquiryListActivity.this, inquiry, mServerService, mInquiriesAdapter);
            }
        });

        View header = getLayoutInflater().inflate(R.layout.inquiry_header, null);
        View footer = getLayoutInflater().inflate(R.layout.inquiry_footer, null);
        Button buttonTemplates = (Button) header.findViewById(R.id.button_templates);
        buttonTemplates.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {

                InquiryActivityHelper.createAndOpenTempInquiry(InquiryListActivity.this);
            }
        });
        mListView.addHeaderView(header);
        mListView.addFooterView(footer);
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

                Inquiry inquiry = mInquiriesAdapter.getItem(i - 1);
                InquiryActivityHelper.openViewActivity(InquiryListActivity.this, inquiry, false);
            }
        });
        mListView.setAdapter(mInquiriesAdapter);
    }

    private void fillInquiriesTable() {

        mInquiriesAdapter.clear();
        mInquiriesAdapter.fillSendQueueMap();
        List<Inquiry> inquiries = Inquiry.getInquiriesWithAttachments();
        mInquiriesAdapter.addAll(inquiries);
        mNoItems.setVisibility(inquiries.size() > 0 ? View.GONE : View.VISIBLE);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.main_menu, menu);
        MenuItem menuItem = menu.findItem(R.id.menu_open_web);
        menuItem.setVisible(getResources().getBoolean(R.bool.show_web_icon));
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {

            case R.id.menu_open_web:

                openWeb();
                break;
            case R.id.menu_refresh:

                refreshData();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void refreshData() {

        final ProgressDialog progressDialog = ProgressDialog.show(this, "Aktualizace", "Stahuji šablony");
        mDownloadData.run(new DownloadData.IDownloadDataCallback() {

            @Override
            public void onInquiriesDownloaded() {

            }

            @Override
            public void onTemplatesDownloadPostponed() {

            }

            @Override
            public void onDownloadTypeChanged(String action) {

            }

            @Override
            public void onProgressUpdate(int total, int progress, String message) {

                progressDialog.setProgress(progress);
                progressDialog.setMax(total);
                progressDialog.setMessage(message);
            }

            @Override
            public void onTemplatesDownloaded() {

                progressDialog.dismiss();
                fillInquiriesTable();
                /*InquiryActivityHelper.resendMessages(mServerService, new InquiryActivityHelper.IResendMessageCallback() {
                    @Override
                    public void onMesagesSent(int count) {

                        Toast.makeText(InquiryListActivity.this, "Počet odeslaných zpráv: " + count, Toast.LENGTH_SHORT).show();
                        fillInquiriesTable();
                    }
                });*/
            }
        }, true);
    }

    private void openWeb() {

        String url = Helper.getWebUrl(this);
        if (url != null) {

            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            startActivity(browserIntent);
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (Config.sActualVendor.equals(Config.VENDOR_MAFRA)) {

            finish();
        }
    }

    private void resendMessage() {

        InquiryActivityHelper.resendMessages(mServerService, new InquiryActivityHelper.IResendMessageCallback() {

            @Override
            public void onMesagesSent(int count) {

                fillInquiriesTable();
                mInquiriesAdapter.notifyDataSetChanged();
            }
        });
    }

    private BroadcastReceiver mConnectivityReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {

            final ConnectivityManager connMgr = (ConnectivityManager) context
                    .getSystemService(Context.CONNECTIVITY_SERVICE);

            final android.net.NetworkInfo wifi = connMgr
                    .getNetworkInfo(ConnectivityManager.TYPE_WIFI);

            final android.net.NetworkInfo mobile = connMgr
                    .getNetworkInfo(ConnectivityManager.TYPE_MOBILE);

            if (wifi.isAvailable() || mobile.isAvailable()) {

                resendMessage();
            }
        }

    };

}
