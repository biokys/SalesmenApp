package eu.janmuller.application.salesmenapp.model.db;

import eu.janmuller.android.dao.api.GenericModel;
import eu.janmuller.android.dao.api.Id;
import eu.janmuller.android.dao.exceptions.DaoConstraintException;
import eu.janmuller.application.salesmenapp.adapter.ISidebarShowable;

/**
 * Konkretni dokument, ktery vznika vytvorenim (kopie) sablony
 * Tento dokument se od sablony zpravidla lisi jinym poctem stran a ruznymi tagy
 * Dokument si drzi referenci na Inquiry
 *
 * Created with IntelliJ IDEA.
 * Coder: Jan Müller
 * Date: 01.11.13
 * Time: 11:56
 */
@GenericModel.TableName(name = "documents")
@GenericModel.IdType(type = GenericModel.IdTypeEnum.LONG)
final public class Document extends Template implements ISidebarShowable {

    @GenericModel.ForeignKey(attributeClass = Inquiry.class)
    public Id inquiryId;

    @GenericModel.DataType(type = DataTypeEnum.BOOLEAN)
    public boolean show;

    /**
     * Implicitni konstruktor kvuli SDD
     */
    public Document() {
    }

    public Document(Template template, Id inquiryId) {

        super(template);
        this.inquiryId = inquiryId;
        this.show = true;
    }

    @Override
    public void setVisibility(boolean visible) {

        show = visible;
    }

    @Override
    public void delete() throws DaoConstraintException {

        for (DocumentPage documentPage : DocumentPage.getByQuery(DocumentPage.class, "documentId=" + id.getId())) {

            documentPage.delete();
        }

        super.delete();
    }

    @Override
    public boolean isVisible() {

        return show;
    }

    @Override
    public String getTitle() {

        return name;
    }

    @Override
    public Document getDocument() {

        return this;
    }

    @Override
    public String getImagePath() {

        return thumbnail;
    }
}
