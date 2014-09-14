package eu.janmuller.application.salesmenapp.model.db;

import android.util.LongSparseArray;

import eu.janmuller.android.dao.api.GenericModel;
import eu.janmuller.android.dao.api.Id;
import eu.janmuller.android.dao.exceptions.DaoConstraintException;
import eu.janmuller.application.salesmenapp.adapter.ISidebarShowable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    public List<DocumentPage> getDocumentPagesByDocument() {

        return getDocumentPagesByDocument(false);
    }

    /**
     * Get pages from particular document. It also build parent/child object structure
     * @param onlyVisible Returns only visible items.
     * @return The list od {@link eu.janmuller.application.salesmenapp.model.db.DocumentPage} instances.
     */
    public List<DocumentPage> getDocumentPagesByDocument(boolean onlyVisible) {

        String visibleSql = onlyVisible ? " and show=1" : "";
        LongSparseArray<List<DocumentPage>> map = new LongSparseArray<List<DocumentPage>>();
        List<DocumentPage> documentPages = DocumentPage.getByQuery(DocumentPage.class, "documentId=" + this.id.getId() + visibleSql);
        List<DocumentPage> newDocumentPages = new ArrayList<DocumentPage>();
        for (DocumentPage documentPage : documentPages) {
            documentPage.parentDocument = this;
            long parentId = documentPage.parentId;
            if (parentId > -1) {
                List<DocumentPage> dpList = map.get(parentId);
                if (dpList == null) {
                    dpList = new ArrayList<DocumentPage>();
                    map.put(parentId, dpList);
                }
                dpList.add(documentPage);
            } else {
                newDocumentPages.add(documentPage);
            }
        }
        for (DocumentPage documentPage : newDocumentPages) {
            List<DocumentPage> list = map.get((Long)documentPage.id.getId());
            if (list != null) {
                documentPage.versions = list;
            }
        }
        return newDocumentPages;
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
