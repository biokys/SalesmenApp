package eu.janmuller.application.SalesmenApp.model;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.Date;

/**
 * Created with IntelliJ IDEA.
 * Coder: Jan Müller
 * Date: 18.10.13
 * Time: 12:03
 */
public class Inquiry implements Serializable {

    @SerializedName("ID")
    public String serverId;

    @SerializedName("Created")
    public String created;

    @SerializedName("Title")
    public String title;

    @SerializedName("Description")
    public String description;

    @SerializedName("Company")
    public String company;

    @SerializedName("Street")
    public String street;

    @SerializedName("City")
    public String city;

    @SerializedName("ZIP")
    public String zip;

    @SerializedName("RegNo")
    public String regNo;

    @SerializedName("Tel")
    public String telephone;

    @SerializedName("Mail")
    public String mail;

    @SerializedName("Contact")
    public String contact;


    public String attachments;
    public Date   date;
    public String state;

    public Inquiry(String company, String contact, String attachments, Date date) {

        this.company = company;
        this.contact = contact;
        this.attachments = attachments;
        this.date = date;
        this.state = "Tohle je nejakej posahanej stav...";
    }

    @Override
    public String toString() {

        return "Inquiry{" +
                "company='" + company + '\'' +
                ", contact='" + contact + '\'' +
                ", attachments='" + attachments + '\'' +
                ", date=" + date +
                ", state='" + state + '\'' +
                "} " + super.toString();
    }
}
