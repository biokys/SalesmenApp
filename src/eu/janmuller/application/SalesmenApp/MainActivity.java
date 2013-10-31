package eu.janmuller.application.salesmenapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Toast;
import eu.janmuller.application.salesmenapp.model.Inquiry;
import roboguice.inject.ContentView;
import roboguice.inject.InjectView;

import java.util.List;

@ContentView(R.layout.main)
public class MainActivity extends BaseActivity {

    @InjectView(R.id.list)
    private ListView mListView;

    @InjectView(R.id.spinner_templates)
    private Spinner mSpinnerTemplates;


    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        new DownloadTask(this, DownloadTask.Type.INQUIRIES, new DownloadTask.ITaskCompleteCallback() {
            @Override
            public void onTaskComplete() {

                fillInquiriesTable();
            }
        }).execute();
        new DownloadTask(this, DownloadTask.Type.TEMPLATES).execute();
    }

    private void fillInquiriesTable() {

        List<Inquiry> list = Inquiry.getAllObjects(Inquiry.class);
        InquiriesAdapter inquiriesAdapter = new InquiriesAdapter(this);
        inquiriesAdapter.setCallbackListener(new InquiriesAdapter.IInquiryAdapterCallback() {
            @Override
            public void onInquirySelect(Inquiry inquiry) {

                openViewActivity(inquiry);
            }

            @Override
            public void onInquiryCloseRequest(Inquiry inquiry) {

                closeInquiry(inquiry);
            }
        });
        inquiriesAdapter.addAll(list);
        mListView.setAdapter(inquiriesAdapter);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.main_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    private void openViewActivity(Inquiry inquiry) {

        Intent intent = new Intent(this, ViewActivity.class);
        intent.putExtra(ViewActivity.INQUIRY, inquiry);
        startActivity(intent);
    }

    private void closeInquiry(Inquiry inquiry) {

        Toast.makeText(this, "Zaviram poptavku " + inquiry.contact, Toast.LENGTH_SHORT).show();
    }

}
