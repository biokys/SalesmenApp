package eu.janmuller.application.salesmenapp.model.db;

import eu.janmuller.android.dao.api.BaseDateModel;
import eu.janmuller.android.dao.api.BaseModel;
import eu.janmuller.android.dao.api.GenericModel;
import eu.janmuller.android.dao.api.Id;

/**
 * Created by muller on 11/10/14.
 */
@GenericModel.TableName(name = "custom_params")
@GenericModel.IdType(type = GenericModel.IdTypeEnum.LONG)
public class CustomParam extends BaseDateModel<CustomParam> {

    @GenericModel.ForeignKey(attributeClass = Inquiry.class)
    public Id inquiryId;

    @GenericModel.DataType(type = DataTypeEnum.TEXT)
    public String key;

    @GenericModel.DataType(type = DataTypeEnum.TEXT)
    public String value;
}
