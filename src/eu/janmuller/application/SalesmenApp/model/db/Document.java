package eu.janmuller.application.salesmenapp.model.db;

import eu.janmuller.android.dao.api.GenericModel;
import eu.janmuller.android.dao.api.Id;

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
final public class Document extends Template {

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
}
