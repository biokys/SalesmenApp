package eu.janmuller.application.salesmenapp.model.db;

import com.google.gson.annotations.SerializedName;
import eu.janmuller.android.dao.api.BaseDateModel;
import eu.janmuller.android.dao.api.GenericModel;
import roboguice.util.Ln;

import java.util.ArrayList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * Coder: Jan Müller
 * Date: 18.10.13
 * Time: 11:39
 */
@GenericModel.TableName(name = "templates")
@GenericModel.IdType(type = GenericModel.IdTypeEnum.LONG)
public class Template extends BaseDateModel<Template> {

    public enum Type {

        INFO("Info"),
        DOCUMENT("Doc");

        public String label;

        private Type(String label) {

            this.label = label;
        }

        public static Type getByLabel(String label) {

            for (Type type : Type.values()) {

                if (type.label.equals(label)) {

                    return type;
                }
            }
            return null;
        }
    }

    @GenericModel.DataType(type = DataTypeEnum.TEXT)
    @SerializedName("Ident")
    public String ident;

    /**
     * Version number consists of major number and minor number, eg. 2.4
     */
    @GenericModel.DataType(type = DataTypeEnum.TEXT)
    @SerializedName("Ver")
    public String version;

    @GenericModel.DataType(type = DataTypeEnum.TEXT)
    @SerializedName("Published")
    public String published;

    @GenericModel.DataType(type = DataTypeEnum.TEXT)
    @SerializedName("Type")
    public String type;

    @GenericModel.DataType(type = DataTypeEnum.TEXT)
    @SerializedName("BaseURL")
    public String baseUrl;

    @GenericModel.DataType(type = DataTypeEnum.TEXT)
    @SerializedName("Name")
    public String name;

    @GenericModel.DataType(type = DataTypeEnum.TEXT)
    @SerializedName("ShortName")
    public String shortName;

    @GenericModel.DataType(type = DataTypeEnum.INTEGER)
    @SerializedName("Order")
    public int position;

    @GenericModel.DataType(type = DataTypeEnum.TEXT)
    @SerializedName("Thumbnail")
    public String thumbnail;

    @GenericModel.DataType(type = DataTypeEnum.BOOLEAN)
    @SerializedName("Landscape")
    public boolean landscape;

    @SerializedName("Files")
    public String[] files;

    //@GenericModel.DataType(type = DataTypeEnum.BLOB)
    //public byte[] fileNamesAsByteArray;

    @SerializedName("Pages")
    public TemplatePage[] pages;

    @SerializedName("DataSize")
    public int dataSize;

    /**
     * Implicitni konstruktor kvuli SDD
     */
    public Template() {

    }

    /**
     * Kopirovaci konstruktor
     */
    public Template(Template template) {

        this.ident = template.ident;
        this.published = template.published;
        this.baseUrl = template.baseUrl;
        this.name = template.name;
        this.position = template.position;
        this.thumbnail = template.thumbnail;
        this.landscape = template.landscape;
        this.shortName = template.shortName;
        this.type = template.type;
        this.version = template.version;
    }

    public void deleteCompleteTemplate() {

        for (TemplatePage templatePage : getTemplatePagesByTemplate()) {
            for (TemplateTag templateTag : templatePage.getTemplateTagsByPage()) {
                templateTag.delete();
            }
            templatePage.delete();
        }
        super.delete();
    }

    /**
     * Remove all the templates given by list.
     * @param templates The list of templates.
     */
    public static void removeTemplates(List<Template> templates) {
        for (Template template : templates) {
            Ln.i("Removing old template id %s", template.getIdentForFolderName());
            template.deleteCompleteTemplate();
        }
    }

    public int getMajorVersion() {
        return Integer.decode(version.split("\\.")[0]);
    }

    public int getMinorVersion() {
        return Integer.decode(version.split("\\.")[1]);
    }

    /**
     * Compares given templates against locally stored templates. All the local templates with major version which is
     * not listed are returned.
     * @param serverTemplates The templates from server
     */
    public static void findAndDeleteMissing(List<Template> serverTemplates) {
        List<Template> templatesInDb = Template.getAllObjects(Template.class);
        List<Template> templatesToDelete = new ArrayList<Template>(templatesInDb);
        //for (Template templateInDb : templatesInDb) {
            for (Template serverTemplate : serverTemplates) {
                templatesToDelete.remove(serverTemplate);
                /*if (templateInDb.getMajorVersion() == serverTemplate.getMajorVersion()) {
                    templatesToDelete.remove(templateInDb);
                }*/
            }
        //}
        removeTemplates(templatesToDelete);
    }

    public List<TemplatePage> getTemplatePagesByTemplate() {

        return TemplatePage.getByQuery(TemplatePage.class, "templateId=" + this.id.getId());
    }

    public String getIdentForFolderName() {
        return ident + "_" + version;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Template template = (Template) o;

        if (getIdentForFolderName() != null
                ? !getIdentForFolderName().equals(template.getIdentForFolderName())
                : template.getIdentForFolderName() != null)
            return false;

        return true;
    }

    @Override
    public int hashCode() {
        return getIdentForFolderName() != null ? getIdentForFolderName().hashCode() : 0;
    }

    @Override
    public String toString() {

        return name;
    }
}
