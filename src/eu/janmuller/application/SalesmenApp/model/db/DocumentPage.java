package eu.janmuller.application.salesmenapp.model.db;

import eu.janmuller.android.dao.api.GenericModel;
import eu.janmuller.android.dao.api.Id;
import eu.janmuller.android.dao.exceptions.DaoConstraintException;
import eu.janmuller.application.salesmenapp.IHideAble;

/**
 * Created with IntelliJ IDEA.
 * Coder: Jan Müller
 * Date: 01.11.13
 * Time: 15:20
 */
@GenericModel.TableName(name = "document_pages")
@GenericModel.IdType(type = GenericModel.IdTypeEnum.LONG)
final public class DocumentPage extends Page<DocumentPage> implements IHideAble {

    @ForeignKey(attributeClass = Document.class)
    public Id documentId;

    @GenericModel.DataType(type = DataTypeEnum.BOOLEAN)
    public boolean show;

    /**
     * Implicitni konstruktor
     */
    public DocumentPage() {

    }

    public DocumentPage(TemplatePage templatePage, Document document) {

        super(templatePage);
        this.documentId = document.id;
        this.show = true;
    }

    @Override
    public void setVisibility(boolean visible) {

        show = visible;
    }

    @Override
    public boolean isVisible() {

        return show;
    }

    @Override
    public boolean equals(Object o) {

        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        DocumentPage that = (DocumentPage) o;

        if (!id.getId().equals(that.id.getId())) return false;

        return true;
    }

    @Override
    public void delete() throws DaoConstraintException {

        DocumentTag.deleteByQuery(DocumentTag.class, "documentPageId=" + id.getId());
        super.delete();    //To change body of overridden methods use File | Settings | File Templates.
    }

    @Override
    public int hashCode() {

        return id.getId().hashCode();
    }
}
