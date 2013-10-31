package eu.janmuller.application.SalesmenApp;

import eu.janmuller.android.dao.api.SimpleDroidDao;
import eu.janmuller.application.SalesmenApp.model.Page;
import eu.janmuller.application.SalesmenApp.model.Tag;
import eu.janmuller.application.SalesmenApp.model.Template;

/**
 * Created with IntelliJ IDEA.
 * Coder: Jan Müller
 * Date: 19.10.13
 * Time: 17:56
 */
public class Application extends android.app.Application {

    public static final String APP_DB  = "app_db";
    public static final int    VERSION = 6;

    @Override
    public void onCreate() {

        super.onCreate();

        SimpleDroidDao.registerModelClass(Template.class);
        SimpleDroidDao.registerModelClass(Page.class);
        SimpleDroidDao.registerModelClass(Tag.class);
        SimpleDroidDao.initialize(this, APP_DB, VERSION, null);
    }
}
