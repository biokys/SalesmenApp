package eu.janmuller.application.salesmenapp.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.EditText;
import eu.janmuller.application.salesmenapp.R;
import eu.janmuller.application.salesmenapp.model.db.Inquiry;
import roboguice.inject.ContentView;
import roboguice.inject.InjectView;

/**
 * Created with IntelliJ IDEA.
 * Coder: Jan Müller
 * Date: 18.11.13
 * Time: 16:23
 */
@ContentView(R.layout.inquiry_info_activity)
public class InquiryInfoActivity extends BaseActivity {

    @InjectView(R.id.name)
    private EditText mName;

    @InjectView(R.id.agent)
    private EditText mAgent;

    @InjectView(R.id.date_of_contact)
    private EditText mDateOfContact;

    @InjectView(R.id.state)
    private EditText mState;

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

        showInfo(inquiry);
    }

    private void showInfo(Inquiry inquiry) {

        mName.setText(inquiry.title);
        mState.setText(inquiry.state.getText());
        mAgent.setText(inquiry.contact);
        mDateOfContact.setText(inquiry.created);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {

            case android.R.id.home:

                finish();
                break;
        }
        return super.onOptionsItemSelected(item);
    }
}
