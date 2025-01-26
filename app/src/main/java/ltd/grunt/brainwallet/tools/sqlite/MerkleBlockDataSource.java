package ltd.grunt.brainwallet.tools.sqlite;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.NetworkOnMainThreadException;

import ltd.grunt.brainwallet.presenter.activities.util.ActivityUTILS;
import ltd.grunt.brainwallet.presenter.entities.BRMerkleBlockEntity;
import ltd.grunt.brainwallet.presenter.entities.BlockEntity;
import ltd.grunt.brainwallet.tools.sqlite.BRDataSourceInterface;
import ltd.grunt.brainwallet.tools.sqlite.BRSQLiteHelper;
import ltd.grunt.brainwallet.tools.util.BRConstants;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import timber.log.Timber;

public class MerkleBlockDataSource implements BRDataSourceInterface {
    private static final String TAG = MerkleBlockDataSource.class.getName();
    private AtomicInteger mOpenCounter = new AtomicInteger();

    // Database fields
    private SQLiteDatabase database;
    private final ltd.grunt.brainwallet.tools.sqlite.BRSQLiteHelper dbHelper;
    private final String[] allColumns = {
            ltd.grunt.brainwallet.tools.sqlite.BRSQLiteHelper.MB_COLUMN_ID,
            ltd.grunt.brainwallet.tools.sqlite.BRSQLiteHelper.MB_BUFF,
            ltd.grunt.brainwallet.tools.sqlite.BRSQLiteHelper.MB_HEIGHT
    };

    private static MerkleBlockDataSource instance;

    public static MerkleBlockDataSource getInstance(Context context) {
        if (instance == null) {
            instance = new MerkleBlockDataSource(context);
        }
        return instance;
    }

    private MerkleBlockDataSource(Context context) {
        dbHelper = ltd.grunt.brainwallet.tools.sqlite.BRSQLiteHelper.getInstance(context);
    }

    public void putMerkleBlocks(BlockEntity[] blockEntities) {
        try {
            database = openDatabase();
            database.beginTransaction();
            for (BlockEntity b : blockEntities) {
                ContentValues values = new ContentValues();
                values.put(ltd.grunt.brainwallet.tools.sqlite.BRSQLiteHelper.MB_BUFF, b.getBlockBytes());
                values.put(ltd.grunt.brainwallet.tools.sqlite.BRSQLiteHelper.MB_HEIGHT, b.getBlockHeight());
                database.insert(ltd.grunt.brainwallet.tools.sqlite.BRSQLiteHelper.MB_TABLE_NAME, null, values);
            }
            database.setTransactionSuccessful();
        } catch (Exception ex) {
            Timber.e(ex, "Error inserting into SQLite");
        } finally {
            database.endTransaction();
            closeDatabase();
        }
    }

    public void deleteAllBlocks() {
        try {
            database = openDatabase();
            database.delete(ltd.grunt.brainwallet.tools.sqlite.BRSQLiteHelper.MB_TABLE_NAME, ltd.grunt.brainwallet.tools.sqlite.BRSQLiteHelper.MB_COLUMN_ID + " <> -1", null);
        } finally {
            closeDatabase();
        }
    }

    public void deleteMerkleBlock(BRMerkleBlockEntity merkleBlock) {
        try {
            database = openDatabase();
            long id = merkleBlock.getId();
            Timber.d("timber: MerkleBlock deleted with id: %s", id);
            database.delete(ltd.grunt.brainwallet.tools.sqlite.BRSQLiteHelper.MB_TABLE_NAME, ltd.grunt.brainwallet.tools.sqlite.BRSQLiteHelper.MB_COLUMN_ID
                    + " = " + id, null);
        } finally {
            closeDatabase();
        }
    }

    public List<BRMerkleBlockEntity> getAllMerkleBlocks() {
        List<BRMerkleBlockEntity> merkleBlocks = new ArrayList<>();
        Cursor cursor = null;
        try {
            database = openDatabase();

            cursor = database.query(BRSQLiteHelper.MB_TABLE_NAME,
                    allColumns, null, null, null, null, null);

            cursor.moveToFirst();
            while (!cursor.isAfterLast()) {
                BRMerkleBlockEntity merkleBlockEntity = cursorToMerkleBlock(cursor);
                merkleBlocks.add(merkleBlockEntity);
                cursor.moveToNext();
            }
            Timber.d("timber: merkleBlocks: %s", merkleBlocks.size());
        } finally {
            closeDatabase();
            if (cursor != null) cursor.close();
        }
        return merkleBlocks;
    }

    private BRMerkleBlockEntity cursorToMerkleBlock(Cursor cursor) {
        BRMerkleBlockEntity merkleBlockEntity = new BRMerkleBlockEntity(cursor.getBlob(1), cursor.getInt(2));
        merkleBlockEntity.setId(cursor.getInt(0));

        return merkleBlockEntity;
    }

    @Override
    public SQLiteDatabase openDatabase() {
//        if (mOpenCounter.incrementAndGet() == 1) {
        // Opening new database
        if (ActivityUTILS.isMainThread()) throw new NetworkOnMainThreadException();
        if (database == null || !database.isOpen())
            database = dbHelper.getWritableDatabase();
        dbHelper.setWriteAheadLoggingEnabled(BRConstants.WAL);
//        }
        return database;
    }

    @Override
    public void closeDatabase() {
//        if (mOpenCounter.decrementAndGet() == 0) {
//            // Closing database
//            database.close();
//
//        }
//        Log.d("Database open counter: " , String.valueOf(mOpenCounter.get()));
    }
}