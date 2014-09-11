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
import roboguice.util.Ln;

import java.util.Date;

/**
 * Created with IntelliJ IDEA.
 * Coder: Jan Müller
 * Date: 20.11.13
 * Time: 13:13
 */
public class InquiryActivityHelper {

    public static final String EMPTY_STRING = "";

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
                                    Toast.makeText(activity, activity.getString(R.string.inquiry_was_closed),
                                            Toast.LENGTH_SHORT).show();
                                }
                            });
                        } catch (final ConnectionException e) {

                            handler.post(new Runnable() {
                                @Override
                                public void run() {

                                    Ln.w(String.format(activity.getString(R.string.error_while_closing_inquiry), e.getMessage()));
                                    Toast.makeText(activity,
                                            String.format(activity.getString(R.string.error_while_closing_inquiry),
                                                    e.getMessage()), Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                    }
                }.start();
            }
        });
        builder.setTitle(activity.getString(R.string.info));
        builder.setMessage(activity.getString(R.string.do_you_want_to_close_inquiry) + inquiry.title);
        builder.create().show();
    }

    public static synchronized void resendMessages(final ServerService serverService,
                                      final IResendMessageCallback resendMessageCallback) {

        final Handler handler = new Handler();
        new Thread() {

            @Override
            public void run() {

                try {

                    final int count = serverService.sendFromSendQueue();
                    if (resendMessageCallback != null) {

                        handler.post(new Runnable() {
                            @Override
                            public void run() {

                                resendMessageCallback.onMesagesSent(count);
                            }
                        });
                    }
                } catch (ConnectionException e) {

                    Ln.e(e);
                }
            }
        }.start();
    }

    public interface IResendMessageCallback {

        public void onMesagesSent(int count);
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
        activity.startActivityForResult(intent, 100);
    }

    public static void createAndOpenTempInquiry(Activity activity) {

        Inquiry inquiry = new Inquiry();
        inquiry.temporary = true;
        inquiry.title = activity.getString(R.string.template_documents);
        inquiry.company = EMPTY_STRING;
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
