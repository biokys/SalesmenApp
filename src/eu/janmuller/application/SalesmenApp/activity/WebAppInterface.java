package eu.janmuller.application.salesmenapp.activity;


import eu.janmuller.android.dao.api.LongId;
import eu.janmuller.application.salesmenapp.model.db.DocumentTag;

public class WebAppInterface {


    public void saveText(String text) {

        String[] split = text.split("::");
        Long id = Long.decode(split[0]);
        DocumentTag documentTag = DocumentTag.findObjectById(DocumentTag.class, new LongId(id));
        documentTag.value = split[1];
        documentTag.save();
    }
}