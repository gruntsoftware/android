package ltd.grunt.brainwallet.tools.sqlite;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.NetworkOnMainThreadException;

import ltd.grunt.brainwallet.presenter.activities.util.ActivityUTILS;
import ltd.grunt.brainwallet.presenter.entities.BRPeerEntity;
import ltd.grunt.brainwallet.presenter.entities.PeerEntity;
import ltd.grunt.brainwallet.tools.sqlite.BRDataSourceInterface;
import ltd.grunt.brainwallet.tools.sqlite.BRSQLiteHelper;
import ltd.grunt.brainwallet.tools.util.BRConstants;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import timber.log.Timber;

public class PeerDataSource implements BRDataSourceInterface {
    private static final String TAG = PeerDataSource.class.getName();

    private AtomicInteger mOpenCounter = new AtomicInteger();

    // Database fields
    private SQLiteDatabase database;
    private final ltd.grunt.brainwallet.tools.sqlite.BRSQLiteHelper dbHelper;
    private final String[] allColumns = {
            ltd.grunt.brainwallet.tools.sqlite.BRSQLiteHelper.PEER_COLUMN_ID,
            ltd.grunt.brainwallet.tools.sqlite.BRSQLiteHelper.PEER_ADDRESS,
            ltd.grunt.brainwallet.tools.sqlite.BRSQLiteHelper.PEER_PORT,
            ltd.grunt.brainwallet.tools.sqlite.BRSQLiteHelper.PEER_TIMESTAMP
    };

    private static PeerDataSource instance;

    public static PeerDataSource getInstance(Context context) {
        if (instance == null) {
            instance = new PeerDataSource(context);
        }
        return instance;
    }

    private PeerDataSource(Context context) {
        dbHelper = ltd.grunt.brainwallet.tools.sqlite.BRSQLiteHelper.getInstance(context);
    }

    public void putPeers(PeerEntity[] peerEntities) {

        try {
            database = openDatabase();
            database.beginTransaction();
            for (PeerEntity p : peerEntities) {
                Timber.d("timber: SQLite peer saved: %s", Arrays.toString(p.getPeerTimeStamp()));
                ContentValues values = new ContentValues();
                values.put(ltd.grunt.brainwallet.tools.sqlite.BRSQLiteHelper.PEER_ADDRESS, p.getPeerAddress());
                values.put(ltd.grunt.brainwallet.tools.sqlite.BRSQLiteHelper.PEER_PORT, p.getPeerPort());
                values.put(ltd.grunt.brainwallet.tools.sqlite.BRSQLiteHelper.PEER_TIMESTAMP, p.getPeerTimeStamp());
                database.insert(ltd.grunt.brainwallet.tools.sqlite.BRSQLiteHelper.PEER_TABLE_NAME, null, values);
            }

            database.setTransactionSuccessful();
        } catch (Exception ex) {
            Timber.e(ex, "Error inserting into SQLite");
        } finally {
            database.endTransaction();
            closeDatabase();
        }

    }

    public void deletePeer(BRPeerEntity peerEntity) {
        try {
            database = openDatabase();
            long id = peerEntity.getId();
            Timber.d("timber: Peer deleted with id: %s", id);
            database.delete(ltd.grunt.brainwallet.tools.sqlite.BRSQLiteHelper.PEER_TABLE_NAME, ltd.grunt.brainwallet.tools.sqlite.BRSQLiteHelper.PEER_COLUMN_ID
                    + " = " + id, null);
        } finally {
            closeDatabase();
        }
    }

    public void deleteAllPeers() {
        try {
            database = dbHelper.getWritableDatabase();
            database.delete(ltd.grunt.brainwallet.tools.sqlite.BRSQLiteHelper.PEER_TABLE_NAME, ltd.grunt.brainwallet.tools.sqlite.BRSQLiteHelper.PEER_COLUMN_ID + " <> -1", null);
        } finally {
            closeDatabase();
        }
    }

    public List<BRPeerEntity> getAllPeers() {
        List<BRPeerEntity> peers = new ArrayList<>();
        Cursor cursor = null;
        try {
            database = openDatabase();

            cursor = database.query(BRSQLiteHelper.PEER_TABLE_NAME,
                    allColumns, null, null, null, null, null);

            cursor.moveToFirst();
            while (!cursor.isAfterLast()) {
                BRPeerEntity peerEntity = cursorToPeer(cursor);
                peers.add(peerEntity);
                cursor.moveToNext();
            }
        } finally {
            if (cursor != null)
                cursor.close();
            closeDatabase();
        }
        Timber.d("timber: peers: %s", peers.size());
        return peers;
    }

    private BRPeerEntity cursorToPeer(Cursor cursor) {
        BRPeerEntity peerEntity = new BRPeerEntity(cursor.getBlob(1), cursor.getBlob(2), cursor.getBlob(3));
        peerEntity.setId(cursor.getInt(0));
        return peerEntity;
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
    }
}