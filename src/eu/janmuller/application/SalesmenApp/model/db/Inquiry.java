package eu.janmuller.application.salesmenapp.model.db;

import com.google.gson.annotations.SerializedName;
import eu.janmuller.android.dao.api.BaseDateModel;
import eu.janmuller.android.dao.api.GenericModel;

import java.io.Serializable;

/**
 * Created with IntelliJ IDEA.
 * Coder: Jan Müller
 * Date: 18.10.13
 * Time: 12:03
 */
@GenericModel.TableName(name = "inquiries")
@GenericModel.IdType(type = GenericModel.IdTypeEnum.LONG)
final public class Inquiry extends BaseDateModel<Inquiry> {

    public enum State {

        NEW("Nová"),
        OPEN("Otevřená"),
        COMPLETE("Ukončená");

        private String mText;

        private State(String text) {

            mText = text;
        }

        public String getText() {

            return mText;
        }
    }

    @GenericModel.DataType(type = DataTypeEnum.TEXT)
    @SerializedName("ID")
    public String serverId;

    @GenericModel.DataType(type = DataTypeEnum.TEXT)
    @SerializedName("Created")
    public String created;

    @GenericModel.DataType(type = DataTypeEnum.TEXT)
    @SerializedName("Title")
    public String title;

    @GenericModel.DataType(type = DataTypeEnum.TEXT)
    @SerializedName("Description")
    public String description;

    @GenericModel.DataType(type = DataTypeEnum.TEXT)
    @SerializedName("Company")
    public String company;

    @GenericModel.DataType(type = DataTypeEnum.TEXT)
    @SerializedName("Street")
    public String street;

    @GenericModel.DataType(type = DataTypeEnum.TEXT)
    @SerializedName("City")
    public String city;

    @GenericModel.DataType(type = DataTypeEnum.TEXT)
    @SerializedName("ZIP")
    public String zip;

    @GenericModel.DataType(type = DataTypeEnum.TEXT)
    @SerializedName("RegNo")
    public String regNo;

    @GenericModel.DataType(type = DataTypeEnum.TEXT)
    @SerializedName("Tel")
    public String telephone;

    @GenericModel.DataType(type = DataTypeEnum.TEXT)
    @SerializedName("Mail")
    public String mail;

    @GenericModel.DataType(type = DataTypeEnum.TEXT)
    @SerializedName("Contact")
    public String contact;

    @GenericModel.DataType(type = DataTypeEnum.ENUM)
    public State state;

    public String attachments;

    @Override
    public String toString() {

        return "Inquiry{" +
                "serverId='" + serverId + '\'' +
                ", created='" + created + '\'' +
                ", title='" + title + '\'' +
                ", description='" + description + '\'' +
                ", company='" + company + '\'' +
                ", street='" + street + '\'' +
                ", city='" + city + '\'' +
                ", zip='" + zip + '\'' +
                ", regNo='" + regNo + '\'' +
                ", telephone='" + telephone + '\'' +
                ", mail='" + mail + '\'' +
                ", contact='" + contact + '\'' +
                ", state=" + state +
                ", attachments='" + attachments + '\'' +
                "} " + super.toString();
    }
}
