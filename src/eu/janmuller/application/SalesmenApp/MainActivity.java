package eu.janmuller.application.SalesmenApp;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Toast;
import com.google.inject.Inject;
import eu.janmuller.application.SalesmenApp.model.Inquiry;
import eu.janmuller.application.SalesmenApp.model.Page;
import eu.janmuller.application.SalesmenApp.model.Tag;
import eu.janmuller.application.SalesmenApp.model.Template;
import org.joda.time.DateTime;
import roboguice.inject.ContentView;
import roboguice.inject.InjectView;
import roboguice.util.RoboAsyncTask;

import java.util.ArrayList;
import java.util.List;

@ContentView(R.layout.main)
public class MainActivity extends BaseActivity {

    @InjectView(R.id.list)
    private ListView mListView;

    @InjectView(R.id.spinner_templates)
    private Spinner mSpinnerTemplates;

    @Inject
    private DownloadService mDownloadService;

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
    }

    @Override
    protected void onStart() {

        super.onStart();

        Tag.deleteAll(Tag.class);
        Page.deleteAll(Page.class);
        Template.deleteAll(Template.class);
        mDownloadService.downloadTemplates();

        //new DownloadTask(this, mDownloadService).execute();

        /*TemplatesAdapter templatesAdapter = new TemplatesAdapter(this);

        List<Template> list = new ArrayList<Template>();
        Template template;

        template = new Template("T1");
        list.add(template);

        template = new Template("T2");
        list.add(template);

        template = new Template("T3");
        list.add(template);

        templatesAdapter.addAll(list);
        mSpinnerTemplates.setAdapter(templatesAdapter);*/

        fillInquiriesTable();
    }

    private static class DownloadTask extends RoboAsyncTask<Void> {

        private DownloadService mDownloadService;

        private DownloadTask(Context context, DownloadService downloadService) {

            super(context);
            this.mDownloadService = downloadService;
        }

        @Override
        public Void call() throws Exception {

            Template.deleteAll(Template.class);
            mDownloadService.downloadTemplates();
            return null;  //To change body of implemented methods use File | Settings | File Templates.
        }
    }


    private void fillInquiriesTable() {

        List<Inquiry> list = new ArrayList<Inquiry>();
        Inquiry inquiry;

        inquiry = new Inquiry("Seznam.cz", "Jara Novak", "2a, 2b, 3a, 3c", new DateTime().minusDays(3).toDate());
        list.add(inquiry);
        inquiry = new Inquiry("Skoda", "Pavel Vodrazka", "1a, 1b, 2a, 2c", new DateTime().minusDays(2).toDate());
        list.add(inquiry);
        inquiry = new Inquiry("Atlas", "Petra Nova", "2a", new DateTime().minusDays(1).toDate());
        list.add(inquiry);
        inquiry = new Inquiry("IBM", "Paul Allen", "3c", new DateTime().toDate());
        list.add(inquiry);

        inquiry = new Inquiry("Seznam.cz", "Jara Novak", "2a, 2b, 3a, 3c", new DateTime().minusDays(3).toDate());
        list.add(inquiry);
        inquiry = new Inquiry("Skoda", "Pavel Vodrazka", "1a, 1b, 2a, 2c", new DateTime().minusDays(2).toDate());
        list.add(inquiry);
        inquiry = new Inquiry("Atlas", "Petra Nova", "2a", new DateTime().minusDays(1).toDate());
        list.add(inquiry);
        inquiry = new Inquiry("IBM", "Paul Allen", "3c", new DateTime().toDate());
        list.add(inquiry);

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
