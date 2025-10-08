package com.example.evcharging.database

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log

class UserDatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "ev_charging.db"
        private const val DATABASE_VERSION = 3

        // EV Owners Table
        private const val TABLE_EV_OWNERS = "ev_owners"
        private const val COLUMN_NIC = "nic"
        private const val COLUMN_FULL_NAME = "full_name"
        private const val COLUMN_EMAIL = "email"
        private const val COLUMN_PHONE = "phone"
        private const val COLUMN_PASSWORD = "password"
        private const val COLUMN_IS_ACTIVE = "is_active"
        private const val COLUMN_CREATED_AT = "created_at"
        private const val COLUMN_UPDATED_AT = "updated_at"

        // Operators Table
        private const val TABLE_OPERATORS = "operators"
        private const val COLUMN_OPERATOR_ID = "operator_id"
        private const val COLUMN_OPERATOR_NAME = "operator_name"
        private const val COLUMN_STATION_ID = "station_id"
        private const val COLUMN_OPERATOR_EMAIL = "operator_email"
        private const val COLUMN_OPERATOR_PASSWORD = "operator_password"
        private const val COLUMN_OPERATOR_IS_ACTIVE = "operator_is_active"

        // Reservations Table
        private const val TABLE_RESERVATIONS = "reservations"
        private const val COLUMN_RESERVATION_ID = "reservation_id"
        private const val COLUMN_OWNER_NIC = "owner_nic"
        private const val COLUMN_STATION_NAME = "station_name"
        private const val COLUMN_RESERVATION_DATE = "reservation_date"
        private const val COLUMN_RESERVATION_TIME = "reservation_time"
        private const val COLUMN_STATUS = "status" // PENDING, APPROVED, CANCELLED, COMPLETED
        private const val COLUMN_QR_CODE = "qr_code"
        private const val COLUMN_RESERVATION_CREATED_AT = "reservation_created_at"
    }

    override fun onCreate(db: SQLiteDatabase) {
        // Create EV Owners table
        val createEVOwnersTable = """
            CREATE TABLE $TABLE_EV_OWNERS (
                $COLUMN_NIC TEXT PRIMARY KEY,
                $COLUMN_FULL_NAME TEXT NOT NULL,
                $COLUMN_EMAIL TEXT UNIQUE NOT NULL,
                $COLUMN_PHONE TEXT NOT NULL,
                $COLUMN_PASSWORD TEXT NOT NULL,
                $COLUMN_IS_ACTIVE INTEGER DEFAULT 1,
                $COLUMN_CREATED_AT DATETIME DEFAULT CURRENT_TIMESTAMP,
                $COLUMN_UPDATED_AT DATETIME DEFAULT CURRENT_TIMESTAMP
            )
        """.trimIndent()

        // Create Operators table
        val createOperatorsTable = """
            CREATE TABLE $TABLE_OPERATORS (
                $COLUMN_OPERATOR_ID TEXT PRIMARY KEY,
                $COLUMN_OPERATOR_NAME TEXT NOT NULL,
                $COLUMN_STATION_ID TEXT NOT NULL,
                $COLUMN_OPERATOR_EMAIL TEXT UNIQUE NOT NULL,
                $COLUMN_OPERATOR_PASSWORD TEXT NOT NULL,
                $COLUMN_OPERATOR_IS_ACTIVE INTEGER DEFAULT 1
            )
        """.trimIndent()

        // Create Reservations table
        val createReservationsTable = """
            CREATE TABLE $TABLE_RESERVATIONS (
                $COLUMN_RESERVATION_ID TEXT PRIMARY KEY,
                $COLUMN_OWNER_NIC TEXT NOT NULL,
                $COLUMN_STATION_NAME TEXT NOT NULL,
                $COLUMN_RESERVATION_DATE TEXT NOT NULL,
                $COLUMN_RESERVATION_TIME TEXT NOT NULL,
                $COLUMN_STATUS TEXT DEFAULT 'PENDING',
                $COLUMN_QR_CODE TEXT,
                $COLUMN_RESERVATION_CREATED_AT DATETIME DEFAULT CURRENT_TIMESTAMP,
                FOREIGN KEY ($COLUMN_OWNER_NIC) REFERENCES $TABLE_EV_OWNERS($COLUMN_NIC)
            )
        """.trimIndent()

        db.execSQL(createEVOwnersTable)
        db.execSQL(createOperatorsTable)
        db.execSQL(createReservationsTable)

        // Insert sample data
        insertSampleData(db)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        Log.d("UserDatabaseHelper", "Upgrading database from version $oldVersion to $newVersion")
        
        when (oldVersion) {
            1 -> {
                try {
                    // Add phone column to ev_owners table
                    db.execSQL("ALTER TABLE $TABLE_EV_OWNERS ADD COLUMN $COLUMN_PHONE TEXT")
                    Log.d("UserDatabaseHelper", "Successfully added phone column")
                } catch (e: Exception) {
                    Log.e("UserDatabaseHelper", "Failed to add phone column: ${e.message}")
                    // If adding column fails, recreate the table
                    recreateTables(db)
                }
            }
            else -> {
                Log.d("UserDatabaseHelper", "Recreating all tables")
                recreateTables(db)
            }
        }
    }
    
    private fun recreateTables(db: SQLiteDatabase) {
        try {
            db.execSQL("DROP TABLE IF EXISTS $TABLE_RESERVATIONS")
            db.execSQL("DROP TABLE IF EXISTS $TABLE_OPERATORS")
            db.execSQL("DROP TABLE IF EXISTS $TABLE_EV_OWNERS")
            onCreate(db)
            Log.d("UserDatabaseHelper", "Successfully recreated all tables")
        } catch (e: Exception) {
            Log.e("UserDatabaseHelper", "Failed to recreate tables: ${e.message}", e)
        }
    }

    private fun insertSampleData(db: SQLiteDatabase) {
        // Insert sample EV owner
        val evOwnerValues = ContentValues().apply {
            put(COLUMN_NIC, "123456789V")
            put(COLUMN_FULL_NAME, "John Doe")
            put(COLUMN_EMAIL, "john.doe@example.com")
            put(COLUMN_PHONE, "+94771234567")
            put(COLUMN_PASSWORD, "password123")
            put(COLUMN_IS_ACTIVE, 1)
        }
        db.insert(TABLE_EV_OWNERS, null, evOwnerValues)

        // Insert sample operator
        val operatorValues = ContentValues().apply {
            put(COLUMN_OPERATOR_ID, "OP001")
            put(COLUMN_OPERATOR_NAME, "Station Operator")
            put(COLUMN_STATION_ID, "ST001")
            put(COLUMN_OPERATOR_EMAIL, "operator@station.com")
            put(COLUMN_OPERATOR_PASSWORD, "operator123")
            put(COLUMN_OPERATOR_IS_ACTIVE, 1)
        }
        db.insert(TABLE_OPERATORS, null, operatorValues)

        // Insert sample reservations
        val reservation1 = ContentValues().apply {
            put(COLUMN_RESERVATION_ID, "RES001")
            put(COLUMN_OWNER_NIC, "123456789V")
            put(COLUMN_STATION_NAME, "Colombo Central")
            put(COLUMN_RESERVATION_DATE, "25/12/2024")
            put(COLUMN_RESERVATION_TIME, "10:00 - 11:00")
            put(COLUMN_STATUS, "PENDING")
        }
        db.insert(TABLE_RESERVATIONS, null, reservation1)

        val reservation2 = ContentValues().apply {
            put(COLUMN_RESERVATION_ID, "RES002")
            put(COLUMN_OWNER_NIC, "123456789V")
            put(COLUMN_STATION_NAME, "Kandy City")
            put(COLUMN_RESERVATION_DATE, "26/12/2024")
            put(COLUMN_RESERVATION_TIME, "14:00 - 15:00")
            put(COLUMN_STATUS, "APPROVED")
            put(COLUMN_QR_CODE, "QR_CODE_12345")
        }
        db.insert(TABLE_RESERVATIONS, null, reservation2)
    }

    // EV Owner methods
    fun createEVOwner(nic: String, fullName: String, email: String, phone: String, password: String): Boolean {
        val db = writableDatabase
        
        try {
            // Check if phone column exists
            if (!columnExists(db, TABLE_EV_OWNERS, COLUMN_PHONE)) {
                Log.e("UserDatabaseHelper", "Phone column does not exist, adding it...")
                db.execSQL("ALTER TABLE $TABLE_EV_OWNERS ADD COLUMN $COLUMN_PHONE TEXT")
            }
            
            val values = ContentValues().apply {
                put(COLUMN_NIC, nic)
                put(COLUMN_FULL_NAME, fullName)
                put(COLUMN_EMAIL, email)
                put(COLUMN_PHONE, phone)
                put(COLUMN_PASSWORD, password)
                put(COLUMN_IS_ACTIVE, 1)
            }
            
            Log.d("UserDatabaseHelper", "Creating EV Owner with data: NIC=$nic, Name=$fullName, Email=$email, Phone=$phone")
            val result = db.insert(TABLE_EV_OWNERS, null, values)
            val success = result != -1L
            Log.d("UserDatabaseHelper", "Insert result: $result, Success: $success")
            
            if (!success) {
                Log.e("UserDatabaseHelper", "Insert failed with result: $result")
            }
            
            return success
        } catch (e: Exception) {
            Log.e("UserDatabaseHelper", "Exception during createEVOwner: ${e.message}", e)
            return false
        }
    }
    
    private fun columnExists(db: SQLiteDatabase, tableName: String, columnName: String): Boolean {
        val cursor = db.rawQuery("PRAGMA table_info($tableName)", null)
        var exists = false
        
        while (cursor.moveToNext()) {
            val name = cursor.getString(cursor.getColumnIndexOrThrow("name"))
            if (name == columnName) {
                exists = true
                break
            }
        }
        cursor.close()
        return exists
    }
    
    fun checkTableSchema(): String {
        val db = readableDatabase
        val cursor = db.rawQuery("PRAGMA table_info($TABLE_EV_OWNERS)", null)
        val columns = mutableListOf<String>()
        
        while (cursor.moveToNext()) {
            val columnName = cursor.getString(cursor.getColumnIndexOrThrow("name"))
            val columnType = cursor.getString(cursor.getColumnIndexOrThrow("type"))
            columns.add("$columnName: $columnType")
        }
        cursor.close()
        
        val schema = columns.joinToString(", ")
        Log.d("UserDatabaseHelper", "Table schema: $schema")
        return schema
    }
    
    fun forceRecreateDatabase(): Boolean {
        return try {
            val db = writableDatabase
            recreateTables(db)
            true
        } catch (e: Exception) {
            Log.e("UserDatabaseHelper", "Failed to force recreate database: ${e.message}", e)
            false
        }
    }
    
    fun resetDatabase(): Boolean {
        return try {
            val db = writableDatabase
            db.execSQL("DROP TABLE IF EXISTS $TABLE_RESERVATIONS")
            db.execSQL("DROP TABLE IF EXISTS $TABLE_OPERATORS")
            db.execSQL("DROP TABLE IF EXISTS $TABLE_EV_OWNERS")
            onCreate(db)
            Log.d("UserDatabaseHelper", "Database completely reset")
            true
        } catch (e: Exception) {
            Log.e("UserDatabaseHelper", "Failed to reset database: ${e.message}", e)
            false
        }
    }
    
    fun getDatabaseInfo(): String {
        val db = readableDatabase
        val path = db.path
        val version = db.version
        val isOpen = db.isOpen
        Log.d("UserDatabaseHelper", "Database path: $path, Version: $version, IsOpen: $isOpen")
        return "Path: $path, Version: $version, IsOpen: $isOpen"
    }
    
    fun testDatabaseConnection(): Boolean {
        return try {
            val db = readableDatabase
            val cursor = db.rawQuery("SELECT 1", null)
            val result = cursor.moveToFirst()
            cursor.close()
            Log.d("UserDatabaseHelper", "Database connection test: $result")
            result
        } catch (e: Exception) {
            Log.e("UserDatabaseHelper", "Database connection test failed: ${e.message}", e)
            false
        }
    }
    
    fun checkDatabaseFile(): String {
        return try {
            val db = readableDatabase
            val file = java.io.File(db.path)
            val exists = file.exists()
            val readable = file.canRead()
            val writable = file.canWrite()
            val size = if (exists) file.length() else 0
            
            val info = "File exists: $exists, Readable: $readable, Writable: $writable, Size: $size bytes, Path: ${db.path}"
            Log.d("UserDatabaseHelper", "Database file info: $info")
            info
        } catch (e: Exception) {
            val error = "Error checking database file: ${e.message}"
            Log.e("UserDatabaseHelper", error, e)
            error
        }
    }

    fun authenticateEVOwner(nicOrEmail: String, password: String): Boolean {
        val db = readableDatabase
        val cursor = db.query(
            TABLE_EV_OWNERS,
            arrayOf(COLUMN_NIC),
            "($COLUMN_NIC = ? OR $COLUMN_EMAIL = ?) AND $COLUMN_PASSWORD = ? AND $COLUMN_IS_ACTIVE = 1",
            arrayOf(nicOrEmail, nicOrEmail, password),
            null, null, null
        )
        val result = cursor.count > 0
        cursor.close()
        return result
    }

    fun updateEVOwner(nic: String, fullName: String, email: String): Boolean {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_FULL_NAME, fullName)
            put(COLUMN_EMAIL, email)
            put(COLUMN_UPDATED_AT, System.currentTimeMillis())
        }
        return db.update(TABLE_EV_OWNERS, values, "$COLUMN_NIC = ?", arrayOf(nic)) > 0
    }

    fun deactivateEVOwner(nic: String): Boolean {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_IS_ACTIVE, 0)
            put(COLUMN_UPDATED_AT, System.currentTimeMillis())
        }
        return db.update(TABLE_EV_OWNERS, values, "$COLUMN_NIC = ?", arrayOf(nic)) > 0
    }

    fun reactivateEVOwner(nic: String): Boolean {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_IS_ACTIVE, 1)
            put(COLUMN_UPDATED_AT, System.currentTimeMillis())
        }
        return db.update(TABLE_EV_OWNERS, values, "$COLUMN_NIC = ?", arrayOf(nic)) > 0
    }

    fun getEVOwnerByNic(nic: String): Map<String, String>? {
        val db = readableDatabase
        var ownerDetails: MutableMap<String, String>? = null

        val cursor = db.query(
            TABLE_EV_OWNERS,
            arrayOf(
                COLUMN_NIC,
                COLUMN_FULL_NAME,
                COLUMN_EMAIL,
                COLUMN_PHONE,
                COLUMN_IS_ACTIVE,
                COLUMN_CREATED_AT,
                COLUMN_UPDATED_AT
            ),
            "$COLUMN_NIC = ?",
            arrayOf(nic),
            null, null, null
        )

        if (cursor.moveToFirst()) {
            ownerDetails = mutableMapOf(
                "nic" to cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NIC)),
                "full_name" to cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_FULL_NAME)),
                "email" to cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_EMAIL)),
                "phone" to cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PHONE)),
                "is_active" to cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_IS_ACTIVE)).toString(),
                "created_at" to cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CREATED_AT)),
                "updated_at" to cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_UPDATED_AT))
            )
        }

        cursor.close()
        db.close()
        return ownerDetails
    }

    // Operator methods
    fun authenticateOperator(operatorIdOrEmail: String, password: String): Boolean {
        val db = readableDatabase
        val cursor = db.query(
            TABLE_OPERATORS,
            arrayOf(COLUMN_OPERATOR_ID),
            "($COLUMN_OPERATOR_ID = ? OR $COLUMN_OPERATOR_EMAIL = ?) AND $COLUMN_OPERATOR_PASSWORD = ? AND $COLUMN_OPERATOR_IS_ACTIVE = 1",
            arrayOf(operatorIdOrEmail, operatorIdOrEmail, password),
            null, null, null
        )
        val result = cursor.count > 0
        cursor.close()
        return result
    }

    // Reservation methods
    fun getPendingReservationsCount(ownerNic: String): Int {
        val db = readableDatabase
        val cursor = db.query(
            TABLE_RESERVATIONS,
            arrayOf(COLUMN_RESERVATION_ID),
            "$COLUMN_OWNER_NIC = ? AND $COLUMN_STATUS = 'PENDING'",
            arrayOf(ownerNic),
            null, null, null
        )
        val count = cursor.count
        cursor.close()
        return count
    }

    fun getApprovedReservationsCount(ownerNic: String): Int {
        val db = readableDatabase
        val cursor = db.query(
            TABLE_RESERVATIONS,
            arrayOf(COLUMN_RESERVATION_ID),
            "$COLUMN_OWNER_NIC = ? AND $COLUMN_STATUS = 'APPROVED'",
            arrayOf(ownerNic),
            null, null, null
        )
        val count = cursor.count
        cursor.close()
        return count
    }

    fun createReservation(
        reservationId: String,
        ownerNic: String,
        stationName: String,
        date: String,
        time: String
    ): Boolean {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_RESERVATION_ID, reservationId)
            put(COLUMN_OWNER_NIC, ownerNic)
            put(COLUMN_STATION_NAME, stationName)
            put(COLUMN_RESERVATION_DATE, date)
            put(COLUMN_RESERVATION_TIME, time)
            put(COLUMN_STATUS, "PENDING")
        }
        return db.insert(TABLE_RESERVATIONS, null, values) != -1L
    }

    fun approveReservation(reservationId: String, qrCode: String): Boolean {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_STATUS, "APPROVED")
            put(COLUMN_QR_CODE, qrCode)
        }
        return db.update(TABLE_RESERVATIONS, values, "$COLUMN_RESERVATION_ID = ?", arrayOf(reservationId)) > 0
    }

    fun cancelReservation(reservationId: String): Boolean {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_STATUS, "CANCELLED")
        }
        return db.update(TABLE_RESERVATIONS, values, "$COLUMN_RESERVATION_ID = ?", arrayOf(reservationId)) > 0
    }
}
