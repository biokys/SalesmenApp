package eu.janmuller.application.salesmenapp.model.db;

import com.google.gson.annotations.SerializedName;

import eu.janmuller.android.dao.api.GenericModel;
import eu.janmuller.android.dao.api.Id;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * Coder: Jan Müller
 * Date: 01.11.13
 * Time: 15:20
 */
@GenericModel.TableName(name = "template_pages")
@GenericModel.IdType(type = GenericModel.IdTypeEnum.LONG)
final public class TemplatePage extends Page<TemplatePage> {

    @GenericModel.ForeignKey(attributeClass = Template.class)
    public Id templateId;

    @SerializedName("Versions")
    public TemplatePage[] versions;

    public List<TemplateTag> getTemplateTagsByPage() {

        return TemplateTag.getByQuery(TemplateTag.class, "pageId=" + this.id.getId());
    }
}
