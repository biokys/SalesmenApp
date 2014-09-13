package eu.janmuller.application.salesmenapp.model.db;

import com.google.gson.annotations.SerializedName;
import eu.janmuller.android.dao.api.BaseDateModel;
import eu.janmuller.android.dao.api.GenericModel;
import eu.janmuller.android.dao.api.Id;
import eu.janmuller.android.dao.exceptions.DaoConstraintException;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * Coder: Jan Müller
 * Date: 19.10.13
 * Time: 16:32
 */

abstract public class Page<T extends Page> extends BaseDateModel<T> {

    public static final String PAGE_TYPE_SLIDE = "slide";
    public static final String PAGE_TYPE_GROUP = "group";

    @GenericModel.DataType(type = DataTypeEnum.TEXT)
    @SerializedName("Type")
    public String type;

    @GenericModel.DataType(type = DataTypeEnum.TEXT)
    @SerializedName("Name")
    public String name;

    @GenericModel.DataType(type = DataTypeEnum.INTEGER)
    @SerializedName("Order")
    public int position;

    @GenericModel.DataType(type = DataTypeEnum.TEXT)
    @SerializedName("File")
    public String file;

    @GenericModel.DataType(type = DataTypeEnum.TEXT)
    @SerializedName("Thumbnail")
    public String thumbnail;

    @GenericModel.DataType(type = DataTypeEnum.LONG)
    public long parentId;

    @SerializedName("Tags")
    public TemplateTag[] tags;

    public Page() {
    }

    public Page(Page page) {

        this.position = page.position;
        this.file = page.file;
        this.thumbnail = page.thumbnail;
        this.tags = page.tags;
        this.name = page.name;
        this.parentId = -1;
        this.type = page.type;
    }
}
