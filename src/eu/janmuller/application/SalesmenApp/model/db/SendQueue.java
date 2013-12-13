package eu.janmuller.application.salesmenapp.model.db;

import eu.janmuller.android.dao.api.BaseDateModel;
import eu.janmuller.android.dao.api.GenericModel;

/**
 * Created with IntelliJ IDEA.
 * Coder: Jan Müller
 * Date: 12.12.13
 * Time: 13:34
 */
@GenericModel.TableName(name = "send_queue")
@GenericModel.IdType(type = GenericModel.IdTypeEnum.LONG)
public class SendQueue extends BaseDateModel<SendQueue> {

    @GenericModel.DataType(type = DataTypeEnum.TEXT)
    public String inquiryServerId;

    @GenericModel.DataType(type = DataTypeEnum.TEXT)
    public String mail;

    @GenericModel.DataType(type = DataTypeEnum.TEXT)
    public String title;

    @GenericModel.DataType(type = DataTypeEnum.TEXT)
    public String text;

    @GenericModel.DataType(type = DataTypeEnum.TEXT)
    public String json;

    public SendQueue() {

    }

    public SendQueue(String inquiryServerId, String mail, String title, String text, String json) {

        this.inquiryServerId = inquiryServerId;
        this.mail = mail;
        this.title = title;
        this.text = text;
        this.json = json;
    }

    public static void push(Inquiry inquiry, String mail, String title, String text, String json) {

        int count = SendQueue.getCountByQuery(SendQueue.class, "inquiryServerId='" + inquiry.serverId +
                "' and mail='" + mail +
                "' and title='" + title +
                "' and text='" + text +
                "' and json='" + json + "'");

        if (count == 0) {

            SendQueue sendQueue = new SendQueue(inquiry.serverId, mail, title, text, json);
            sendQueue.save();
        }
    }
}
