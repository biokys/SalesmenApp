package eu.janmuller.application.salesmenapp.activity;


import eu.janmuller.android.dao.api.LongId;
import eu.janmuller.application.salesmenapp.model.db.DocumentTag;
import org.apache.commons.lang3.StringEscapeUtils;
import roboguice.util.Ln;

import java.util.List;

/**
 * JS INTERFACE
 *
 * Trida slouzi pro komunikaci s JS
 *
 */
public class WebAppInterface {

    /**
     * Pri ukonceni editace nad nejakou strankou se z JS vola teto metoda pro kazdy
     * nalezeny element obsahujici edit class.
     *
     * Prichozi text se ulozi do db vcetne idcka elementu
     * @param result
     */
    public void saveTag(String result) {

        Ln.d("saveTag: " + result);
        String[] split = result.split("::");
        if (split.length != 3) {

            return;
        }

        String pageid = split[0];
        String id = split[1];
        String text = split[2];

        List<DocumentTag> documentTags = DocumentTag.getByQuery(DocumentTag.class,
                "documentPageId=" + Long.decode(pageid) + " and tagIdent='" + id + "'");

        DocumentTag documentTag;
        if (documentTags.size() > 0) {

            documentTag = documentTags.get(0);
            text = StringEscapeUtils.unescapeJava(text);
            documentTag.value = text;
        } else {

            documentTag = new DocumentTag();
            documentTag.tagIdent = id;
            documentTag.documentPageId = new LongId(Long.decode(pageid));
            documentTag.value = text;
        }
        documentTag.save();
    }
}