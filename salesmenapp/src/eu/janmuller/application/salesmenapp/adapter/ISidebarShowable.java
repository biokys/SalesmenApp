package eu.janmuller.application.salesmenapp.adapter;

import eu.janmuller.application.salesmenapp.model.db.Document;

/**
 * Created with IntelliJ IDEA.
 * Coder: Jan Müller
 * Date: 05.12.13
 * Time: 15:22
 */
public interface ISidebarShowable {

    public String getTitle();

    public String getImagePath();

    public Document getDocument();

    public void setVisibility(boolean visible);

    public boolean isVisible();
}
