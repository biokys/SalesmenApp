package eu.janmuller.application.SalesmenApp.model;

import com.google.gson.annotations.SerializedName;

/**
 * Created with IntelliJ IDEA.
 * Coder: Jan Müller
 * Date: 19.10.13
 * Time: 17:46
 */
public class TemplatesEnvelope {

    @SerializedName("Templates")
    public Template[] templates;
}
