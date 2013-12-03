package eu.janmuller.application.salesmenapp.activity;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Patterns;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.GridLayout;
import android.widget.Toast;
import com.google.inject.Inject;
import eu.janmuller.application.salesmenapp.R;
import eu.janmuller.application.salesmenapp.model.db.Document;
import eu.janmuller.application.salesmenapp.model.db.Inquiry;
import eu.janmuller.application.salesmenapp.server.ServerService;
import roboguice.inject.ContentView;
import roboguice.inject.InjectView;

import java.util.Calendar;
import java.util.Date;
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

    @InjectView(R.id.subject)
    private EditText mSubject;

    @InjectView(R.id.body)
    private EditText mBody;

    @InjectView(R.id.grid)
    private GridLayout mGridLayout;

    @Inject
    private ServerService mServerService;

    private List<Document> mDocuments;
    private Inquiry        mInquiry;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        mActionBar.setDisplayHomeAsUpEnabled(true);
        mActionBar.setHomeButtonEnabled(true);

        Intent intent = getIntent();
        mInquiry = (Inquiry) intent.getSerializableExtra(ViewActivity.INQUIRY);

        if (mInquiry == null) {

            finish();
            return;
        }

        if (mInquiry.mail != null) {

            mEmailAddress.setText(mInquiry.mail);
        }

        mDocuments = getDocuments(mInquiry);
        for (Document document : mDocuments) {

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

        if (!validateMessage()) {

            return;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(SendActivity.this);
        builder.setNegativeButton(R.string.cancel, null);
        builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

                Toast.makeText(SendActivity.this, "Odesílám poptávku...", Toast.LENGTH_SHORT).show();

                final Handler handler = new Handler();
                new Thread() {

                    @Override
                    public void run() {

                        if (mServerService.send(mEmailAddress.getText().toString(),
                                mSubject.getText().toString(),
                                mBody.getText().toString(),
                                mDocuments)) {

                            handler.post(new Runnable() {
                                @Override
                                public void run() {

                                    showFollowUpDialog();
                                }
                            });
                        } else {

                            handler.post(new Runnable() {
                                @Override
                                public void run() {

                                    Toast.makeText(SendActivity.this, "Při odesílání zprávy došlo k chybě. Zkuste to později", Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                    }
                }.start();
            }
        });
        builder.setTitle("Odeslání zprávy");
        builder.setMessage("Opravdu si přejete odeslat zprávu?");

        builder.create().show();
    }

    private void showFollowUpDialog() {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        View view = getLayoutInflater().inflate(R.layout.followup_dialog, null);
        builder.setView(view);
        builder.setNegativeButton(R.string.cancel, null);
        builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

                sendFollowUpRequest(new Date(), "Bla bla");
            }
        });
        builder.setTitle("Nastavení připomenutí");
        builder.create().show();
    }

    private void sendFollowUpRequest(final Date date, final String message) {

        final Handler handler = new Handler();
        new Thread() {

            @Override
            public void run() {

                if (mServerService.followUp(mInquiry, date, message)) {

                    handler.post(new Runnable() {
                        @Override
                        public void run() {

                            Toast.makeText(SendActivity.this, "Připomenutí úspěšně odesláno", Toast.LENGTH_SHORT).show();
                        }
                    });
                } else {

                    handler.post(new Runnable() {
                        @Override
                        public void run() {

                            Toast.makeText(SendActivity.this, "Odeslání připomeutí se nezdařilo. Zkuste to později", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
        }.start();

    }

    private boolean validateMessage() {

        if (!Patterns.EMAIL_ADDRESS.matcher(mEmailAddress.getText().toString()).find()) {

            Toast.makeText(SendActivity.this, "Zadejte validní email", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (mSubject.getText().toString().length() == 0) {

            Toast.makeText(SendActivity.this, "Zadejte předmět", Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }

    private static final int DATE_DIALOG_ID = 1;

    private int    year;
    private int    month;
    private int    day;
    private String currentDate;

    private void showDateDialog() {


        final Calendar c = Calendar.getInstance();
        year = c.get(Calendar.YEAR);
        month = c.get(Calendar.MONTH);
        day = c.get(Calendar.DAY_OF_MONTH);
        showDialog(DATE_DIALOG_ID);
    }


    private void updateDisplay() {

        currentDate = new StringBuilder().append(day).append(".")
                .append(month + 1).append(".").append(year).toString();

    }

    DatePickerDialog.OnDateSetListener myDateSetListener = new DatePickerDialog.OnDateSetListener() {

        @Override
        public void onDateSet(DatePicker datePicker, int i, int j, int k) {

            year = i;
            month = j;
            day = k;
            updateDisplay();
        }
    };


    @Override
    protected Dialog onCreateDialog(int id) {

        switch (id) {
            case DATE_DIALOG_ID:
                return new DatePickerDialog(this, myDateSetListener, year, month,
                        day);
        }
        return null;
    }
}
