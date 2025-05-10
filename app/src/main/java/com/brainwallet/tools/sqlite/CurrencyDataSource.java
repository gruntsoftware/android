package com.brainwallet.tools.sqlite;

import static kotlinx.coroutines.flow.FlowKt.collect;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.brainwallet.data.model.CurrencyEntity;
import com.brainwallet.tools.util.BRConstants;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import timber.log.Timber;

public class CurrencyDataSource implements BRDataSourceInterface {
    private SQLiteDatabase database;
    private final BRSQLiteHelper dbHelper;

    // Database fields
    private final String[] allColumns = {
            BRSQLiteHelper.CURRENCY_CODE,
            BRSQLiteHelper.CURRENCY_NAME,
            BRSQLiteHelper.CURRENCY_RATE
    };

    /// Set the most popular fiats
    /// Hack: Database needs a symbol column. This is injected here.
    private final HashMap<String, String> codeSymbolsMap = new HashMap<String, String>() {{
        put("USD","$");
        put("EUR","€");
        put("GBP","£");
        put("SGD","$");
        put("CAD","$");
        put("AUD","$");
        put("RUB","₽");
        put("KRW","₩");
        put("MXN","$");
        put("SAR","﷼");
        put("UAH","₴");
        put("NGN","₦");
        put("JPY","¥");
        put("CNY","¥");
        put("IDR","Rp");
        put("TRY","₺");
    }};

    private static CurrencyDataSource instance;

    public static CurrencyDataSource getInstance(Context context) {
        if (instance == null) {
            instance = new CurrencyDataSource(context);
        }
        return instance;
    }

    public CurrencyDataSource(Context context) {
        dbHelper = BRSQLiteHelper.getInstance(context);
    }

    public void putCurrencies(Collection<CurrencyEntity> currencyEntities) {
        if (currencyEntities == null || currencyEntities.size() <= 0) return;

        try {
            database = openDatabase();
            database.beginTransaction();
            for (CurrencyEntity c : currencyEntities) {
                ContentValues values = new ContentValues();
                values.put(BRSQLiteHelper.CURRENCY_CODE, c.code);
                values.put(BRSQLiteHelper.CURRENCY_NAME, c.name);
                values.put(BRSQLiteHelper.CURRENCY_RATE, c.rate);
                database.insertWithOnConflict(BRSQLiteHelper.CURRENCY_TABLE_NAME, null, values, SQLiteDatabase.CONFLICT_REPLACE);
            }

            database.setTransactionSuccessful();
        } catch (Exception ex) {
            Timber.e(ex);
        } finally {
            database.endTransaction();
            closeDatabase();
        }
    }

    public List<CurrencyEntity> getAllCurrencies(Boolean shouldBeFiltered) {

        List<CurrencyEntity> currencies = new ArrayList<>();

        /// Set the most popular fiats
        List<String> filteredFiatCodes =
                Arrays.asList("USD","EUR","GBP", "SGD","CAD","AUD","RUB","KRW","MXN","SAR","UAH","NGN","JPY","CNY","IDR","TRY");

            Cursor cursor = null;
            try {
                database = openDatabase();

                cursor = database.query(BRSQLiteHelper.CURRENCY_TABLE_NAME,
                        allColumns, null, null, null, null, "\'" + BRSQLiteHelper.CURRENCY_CODE + "\'");

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

            if (shouldBeFiltered) {
                // Filtering Brainwallet fiats
                List<CurrencyEntity> filteredFiats = currencies.stream()
                        .filter(currency -> filteredFiatCodes.contains(currency.code))
                        .collect(Collectors.toList());
                currencies = filteredFiats;

                // Load the symbols to Brainwallet fiats
                List<CurrencyEntity> completeCurrencyEntities = new ArrayList<>();
                for(int i=0;i<currencies.size();i++) {
                    CurrencyEntity entity = currencies.get(i);
                    entity.symbol = Objects.requireNonNullElse(codeSymbolsMap.get(entity.code), "");
                    completeCurrencyEntities.add(entity);
                }
                currencies = completeCurrencyEntities;
            }
            return currencies;
        }

    public CurrencyEntity getCurrencyByIso(String iso) {
        Cursor cursor = null;
        try {
            database = openDatabase();

            cursor = database.query(BRSQLiteHelper.CURRENCY_TABLE_NAME,
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
        return new CurrencyEntity(
                cursor.getString(0),
                cursor.getString(1),
                cursor.getFloat(2),
                Objects.requireNonNullElse(codeSymbolsMap.get(cursor.getString(0)), "")
        );
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