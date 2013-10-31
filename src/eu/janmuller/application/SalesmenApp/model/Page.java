package eu.janmuller.application.SalesmenApp.model;

import com.google.gson.annotations.SerializedName;
import eu.janmuller.android.dao.api.BaseDateModel;
import eu.janmuller.android.dao.api.GenericModel;
import eu.janmuller.android.dao.api.Id;

/**
 * Created with IntelliJ IDEA.
 * Coder: Jan Müller
 * Date: 19.10.13
 * Time: 16:32
 */
@GenericModel.TableName(name = "pages")
@GenericModel.IdType(type = GenericModel.IdTypeEnum.LONG)
public class Page extends BaseDateModel<Page> {

    @GenericModel.ForeignKey(attributeClass = Template.class)
    public Id templateId;

    @GenericModel.DataType(type = DataTypeEnum.INTEGER)
    @SerializedName("Order")
    public int position;

    @GenericModel.DataType(type = DataTypeEnum.TEXT)
    @SerializedName("File")
    public String file;

    @GenericModel.DataType(type = DataTypeEnum.TEXT)
    @SerializedName("Thumbnail")
    public String thumbnail;

    @SerializedName("Tags")
    public Tag[] tags;
}
