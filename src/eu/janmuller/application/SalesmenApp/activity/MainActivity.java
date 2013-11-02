package eu.janmuller.application.salesmenapp.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Toast;
import eu.janmuller.application.salesmenapp.DownloadTask;
import eu.janmuller.application.salesmenapp.adapter.InquiriesAdapter;
import eu.janmuller.application.salesmenapp.R;
import eu.janmuller.application.salesmenapp.model.db.Inquiry;
import eu.janmuller.application.salesmenapp.model.db.Template;
import roboguice.inject.ContentView;
import roboguice.inject.InjectView;

import java.util.List;

@ContentView(R.layout.main)
public class MainActivity extends BaseActivity {

    @InjectView(R.id.list)
    private ListView mListView;

    @InjectView(R.id.spinner_templates)
    private Spinner mSpinnerTemplates;

    private Button mButtonTemplates;

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        if (Inquiry.getCountByQuery(Inquiry.class, "1=1") == 0) {

            new DownloadTask(this, DownloadTask.Type.INQUIRIES, new DownloadTask.ITaskCompleteCallback() {
                @Override
                public void onTaskComplete() {

                    fillInquiriesTable();
                }
            }).execute();
        } else {

            fillInquiriesTable();
        }

        if (Template.getCountByQuery(Template.class, "1=1") == 0) {

            new DownloadTask(this, DownloadTask.Type.TEMPLATES).execute();
        }
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
        View header = getLayoutInflater().inflate(R.layout.inquiry_header, null);
        mButtonTemplates = (Button) header.findViewById(R.id.button_templates);
        mButtonTemplates.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //To change body of implemented methods use File | Settings | File Templates.
            }
        });
        mListView.addHeaderView(header);
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
