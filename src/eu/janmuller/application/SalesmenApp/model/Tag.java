package eu.janmuller.application.salesmenapp.model;

import com.google.gson.annotations.SerializedName;
import eu.janmuller.android.dao.api.BaseDateModel;
import eu.janmuller.android.dao.api.GenericModel;
import eu.janmuller.android.dao.api.Id;

/**
 * Created with IntelliJ IDEA.
 * Coder: Jan Müller
 * Date: 19.10.13
 * Time: 16:33
 */
@GenericModel.TableName(name = "tags")
@GenericModel.IdType(type = GenericModel.IdTypeEnum.LONG)
public class Tag extends BaseDateModel<Tag> {

    @GenericModel.ForeignKey(attributeClass = Page.class)
    public Id     pageId;

    @GenericModel.DataType(type = DataTypeEnum.TEXT)
    @SerializedName("TagIdent")
    public String tagIndent;

    @GenericModel.DataType(type = DataTypeEnum.TEXT)
    @SerializedName("Value")
    public String value;
}
