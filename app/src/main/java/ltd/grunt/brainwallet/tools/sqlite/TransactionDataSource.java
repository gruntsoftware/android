package ltd.grunt.brainwallet.tools.sqlite;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.NetworkOnMainThreadException;

import ltd.grunt.brainwallet.presenter.activities.util.ActivityUTILS;
import ltd.grunt.brainwallet.presenter.entities.BRTransactionEntity;
import ltd.grunt.brainwallet.tools.sqlite.BRDataSourceInterface;
import ltd.grunt.brainwallet.tools.sqlite.BRSQLiteHelper;
import ltd.grunt.brainwallet.tools.util.BRConstants;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import timber.log.Timber;

public class TransactionDataSource implements BRDataSourceInterface {
    private static final String TAG = TransactionDataSource.class.getName();
    private AtomicInteger mOpenCounter = new AtomicInteger();

    // Database fields
    private SQLiteDatabase database;
    private final ltd.grunt.brainwallet.tools.sqlite.BRSQLiteHelper dbHelper;
    private final String[] allColumns = {
            ltd.grunt.brainwallet.tools.sqlite.BRSQLiteHelper.TX_COLUMN_ID,
            ltd.grunt.brainwallet.tools.sqlite.BRSQLiteHelper.TX_BUFF,
            ltd.grunt.brainwallet.tools.sqlite.BRSQLiteHelper.TX_BLOCK_HEIGHT,
            ltd.grunt.brainwallet.tools.sqlite.BRSQLiteHelper.TX_TIME_STAMP
    };

    public interface OnTxAddedListener {
        void onTxAdded();
    }

    List<OnTxAddedListener> listeners = new ArrayList<>();

    public void addTxAddedListener(OnTxAddedListener listener) {
        if (!listeners.contains(listener))
            listeners.add(listener);
    }

    public void removeListener(OnTxAddedListener listener) {
        listeners.remove(listener);

    }

    private static TransactionDataSource instance;

    public static TransactionDataSource getInstance(Context context) {
        if (instance == null) {
            instance = new TransactionDataSource(context);
        }
        return instance;
    }


    private TransactionDataSource(Context context) {
        dbHelper = ltd.grunt.brainwallet.tools.sqlite.BRSQLiteHelper.getInstance(context);
    }

    public BRTransactionEntity putTransaction(BRTransactionEntity transactionEntity) {
        Cursor cursor = null;
        try {
            database = openDatabase();
            ContentValues values = new ContentValues();
            values.put(ltd.grunt.brainwallet.tools.sqlite.BRSQLiteHelper.TX_COLUMN_ID, transactionEntity.getTxHash());
            values.put(ltd.grunt.brainwallet.tools.sqlite.BRSQLiteHelper.TX_BUFF, transactionEntity.getBuff());
            values.put(ltd.grunt.brainwallet.tools.sqlite.BRSQLiteHelper.TX_BLOCK_HEIGHT, transactionEntity.getBlockheight());
            values.put(ltd.grunt.brainwallet.tools.sqlite.BRSQLiteHelper.TX_TIME_STAMP, transactionEntity.getTimestamp());

            database.beginTransaction();
            database.insert(ltd.grunt.brainwallet.tools.sqlite.BRSQLiteHelper.TX_TABLE_NAME, null, values);
            cursor = database.query(ltd.grunt.brainwallet.tools.sqlite.BRSQLiteHelper.TX_TABLE_NAME,
                    allColumns, null, null, null, null, null);
            cursor.moveToFirst();
            BRTransactionEntity transactionEntity1 = cursorToTransaction(cursor);

            database.setTransactionSuccessful();
            for (OnTxAddedListener listener : listeners) {
                if (listener != null) listener.onTxAdded();
            }
            return transactionEntity1;
        } catch (Exception ex) {
            Timber.e(ex, "Error inserting into SQLite");
        } finally {
            database.endTransaction();
            closeDatabase();
            if (cursor != null) cursor.close();
        }
        return null;
    }

    public void deleteAllTransactions() {
        try {
            database = openDatabase();
            database.delete(ltd.grunt.brainwallet.tools.sqlite.BRSQLiteHelper.TX_TABLE_NAME, null, null);
        } finally {
            closeDatabase();
        }
    }

    public List<BRTransactionEntity> getAllTransactions() {
        List<BRTransactionEntity> transactions = new ArrayList<>();
        Cursor cursor = null;
        try {
            database = openDatabase();

            cursor = database.query(ltd.grunt.brainwallet.tools.sqlite.BRSQLiteHelper.TX_TABLE_NAME,
                    allColumns, null, null, null, null, null);

            cursor.moveToFirst();
            while (!cursor.isAfterLast()) {
                BRTransactionEntity transactionEntity = cursorToTransaction(cursor);
                transactions.add(transactionEntity);
                cursor.moveToNext();
            }
        } finally {
            closeDatabase();
            if (cursor != null)
                cursor.close();
        }
        return transactions;
    }

    private BRTransactionEntity cursorToTransaction(Cursor cursor) {
        return new BRTransactionEntity(cursor.getBlob(1), cursor.getInt(2), cursor.getLong(3), cursor.getString(0));
    }

    public void updateTxBlockHeight(String hash, int blockHeight, int timeStamp) {
        try {
            database = openDatabase();
            Timber.d("timber: transaction updated with id: %s", hash);
            String strFilter = "_id=\'" + hash + "\'";
            ContentValues args = new ContentValues();
            args.put(ltd.grunt.brainwallet.tools.sqlite.BRSQLiteHelper.TX_BLOCK_HEIGHT, blockHeight);
            args.put(ltd.grunt.brainwallet.tools.sqlite.BRSQLiteHelper.TX_TIME_STAMP, timeStamp);

            database.update(ltd.grunt.brainwallet.tools.sqlite.BRSQLiteHelper.TX_TABLE_NAME, args, strFilter, null);
        } finally {
            closeDatabase();
        }
    }

    public void deleteTxByHash(String hash) {
        try {
            database = openDatabase();
            Timber.d("timber: transaction deleted with id: %s", hash);
            database.delete(ltd.grunt.brainwallet.tools.sqlite.BRSQLiteHelper.TX_TABLE_NAME, BRSQLiteHelper.TX_COLUMN_ID
                    + " = \'" + hash + "\'", null);
        } finally {
            closeDatabase();
        }
    }

    @Override
    public SQLiteDatabase openDatabase() {
        if (ActivityUTILS.isMainThread()) throw new NetworkOnMainThreadException();
        // Opening new database
        if (database == null || !database.isOpen())
            database = dbHelper.getWritableDatabase();
        dbHelper.setWriteAheadLoggingEnabled(BRConstants.WAL);
        return database;
    }

    @Override
    public void closeDatabase() {
//        if (mOpenCounter.decrementAndGet() == 0) {
//            // Closing database
//            database.close();

//        }
//        Log.d("Database open counter: " , String.valueOf(mOpenCounter.get()));
    }
}