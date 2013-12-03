package eu.janmuller.application.salesmenapp.model.db;

import com.google.gson.annotations.SerializedName;
import eu.janmuller.android.dao.api.BaseDateModel;
import eu.janmuller.android.dao.api.GenericModel;

/**
 * Created with IntelliJ IDEA.
 * Coder: Jan Müller
 * Date: 19.10.13
 * Time: 16:33
 */
abstract public class Tag<T extends Tag> extends BaseDateModel<T> {

    @GenericModel.DataType(type = DataTypeEnum.TEXT)
    @SerializedName("TagIdent")
    public String tagIdent;

    @GenericModel.DataType(type = DataTypeEnum.TEXT)
    @SerializedName("Value")
    public String value;

    protected Tag() {

    }

    protected Tag(Tag tag) {

        this.tagIdent = tag.tagIdent;
        this.value = tag.value;
    }
}
