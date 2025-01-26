package ltd.grunt.brainwallet.tools.sqlite;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import ltd.grunt.brainwallet.presenter.entities.CurrencyEntity;
import ltd.grunt.brainwallet.tools.sqlite.BRDataSourceInterface;
import ltd.grunt.brainwallet.tools.sqlite.BRSQLiteHelper;
import ltd.grunt.brainwallet.tools.util.BRConstants;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import timber.log.Timber;

public class CurrencyDataSource implements BRDataSourceInterface {
    private SQLiteDatabase database;
    private final ltd.grunt.brainwallet.tools.sqlite.BRSQLiteHelper dbHelper;

    // Database fields
    private final String[] allColumns = {
            ltd.grunt.brainwallet.tools.sqlite.BRSQLiteHelper.CURRENCY_CODE,
            ltd.grunt.brainwallet.tools.sqlite.BRSQLiteHelper.CURRENCY_NAME,
            ltd.grunt.brainwallet.tools.sqlite.BRSQLiteHelper.CURRENCY_RATE
    };

    private static CurrencyDataSource instance;

    public static CurrencyDataSource getInstance(Context context) {
        if (instance == null) {
            instance = new CurrencyDataSource(context);
        }
        return instance;
    }

    public CurrencyDataSource(Context context) {
        dbHelper = ltd.grunt.brainwallet.tools.sqlite.BRSQLiteHelper.getInstance(context);
    }

    public void putCurrencies(Collection<CurrencyEntity> currencyEntities) {
        if (currencyEntities == null || currencyEntities.size() <= 0) return;

        try {
            database = openDatabase();
            database.beginTransaction();
            for (CurrencyEntity c : currencyEntities) {
                ContentValues values = new ContentValues();
                values.put(ltd.grunt.brainwallet.tools.sqlite.BRSQLiteHelper.CURRENCY_CODE, c.code);
                values.put(ltd.grunt.brainwallet.tools.sqlite.BRSQLiteHelper.CURRENCY_NAME, c.name);
                values.put(ltd.grunt.brainwallet.tools.sqlite.BRSQLiteHelper.CURRENCY_RATE, c.rate);
                database.insertWithOnConflict(ltd.grunt.brainwallet.tools.sqlite.BRSQLiteHelper.CURRENCY_TABLE_NAME, null, values, SQLiteDatabase.CONFLICT_REPLACE);
            }

            database.setTransactionSuccessful();
        } catch (Exception ex) {
            Timber.e(ex);
        } finally {
            database.endTransaction();
            closeDatabase();
        }
    }

    public List<CurrencyEntity> getAllCurrencies() {

        List<CurrencyEntity> currencies = new ArrayList<>();
        Cursor cursor = null;
        try {
            database = openDatabase();

            cursor = database.query(ltd.grunt.brainwallet.tools.sqlite.BRSQLiteHelper.CURRENCY_TABLE_NAME,
                    allColumns, null, null, null, null, "\'" + ltd.grunt.brainwallet.tools.sqlite.BRSQLiteHelper.CURRENCY_CODE + "\'");

            cursor.moveToFirst();
            while (!cursor.isAfterLast()) {
                CurrencyEntity curEntity = cursorToCurrency(cursor);
                currencies.add(curEntity);
                cursor.moveToNext();
            }
        } finally {
            if (cursor != null)
                cursor.close();
            closeDatabase();
        }

        return currencies;
    }

    public CurrencyEntity getCurrencyByIso(String iso) {
        Cursor cursor = null;
        try {
            database = openDatabase();

            cursor = database.query(ltd.grunt.brainwallet.tools.sqlite.BRSQLiteHelper.CURRENCY_TABLE_NAME,
                    allColumns, BRSQLiteHelper.CURRENCY_CODE + " = ?", new String[]{iso}, null, null, null);

            cursor.moveToFirst();
            if (!cursor.isAfterLast()) {
                return cursorToCurrency(cursor);
            }
        } finally {
            if (cursor != null)
                cursor.close();
            closeDatabase();
        }

        return null;
    }

    private CurrencyEntity cursorToCurrency(Cursor cursor) {
        return new CurrencyEntity(cursor.getString(0), cursor.getString(1), cursor.getFloat(2));
    }

    @Override
    public SQLiteDatabase openDatabase() {
        if (database == null || !database.isOpen())
            database = dbHelper.getWritableDatabase();
        dbHelper.setWriteAheadLoggingEnabled(BRConstants.WAL);
        return database;
    }

    @Override
    public void closeDatabase() {
    }
}