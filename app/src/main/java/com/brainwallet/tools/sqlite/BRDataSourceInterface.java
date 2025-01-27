package com.brainwallet.tools.sqlite;
import android.database.sqlite.SQLiteDatabase;

public interface BRDataSourceInterface {
    public static final String TAG = BRDataSourceInterface.class.getName();

    SQLiteDatabase openDatabase();
    void closeDatabase();
}
