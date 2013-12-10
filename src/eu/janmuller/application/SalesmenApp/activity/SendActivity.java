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
import android.widget.*;
import com.google.inject.Inject;
import eu.janmuller.application.salesmenapp.Helper;
import eu.janmuller.application.salesmenapp.R;
import eu.janmuller.application.salesmenapp.adapter.DocumentAdapter;
import eu.janmuller.application.salesmenapp.model.db.Document;
import eu.janmuller.application.salesmenapp.model.db.Inquiry;
import eu.janmuller.application.salesmenapp.server.ConnectionException;
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
    private GridView mGridLayout;

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

        mDocuments = mInquiry.getDocumentsByInquiry();

        DocumentAdapter documentAdapter = new DocumentAdapter(this);
        documentAdapter.setEditMode(true);
        mGridLayout.setAdapter(documentAdapter);
        documentAdapter.addAll(mDocuments);
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



    private void sendMessage() {

        if (!validateMessage()) {

            return;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(SendActivity.this);
        builder.setNegativeButton(R.string.cancel, null);
        builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

                final Handler handler = new Handler();
                new Thread() {

                    @Override
                    public void run() {

                        try {

                            // odeslu zpravu
                            mServerService.send(mInquiry, mEmailAddress.getText().toString(),
                                    mSubject.getText().toString(),
                                    mBody.getText().toString(),
                                    mDocuments);

                            // pokud nedoslo k chybe, zobrazim followup dialog
                            handler.post(new Runnable() {
                                @Override
                                public void run() {

                                    showFollowUpDialog();
                                }
                            });
                        } catch (final ConnectionException e) {

                            // v pripade chyby zobrazim chybovou hlasku
                            handler.post(new Runnable() {
                                @Override
                                public void run() {

                                    Toast.makeText(SendActivity.this, "Při odesílání zprávy došlo k chybě [" + e.getMessage() + "]", Toast.LENGTH_SHORT).show();
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

    DatePicker mDatePicker;
    EditText   mEditText;

    /**
     * Zobrazeni followup dialogu
     */
    private void showFollowUpDialog() {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        View view = getLayoutInflater().inflate(R.layout.followup_dialog, null);
        builder.setView(view);
        builder.setNegativeButton(R.string.cancel, null);
        builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

                // nactu datum z datepickeru
                Date date = new Date(mDatePicker.getCalendarView().getDate());

                // zobrazim followup dialog
                sendFollowUpRequest(date, mEditText.getText().toString());
            }
        });
        builder.setTitle("Nastavení připomenutí");
        AlertDialog alertDialog = builder.create();

        mDatePicker = (DatePicker) view.findViewById(R.id.datePicker);
        mEditText = (EditText) view.findViewById(R.id.text);
        alertDialog.show();
    }

    /**
     * Poslani follow up requestu
     */
    private void sendFollowUpRequest(final Date date, final String message) {

        final Handler handler = new Handler();
        new Thread() {

            @Override
            public void run() {

                String strDate = Helper.sSdf.format(date);
                try {

                    // odeslu followup na server
                    mServerService.followUp(mInquiry, strDate, message);

                    // zobrazim hlasku o uspechu
                    handler.post(new Runnable() {
                        @Override
                        public void run() {

                            Toast.makeText(SendActivity.this, "Zpráva byla odeslána", Toast.LENGTH_SHORT).show();
                            finish();
                        }
                    });
                } catch (final ConnectionException e) {

                    // v pripade chyby zobrazim chybovou hlasku
                    handler.post(new Runnable() {
                        @Override
                        public void run() {

                            Toast.makeText(SendActivity.this, "Odeslání připomeutí se nezdařilo [" + e.getMessage() + "]", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
        }.start();

    }

    /**
     * Validace vstupniho formulare
     * Validujeme email a predmet. V pripade problemu, zobrazime toast + requestujeme focus na konkretni edittext
     * @return true = vsechno OK
     */
    private boolean validateMessage() {

        if (!Patterns.EMAIL_ADDRESS.matcher(mEmailAddress.getText().toString()).find()) {

            Toast.makeText(SendActivity.this, "Zadejte validní email", Toast.LENGTH_SHORT).show();
            mEmailAddress.requestFocus();
            return false;
        }

        if (mSubject.getText().toString().length() == 0) {

            Toast.makeText(SendActivity.this, "Zadejte předmět", Toast.LENGTH_SHORT).show();
            mSubject.requestFocus();
            return false;
        }

        return true;
    }
}
