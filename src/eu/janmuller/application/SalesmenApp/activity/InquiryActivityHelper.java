package eu.janmuller.application.salesmenapp.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Handler;
import android.widget.ArrayAdapter;
import android.widget.Toast;
import eu.janmuller.application.salesmenapp.Helper;
import eu.janmuller.application.salesmenapp.R;
import eu.janmuller.application.salesmenapp.model.db.*;
import eu.janmuller.application.salesmenapp.server.ConnectionException;
import eu.janmuller.application.salesmenapp.server.ServerService;

import java.util.Date;

/**
 * Created with IntelliJ IDEA.
 * Coder: Jan Müller
 * Date: 20.11.13
 * Time: 13:13
 */
public class InquiryActivityHelper {

    public static void closeInquiry(final Activity activity, final Inquiry inquiry,
                                    final ServerService serverService, final ArrayAdapter listAdapter) {

        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setNegativeButton(R.string.cancel, null);
        builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

                final Handler handler = new Handler();
                new Thread() {

                    @Override
                    public void run() {

                        try {

                            serverService.closeInquiry(inquiry);

                            inquiry.state = Inquiry.State.COMPLETE;
                            inquiry.save();
                            handler.post(new Runnable() {
                                @Override
                                public void run() {

                                    listAdapter.remove(inquiry);
                                    listAdapter.notifyDataSetChanged();
                                    Toast.makeText(activity, "Poptávka byla uzavřena", Toast.LENGTH_SHORT).show();
                                }
                            });
                        } catch (final ConnectionException e) {

                            handler.post(new Runnable() {
                                @Override
                                public void run() {

                                    Toast.makeText(activity, "Během uzavírání došlo k chybě [" + e.getMessage() + "]", Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                    }
                }.start();

            }
        });
        builder.setTitle("Info");
        builder.setMessage("Chcete opravdu uzavřít poptávku " + inquiry.title);
        builder.create().show();
    }

    /**
     * Otevre prohlizeni dokumentu pro konkretni poptavku
     *
     * @param activity
     * @param inquiry
     */
    public static void openViewActivity(Activity activity, Inquiry inquiry) {

        openViewActivity(activity, inquiry, false);
    }

    /**
     * Otevre prohlizeni dokumentu
     *
     * @param activity
     * @param inquiry
     * @param tempInquiry pokud true, pak se vytvori jen docasna poptavka, ktera se po navratu zpet zase smaze
     */
    public static void openViewActivity(Activity activity, Inquiry inquiry, boolean tempInquiry) {

        Intent intent = new Intent(activity, ViewActivity.class);
        intent.putExtra(ViewActivity.INQUIRY, inquiry);
        intent.putExtra(ViewActivity.TEMP, tempInquiry);
        activity.startActivityForResult(intent, 100);
    }

    public static void createAndOpenTempInquiry(Activity activity) {

        Inquiry inquiry = new Inquiry();
        inquiry.title = "Dočasná poptávka";
        inquiry.company = "Společnost";
        inquiry.created = Helper.sSdf.format(new Date());
        inquiry.state = Inquiry.State.NEW;
        inquiry.save();
        InquiryActivityHelper.openViewActivity(activity, inquiry, true);
    }

    public static void deleteAll() {

        TemplateTag.deleteAll(TemplateTag.class);
        DocumentTag.deleteAll(DocumentTag.class);
        TemplatePage.deleteAll(TemplatePage.class);
        DocumentPage.deleteAll(DocumentPage.class);
        Template.deleteAll(Template.class);
        Document.deleteAll(Document.class);
        Inquiry.deleteAll((Inquiry.class));
    }
}
