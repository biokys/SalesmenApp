package eu.janmuller.application.salesmenapp.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.GridLayout;
import android.widget.Toast;
import eu.janmuller.application.salesmenapp.R;
import eu.janmuller.application.salesmenapp.model.db.Document;
import eu.janmuller.application.salesmenapp.model.db.Inquiry;
import roboguice.inject.ContentView;
import roboguice.inject.InjectView;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * Coder: Jan Müller
 * Date: 18.10.13
 * Time: 14:55
 */
@ContentView(R.layout.send_activity)
public class SendActivity extends BaseActivity {

    @InjectView(R.id.email)
    private EditText mEmailAddress;

    @InjectView(R.id.grid)
    private GridLayout mGridLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        mActionBar.setDisplayHomeAsUpEnabled(true);
        mActionBar.setHomeButtonEnabled(true);

        Intent intent = getIntent();
        Inquiry inquiry = (Inquiry) intent.getSerializableExtra(ViewActivity.INQUIRY);

        if (inquiry == null) {

            finish();
            return;
        }

        if (inquiry.mail != null) {

            mEmailAddress.setText(inquiry.mail);
        }

        for (Document document : getDocuments(inquiry)) {

            final View view = getLayoutInflater().inflate(R.layout.documentlistview, null);
            view.post(new Runnable() {
                @Override
                public void run() {

                    view.getLayoutParams().width = getResources().getDimensionPixelSize(R.dimen.sidebar_width);
                }
            });
            ViewActivityHelper.getThumbnailImage(view, document, document.thumbnail);
            mGridLayout.addView(view);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.send_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {

            case android.R.id.home:

                finish();
                break;
            case R.id.menu_send:

                sendMessage();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private List<Document> getDocuments(Inquiry inquiry) {

        return Document.getByQuery(Document.class, "show=1 and inquiryId=" + inquiry.id.getId());
    }

    private void sendMessage() {

        Toast.makeText(this, "Sending an inquiry...", Toast.LENGTH_SHORT).show();
    }
}
