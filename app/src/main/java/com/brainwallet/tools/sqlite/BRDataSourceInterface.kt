package com.brainwallet.tools.sqlite

import android.database.sqlite.SQLiteDatabase

interface BRDataSourceInterface {
    fun openDatabase(): SQLiteDatabase?
    fun closeDatabase()

    companion object {
        val TAG: String = BRDataSourceInterface::class.java.getName()
    }
}
