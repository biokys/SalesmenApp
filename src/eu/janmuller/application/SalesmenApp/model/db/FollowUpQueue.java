package eu.janmuller.application.salesmenapp.model.db;

import eu.janmuller.android.dao.api.BaseDateModel;
import eu.janmuller.android.dao.api.GenericModel;

import java.util.Date;

/**
 * @author Jan Muller (muller).
 */
@GenericModel.TableName(name = "follow_up_queue")
@GenericModel.IdType(type = GenericModel.IdTypeEnum.LONG)
public class FollowUpQueue extends BaseDateModel<FollowUpQueue> {

    @GenericModel.DataType(type = DataTypeEnum.TEXT)
    public String inquiryServerId;

    @GenericModel.DataType(type = DataTypeEnum.TEXT)
    public String text;

    @GenericModel.DataType(type = DataTypeEnum.DATE)
    public Date date;

    public static void push(String inquiryServerId, String text, Date date) {

        FollowUpQueue followUpQueue = new FollowUpQueue();
        followUpQueue.inquiryServerId = inquiryServerId;
        followUpQueue.text = text;
        followUpQueue.date = date;
        followUpQueue.save();
    }
}
