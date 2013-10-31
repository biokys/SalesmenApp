package eu.janmuller.application.salesmenapp.model;

import com.google.gson.annotations.SerializedName;
import eu.janmuller.android.dao.api.BaseDateModel;
import eu.janmuller.android.dao.api.GenericModel;

/**
 * Created with IntelliJ IDEA.
 * Coder: Jan Müller
 * Date: 18.10.13
 * Time: 11:39
 */
@GenericModel.TableName(name = "templates")
@GenericModel.IdType(type = GenericModel.IdTypeEnum.LONG)
public class Template extends BaseDateModel<Template> {

    @GenericModel.DataType(type = DataTypeEnum.FLOAT)
    @SerializedName("Version")
    public float version;

    @GenericModel.DataType(type = DataTypeEnum.TEXT)
    @SerializedName("Published")
    public String published;

    @GenericModel.DataType(type = DataTypeEnum.TEXT)
    @SerializedName("BaseURL")
    public String baseUrl;

    @GenericModel.DataType(type = DataTypeEnum.TEXT)
    @SerializedName("Name")
    public String name;

    @GenericModel.DataType(type = DataTypeEnum.INTEGER)
    @SerializedName("Order")
    public int position;

    @GenericModel.DataType(type = DataTypeEnum.TEXT)
    @SerializedName("Thumbnail")
    public String thumbnail;

    @GenericModel.DataType(type = DataTypeEnum.BOOLEAN)
    @SerializedName("Landscape")
    public boolean landscape;

    @SerializedName("Files")
    public String[] files;

    @SerializedName("Pages")
    public Page[] pages;

    @Override
    public String toString() {

        return name;
    }
}
