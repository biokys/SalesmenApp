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
    public TemplateTag[] tags;

    protected Page() {
    }

    protected Page(Page page) {

        this.position = page.position;
        this.file = page.file;
        this.thumbnail = page.thumbnail;
        this.tags = page.tags;
    }

    /**
     * Smaze stranku a odkazy na ni (Tags)
     * @throws DaoConstraintException
     */
    /*@Override
    public void delete() throws DaoConstraintException {

        List<Tag> tagList = Tag.getByQuery(Tag.class, "pageId=" + id.getId());
        for (Tag tag : tagList) {

            tag.delete();
        }
        super.delete();
    }*/
}
