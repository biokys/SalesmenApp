package eu.janmuller.application.salesmenapp.model.db;

import eu.janmuller.android.dao.api.BaseDateModel;
import eu.janmuller.android.dao.api.GenericModel;
import eu.janmuller.android.dao.api.Id;

/**
 * Created with IntelliJ IDEA.
 * Coder: Jan Müller
 * Date: 19.10.13
 * Time: 16:33
 */
@GenericModel.TableName(name = "document_tags")
@GenericModel.IdType(type = GenericModel.IdTypeEnum.LONG)
final public class DocumentTag extends Tag<DocumentTag> {

    @ForeignKey(attributeClass = DocumentPage.class)
    public Id documentPageId;

    public DocumentTag() {
    }

    public DocumentTag(TemplateTag templateTag, DocumentPage documentPage) {

        super(templateTag);
        this.documentPageId = documentPage.id;
    }
}
