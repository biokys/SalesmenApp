package eu.janmuller.application.salesmenapp.activity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;
import com.google.inject.Inject;
import eu.janmuller.application.salesmenapp.Helper;
import eu.janmuller.application.salesmenapp.R;
import eu.janmuller.application.salesmenapp.adapter.InquiriesAdapter;
import eu.janmuller.application.salesmenapp.model.db.*;
import eu.janmuller.application.salesmenapp.server.DownloadData;
import eu.janmuller.application.salesmenapp.server.ServerService;
import roboguice.inject.ContentView;
import roboguice.inject.InjectView;

import java.util.Date;
import java.util.List;

/**
 * ------ UZAVRENI POPTAVKY -------
 * Bude pro vsechny vendory stejna url krom baseurl?
 * <p/>
 * <p/>
 * ------- POST-IT ----------
 * Dokuementy by měly umět pracovat s prvkem, který se ve výsledném HTML zobrazuje pouze pokud není prázdný.
 * K tomuto účelu slouží class "post-it". V režimu zobrazení se všem prázdným HTML elementům s class "post-it" přařadí navíc class "empty",
 * která bude element skrývat. V režimu editace se všem HTML elementům naopak class "empty" odebere.
 * <p/>
 * <p/>
 * -----------------
 * <p/>
 * http://api-dallmayr.sb2000.cz/service.ashx/check-registration
 * http://api-dallmayr.sb2000.cz/service.ashx/send
 * http://api-dallmayr.sb2000.cz/service.ashx/followup
 * <p/>
 * pro všechny funkce je opět povinný parametr „auth“, který představuje ID tabletu.
 * <p/>
 * Metoda „send“, která slouží pro odeslání zprávy včetně příloh, navíc očekává následující parametry:
 * mail: adresa, na kterou se má mail poslat
 * title: předmět
 * text: text mailu
 * data: JSON obsahující data dokumentů viz. https://basecamp.com/2403385/projects/3921360-app-pro-obchodniky/messages/16184899-datova-komunikace#events_message_16184899). Jeho strukturu nechám na tobě, protože nějakou už stejně musíš používat v rámci tabletu…
 * <p/>
 * Metoda „followup“ pak slouží k nastavení znovuoslovení a očekává následující parametry:
 * date: datum znovuoslovení
 * description: popis znovuoslovení
 * <p/>
 * Pokdu jde o tu registraci ID zařízení:
 * https://basecamp.com/2403385/projects/3921360-app-pro-obchodniky/messages/16185664-parovani-aplikace-na#events_message_16185664
 * <p/>
 * Metoda „check-registration“ ověřuje, zda je tablet napárován na systém. Vrací JSON s parametrem status (true/false)
 */
@ContentView(R.layout.main)
public class InquiryListActivity extends BaseActivity {

    public static final String VENDOR_MAFRA    = "mafra";
    public static final String VENDOR_DALLMAYR = "dallmayr";

    public static String sActualVendor;

    @InjectView(R.id.list)
    private ListView mListView;

    private InquiriesAdapter mInquiriesAdapter;

    @Inject
    private DownloadData mDownloadData;

    @Inject
    private ServerService mServerService;

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        if (!Helper.isPaired(this)) {


        }
        sActualVendor = getString(R.string.vendor);


        if (sActualVendor.equals(VENDOR_MAFRA)) {

            if (Template.getCountByQuery(Template.class, "1=1") == 0) {

                loadData();
            } else {

                List<Inquiry> list = Inquiry.getAllObjects(Inquiry.class);
                if (list.size() > 0) {

                    InquiryActivityHelper.openViewActivity(this, list.get(0));
                } else {

                    InquiryActivityHelper.deleteAll();
                    loadData();
                }
            }
        } else {

            prepareListAdapter();
            loadData();
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

                            List<Inquiry> list = Inquiry.getAllObjects(Inquiry.class);
                            Inquiry inquiry;

                            inquiry = list.size() > 0 ? list.get(0) : new Inquiry();
                            inquiry.title = "Mafra";
                            inquiry.created = InquiriesAdapter.mSdf.format(new Date());
                            inquiry.state = Inquiry.State.NEW;
                            inquiry.save();
                            InquiryActivityHelper.openViewActivity(InquiryListActivity.this, inquiry);
                        }
                    }
                });
    }


    @Override
    protected void onStart() {

        super.onStart();
        if (sActualVendor.equals(VENDOR_DALLMAYR)) {

            fillInquiriesTable();
        }
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
        mInquiriesAdapter.addAll(Inquiry.getInquiriesWithAttachments());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.main_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {

            case R.id.menu_open_web:

                Toast.makeText(InquiryListActivity.this, "Tady se otevre web", Toast.LENGTH_SHORT).show();
                break;
            /*case R.id.menu_delete:

                AlertDialog.Builder builder = new AlertDialog.Builder(InquiryListActivity.this);
                builder.setNegativeButton(R.string.cancel, null);
                builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                        InquiryActivityHelper.deleteAll();
                        loadData();
                    }
                });
                builder.setTitle("Varovani");
                builder.setMessage("Opravdu vse smazat?");

                builder.create().show();
                break;*/
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
