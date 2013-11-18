package eu.janmuller.application.salesmenapp.model.db;

import com.google.gson.annotations.SerializedName;
import eu.janmuller.android.dao.api.BaseDateModel;
import eu.janmuller.android.dao.api.GenericModel;
import eu.janmuller.android.dao.exceptions.DaoConstraintException;

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

    @GenericModel.DataType(type = DataTypeEnum.FLOAT)
    @SerializedName("Version")
    public float version;

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

    @GenericModel.DataType(type = DataTypeEnum.BOOLEAN)
    public boolean dataDownloaded;

    @SerializedName("Files")
    public String[] files;

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
        this.published = template.published;
        this.baseUrl = template.baseUrl;
        this.name = template.name;
        this.position = template.position;
        this.thumbnail = template.thumbnail;
        this.landscape = template.landscape;
        this.shortName = template.shortName;
        this.type = template.type;
        this.dataDownloaded = template.dataDownloaded;
    }

    /**
     * Smaze sablonu vcetne odkazu na ni (Pages)
     *
     * @throws DaoConstraintException
     */
    /*@Override
    public void delete() throws DaoConstraintException {

        List<Page> pageList = Page.getByQuery(Page.class, "templateId=" + id.getId());
        for (Page page : pageList) {

            page.delete();
        }
        super.delete();
    }*/
    @Override
    public String toString() {

        return name;
    }
}
