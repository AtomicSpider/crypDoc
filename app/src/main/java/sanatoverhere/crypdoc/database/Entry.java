package sanatoverhere.crypdoc.database;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;

import java.util.Date;

@Entity(tableName = "entry")
public class Entry {
    @PrimaryKey(autoGenerate = true)
    public long id;

    @ColumnInfo(name = "title")
    public String title;

    @ColumnInfo(name = "filename")
    public String filename;

    @ColumnInfo(name = "type")
    public String type;

    @ColumnInfo(name = "date_created", defaultValue = "CURRENT_TIMESTAMP")
    @TypeConverters(DateConverter.class)
    public Date date_created;

    @ColumnInfo(name = "date_modified", defaultValue = "CURRENT_TIMESTAMP")
    @TypeConverters(DateConverter.class)
    public Date date_modified;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Date getDate_created() {
        return date_created;
    }

    public void setDate_created(Date date_created) {
        this.date_created = date_created;
    }

    public Date getDate_modified() {
        return date_modified;
    }

    public void setDate_modified(Date date_modified) {
        this.date_modified = date_modified;
    }

    public Entry(String title, String filename, String type, Date date_created, Date date_modified) {
        this.title = title;
        this.filename = filename;
        this.type = type;
        this.date_created = date_created;
        this.date_modified = date_modified;
    }

    @Override
    public String toString() {
        return "Entry{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", filename='" + filename + '\'' +
                ", type='" + type + '\'' +
                ", date_created=" + date_created +
                ", date_modified=" + date_modified +
                '}';
    }
}
