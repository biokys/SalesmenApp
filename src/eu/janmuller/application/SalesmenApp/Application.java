package eu.janmuller.application.salesmenapp;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import eu.janmuller.android.dao.api.SimpleDroidDao;
import eu.janmuller.application.salesmenapp.model.db.*;

/**
 * Created with IntelliJ IDEA.
 * Coder: Jan Müller
 * Date: 19.10.13
 * Time: 17:56
 */
public class Application extends android.app.Application {

    public static final String APP_DB  = "app_db";
    public static final int    VERSION = 38;

    private static Context sContext;

    @Override
    public void onCreate() {

        super.onCreate();
        sContext = getApplicationContext();

        SimpleDroidDao.registerModelClass(Template.class);
        SimpleDroidDao.registerModelClass(TemplatePage.class);
        SimpleDroidDao.registerModelClass(DocumentPage.class);
        SimpleDroidDao.registerModelClass(TemplateTag.class);
        SimpleDroidDao.registerModelClass(DocumentTag.class);
        SimpleDroidDao.registerModelClass(Inquiry.class);
        SimpleDroidDao.registerModelClass(Document.class);
        SimpleDroidDao.registerModelClass(SendQueue.class);
        SimpleDroidDao.registerModelClass(FollowUpQueue.class);
        SimpleDroidDao.initialize(this, APP_DB, VERSION, new SimpleDroidDao.IUpgradeHandler() {

            @Override
            public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {

                switch (oldVersion) {
                    case 37:
                        sqLiteDatabase.execSQL("ALTER TABLE inquiries ADD COLUMN pozice text;");
                    break;
                }
            }
        });
    }

    public static Context getContext() {

        return sContext;
    }

    public static String getVendorAsString() {
        return sContext.getString(R.string.vendor);
    }
}
