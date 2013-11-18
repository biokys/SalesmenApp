package eu.janmuller.application.salesmenapp.activity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Toast;
import com.google.inject.Inject;
import eu.janmuller.application.salesmenapp.R;
import eu.janmuller.application.salesmenapp.adapter.InquiriesAdapter;
import eu.janmuller.application.salesmenapp.model.db.*;
import eu.janmuller.application.salesmenapp.server.DownloadData;
import roboguice.inject.ContentView;
import roboguice.inject.InjectView;

import java.util.Date;
import java.util.List;

@ContentView(R.layout.main)
public class MainActivity extends BaseActivity {

    public static final String VENDOR_MAFRA    = "Mafra";
    public static final String VENDOR_DALLMAYR = "Dallmayr";

    public static String sActualVendor = VENDOR_MAFRA;

    @InjectView(R.id.list)
    private ListView mListView;

    @InjectView(R.id.spinner_templates)
    private Spinner mSpinnerTemplates;

    private Button mButtonTemplates;

    private InquiriesAdapter mInquiriesAdapter;

    @Inject
    private DownloadData mDownloadData;

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        //prepareListAdapter();

        if (Template.getCountByQuery(Template.class, "1=1") == 0) {

        loadData();
        } else {

            List<Inquiry> list = Inquiry.getAllObjects(Inquiry.class);
            openViewActivity(list.get(0));
        }

    }

    private void loadData() {

        mDownloadData
                .setDownloadInquiries(sActualVendor.equals(VENDOR_DALLMAYR))
                .run(new DownloadData.IDownloadDataCallback() {
                    @Override
                    public void onInquiriesDownloaded() {

                        if (sActualVendor.equals(VENDOR_DALLMAYR)) {

                            fillInquiriesTable();
                        }
                    }

                    @Override
                    public void onTemplatesDownloaded() {

                        if (sActualVendor.equals(VENDOR_MAFRA)) {

                            Inquiry inquiry;
                            List<Inquiry> list = Inquiry.getAllObjects(Inquiry.class);

                            inquiry = list.size() > 0 ? list.get(0) : new Inquiry();
                            inquiry.title = "Mafra";
                            inquiry.created = InquiriesAdapter.mSdf.format(new Date());
                            inquiry.state = Inquiry.State.NEW;
                            inquiry.save();
                            openViewActivity(inquiry);
                        }
                    }
                });
    }

    @Override
    protected void onStart() {

        super.onStart();
        //fillInquiriesTable();
    }

    private void prepareListAdapter() {

        mInquiriesAdapter = new InquiriesAdapter(this);
        mInquiriesAdapter.setCallbackListener(new InquiriesAdapter.IInquiryAdapterCallback() {
            @Override
            public void onInquirySelect(Inquiry inquiry) {

                openViewActivity(inquiry);
            }

            @Override
            public void onInquiryCloseRequest(Inquiry inquiry) {

                closeInquiry(inquiry);
            }
        });

        View header = getLayoutInflater().inflate(R.layout.inquiry_header, null);
        mButtonTemplates = (Button) header.findViewById(R.id.button_templates);
        mButtonTemplates.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });
        mListView.addHeaderView(header);
        mListView.setAdapter(mInquiriesAdapter);
    }

    private void fillInquiriesTable() {

        mInquiriesAdapter.clear();

        List<Inquiry> list = Inquiry.getInquiriesWithAttachments();
        mInquiriesAdapter.addAll(list);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.main_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    private void openViewActivity(Inquiry inquiry) {

        Intent intent = new Intent(this, ViewActivity.class);
        intent.putExtra(ViewActivity.INQUIRY, inquiry);
        startActivityForResult(intent, 100);
    }

    private void closeInquiry(Inquiry inquiry) {

        Toast.makeText(this, "Zaviram poptavku " + inquiry.contact, Toast.LENGTH_SHORT).show();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {

            case R.id.menu_open_web:

                Toast.makeText(MainActivity.this, "Tady se otevre web", Toast.LENGTH_SHORT).show();
                break;
            case R.id.menu_delete:

                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setNegativeButton(R.string.cancel, null);
                builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                        TemplateTag.deleteAll(TemplateTag.class);
                        DocumentTag.deleteAll(DocumentTag.class);
                        TemplatePage.deleteAll(TemplatePage.class);
                        DocumentPage.deleteAll(DocumentPage.class);
                        Template.deleteAll(Template.class);
                        Document.deleteAll(Document.class);
                        Inquiry.deleteAll((Inquiry.class));
                        loadData();
                    }
                });
                builder.setTitle("Varovani");
                builder.setMessage("Opravdu vse smazat?");

                builder.create().show();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {


        if (sActualVendor.equals(VENDOR_MAFRA)) {

            finish();
        }
    }
}
