package eu.janmuller.application.salesmenapp.model.db;

import com.google.gson.annotations.SerializedName;
import eu.janmuller.android.dao.api.BaseDateModel;
import eu.janmuller.android.dao.api.GenericModel;
import eu.janmuller.android.dao.exceptions.DaoConstraintException;

import java.io.Serializable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

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
    @SerializedName("SellerTel")
    public String telephone;

    @GenericModel.DataType(type = DataTypeEnum.TEXT)
    @SerializedName("SellerMail")
    public String sellerMail;

    @GenericModel.DataType(type = DataTypeEnum.TEXT)
    @SerializedName("SellerName")
    public String sellerName;

    @GenericModel.DataType(type = DataTypeEnum.TEXT)
    @SerializedName("SellerStreet")
    public String sellerStreet;

    @GenericModel.DataType(type = DataTypeEnum.TEXT)
    @SerializedName("SellerCity")
    public String sellerCity;

    @GenericModel.DataType(type = DataTypeEnum.TEXT)
    @SerializedName("SellerZIP")
    public String sellerZIP;

    @GenericModel.DataType(type = DataTypeEnum.TEXT)
    @SerializedName("SellerPosition")
    public String pozice;

    @GenericModel.DataType(type = DataTypeEnum.TEXT)
    @SerializedName("Mail")
    public String mail;

    @GenericModel.DataType(type = DataTypeEnum.TEXT)
    @SerializedName("MailAF")
    public String mailAf;

    @GenericModel.DataType(type = DataTypeEnum.TEXT)
    @SerializedName("Contacts")
    public String contact;

    @GenericModel.DataType(type = DataTypeEnum.BOOLEAN)
    public boolean temporary;

    @GenericModel.DataType(type = DataTypeEnum.ENUM)
    public State state;

    @SerializedName("Custom")
    public Map<String, String> custom;

    public String attachments;

    public void mergeWith(Inquiry inquiryFromServer) {

        inquiryFromServer.id = id;
        inquiryFromServer.state = state == State.NEW ? state : State.OPEN;
        inquiryFromServer.attachments = attachments;
        inquiryFromServer.save();
    }

    public static Inquiry getByServerId(String serverId) {

        List<Inquiry> list = Inquiry.getByQuery(Inquiry.class, "serverId='" + serverId + "'");
        if (list.size() > 0) {
            return list.get(0);
        }
        return null;
    }

    public List<Document> getDocumentsByInquiry() {

        return Document.getByQuery(Document.class, "inquiryId=" + this.id.getId());
    }
    /**
     * Vrati vsechny poptavky
     * Metoda take vyplni ke kazde poptavce zkraceny nazev jejich priloh
     * Poptavky jsou serazeni podle jejich stavu
     * @return
     */
    public static List<Inquiry> getInquiriesWithAttachments() {

        List<Inquiry> list = Inquiry.getByQuery(Inquiry.class, "state < " + State.COMPLETE.ordinal() + " order by state asc");
        Iterator<Inquiry> iterator = list.iterator();
        while (iterator.hasNext()) {

            Inquiry inquiry = iterator.next();
            if (inquiry.temporary) {
                inquiry.delete();
                iterator.remove();
                continue;
            }
            if (inquiry.state == State.NEW) {
                inquiry.attachments = "-";
                continue;
            }
            String attachments = "";
            List<Document> documents = Document.getByQuery(Document.class, "show=1 and inquiryId=" + inquiry.id.getId());
            int loop = 0;
            for (Document document : documents) {
                attachments += document.shortName;
                if (++loop < documents.size()) {
                    attachments += ", ";
                }
            }
            inquiry.attachments = attachments;
        }

        return list;
    }

    @Override
    public void delete() throws DaoConstraintException {

        for (Document document : Document.getByQuery(Document.class, "inquiryId=" + id.getId())) {
            document.delete();
        }
        super.delete();
    }

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
