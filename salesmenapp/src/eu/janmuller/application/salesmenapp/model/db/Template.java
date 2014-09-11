package eu.janmuller.application.salesmenapp.model.db;

import com.google.gson.annotations.SerializedName;
import eu.janmuller.android.dao.api.BaseDateModel;
import eu.janmuller.android.dao.api.GenericModel;
import eu.janmuller.android.dao.exceptions.DaoConstraintException;

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
    @SerializedName("Version")
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

    @GenericModel.DataType(type = DataTypeEnum.BLOB)
    public byte[] fileNamesAsByteArray;

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

        this.version = template.version;
        this.ident = template.ident;
        this.published = template.published;
        this.baseUrl = template.baseUrl;
        this.name = template.name;
        this.position = template.position;
        this.thumbnail = template.thumbnail;
        this.landscape = template.landscape;
        this.shortName = template.shortName;
        this.type = template.type;
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

    public List<TemplatePage> getTemplatePagesByTemplate() {

        return TemplatePage.getByQuery(TemplatePage.class, "templateId=" + this.id.getId());
    }

    public int getMajorVersion() {
        return Integer.parseInt(version.split("\\.")[0]);
    }

    public int getMinorVersion() {
        return Integer.parseInt(version.split("\\.")[1]);
    }

    @Override
    public boolean equals(Object o) {

        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Template template = (Template) o;

        if (template.getMajorVersion() != getMajorVersion()) {
            return false;
        }
        if (!ident.equals(template.ident)) return false;

        return true;
    }

    @Override
    public int hashCode() {

        int result = ident.hashCode();
        result = 31 * result + (getMajorVersion() != 0 ? getMajorVersion() : 0);
        return result;
    }

    @Override
    public String toString() {

        return name;
    }
}
