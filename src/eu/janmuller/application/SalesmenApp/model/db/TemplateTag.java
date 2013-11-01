package eu.janmuller.application.salesmenapp.model.db;

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
@GenericModel.TableName(name = "template_tags")
@GenericModel.IdType(type = GenericModel.IdTypeEnum.LONG)
final public class TemplateTag extends Tag<TemplateTag> {

    @ForeignKey(attributeClass = TemplatePage.class)
    public Id pageId;

}
