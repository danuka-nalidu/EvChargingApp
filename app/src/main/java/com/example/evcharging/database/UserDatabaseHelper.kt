package com.example.evcharging.database

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class UserDatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "EVChargingDB"
        private const val DATABASE_VERSION = 1

        // EV Owners Table
        private const val TABLE_EV_OWNERS = "ev_owners"
        private const val COLUMN_NIC = "nic"
        private const val COLUMN_FULL_NAME = "full_name"
        private const val COLUMN_EMAIL = "email"
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
        db.execSQL("DROP TABLE IF EXISTS $TABLE_RESERVATIONS")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_OPERATORS")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_EV_OWNERS")
        onCreate(db)
    }

    private fun insertSampleData(db: SQLiteDatabase) {
        // Insert sample EV owner
        val evOwnerValues = ContentValues().apply {
            put(COLUMN_NIC, "123456789V")
            put(COLUMN_FULL_NAME, "John Doe")
            put(COLUMN_EMAIL, "john.doe@example.com")
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
    fun createEVOwner(nic: String, fullName: String, email: String, password: String): Boolean {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_NIC, nic)
            put(COLUMN_FULL_NAME, fullName)
            put(COLUMN_EMAIL, email)
            put(COLUMN_PASSWORD, password)
            put(COLUMN_IS_ACTIVE, 1)
        }
        return db.insert(TABLE_EV_OWNERS, null, values) != -1L
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
