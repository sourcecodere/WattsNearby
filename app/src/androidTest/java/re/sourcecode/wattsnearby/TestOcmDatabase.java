package re.sourcecode.wattsnearby;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.test.runner.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.HashSet;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;

/**
 * Created by olem on 3/19/17.
 */

@RunWith(AndroidJUnit4.class)
public class TestOcmDatabase {

    private static final String databaseVersionVariableName = "DATABASE_VERSION";
    private static int REFLECTED_DATABASE_VERSION;

    private static final String tableNameVariableName = "TABLE_1_NAME";
    private static String REFLECTED_TABLE_1_NAME;

    private static final String tableNameVariableName = "TABLE_2_NAME";
    private static String REFLECTED_TABLE_2_NAME;


    private SQLiteDatabase database;


    @Test
    public void testDatabaseVersionWasIncremented() {
        int expectedDatabaseVersion = 3;
        String databaseVersionShouldBe1 = "Database version should be "
                + expectedDatabaseVersion + " but isn't."
                + "\n Database version: ";

        assertEquals(databaseVersionShouldBe1,
                expectedDatabaseVersion,
                REFLECTED_DATABASE_VERSION);
    }

    /**
     * This method tests that our database contains all of the tables that we think it should
     * contain.
     * <p>
     * {@link re.sourcecode.wattsnearby.data.OcmContract.ConnectionEntry#TABLE_NAME}.
     * {@link re.sourcecode.wattsnearby.data.OcmContract.StationEntry#TABLE_NAME}.
     * <p>
     */
    @Test
    public void testCreateDb() {
        /*
         * Will contain the name of every table in our database. Even though in our case, we only
         * have only table, in many cases, there are multiple tables. Because of that, we are
         * showing you how to test that a database with multiple tables was created properly.
         */
        final HashSet<String> tableNameHashSet = new HashSet<>();

        /* Here, we add the name of our only table in this particular database */
        tableNameHashSet.add(REFLECTED_TABLE_1_NAME);
        tableNameHashSet.add(REFLECTED_TABLE_2_NAME);
        /* Students, here is where you would add any other table names if you had them */
//        tableNameHashSet.add(MyAwesomeSuperCoolTableName);
//        tableNameHashSet.add(MyOtherCoolTableNameThatContainsOtherCoolData);

        /* We think the database is open, let's verify that here */
        String databaseIsNotOpen = "The database should be open and isn't";
        assertEquals(databaseIsNotOpen,
                true,
                database.isOpen());

        /* This Cursor will contain the names of each table in our database */
        Cursor tableNameCursor = database.rawQuery(
                "SELECT name FROM sqlite_master WHERE type='table'",
                null);

        /*
         * If tableNameCursor.moveToFirst returns false from this query, it means the database
         * wasn't created properly. In actuality, it means that your database contains no tables.
         */
        String errorInCreatingDatabase =
                "Error: This means that the database has not been created correctly";
        assertTrue(errorInCreatingDatabase,
                tableNameCursor.moveToFirst());

        /*
         * tableNameCursor contains the name of each table in this database. Here, we loop over
         * each table that was ACTUALLY created in the database and remove it from the
         * tableNameHashSet to keep track of the fact that was added. At the end of this loop, we
         * should have removed every table name that we thought we should have in our database.
         * If the tableNameHashSet isn't empty after this loop, there was a table that wasn't
         * created properly.
         */
        do {
            tableNameHashSet.remove(tableNameCursor.getString(0));
        } while (tableNameCursor.moveToNext());

        /* If this fails, it means that your database doesn't contain the expected table(s) */
        assertTrue("Error: Your database was created without the expected tables.",
                tableNameHashSet.isEmpty());

        /* Always close the cursor when you are finished with it */
        tableNameCursor.close();
    }


}
