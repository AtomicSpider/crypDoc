package sanatoverhere.crypdoc.database;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import static sanatoverhere.crypdoc.common.Consts.DB_NAME;

@Database(entities = Entry.class, exportSchema = false, version = 1)
public abstract class CryptoDatabase extends RoomDatabase {
    private static CryptoDatabase instance;

    public static synchronized CryptoDatabase getInstance(Context context) {
        if (instance == null) {
            instance = Room.databaseBuilder(context.getApplicationContext(), CryptoDatabase.class, DB_NAME)
                    .fallbackToDestructiveMigration()
                    .setJournalMode(JournalMode.TRUNCATE)
                    .allowMainThreadQueries()
                    .build();
        }
        return instance;
    }

    public abstract EntryDao entryDao();
}
