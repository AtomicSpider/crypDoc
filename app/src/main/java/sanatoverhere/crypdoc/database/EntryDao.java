package sanatoverhere.crypdoc.database;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.RawQuery;
import androidx.room.Update;
import androidx.sqlite.db.SupportSQLiteQuery;

import java.util.List;

@Dao
public interface EntryDao {

    @RawQuery
    int checkpoint(SupportSQLiteQuery supportSQLiteQuery);

    @Query("SELECT * FROM entry ORDER BY type DESC, title COLLATE NOCASE ASC")
    List<Entry> getEntryList();

    @Query("SELECT COUNT(id) FROM entry WHERE type LIKE :type")
    int getEntryCount(String type);

    @Insert
    long insertEntry(Entry entry);

    @Update
    int updateEntry(Entry entry);

    @Delete
    void deleteEntry(Entry entry);
}
