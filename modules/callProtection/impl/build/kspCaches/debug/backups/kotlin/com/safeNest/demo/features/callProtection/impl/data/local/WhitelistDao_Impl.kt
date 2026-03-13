package com.safeNest.demo.features.callProtection.`impl`.`data`.local

import androidx.room.EntityInsertAdapter
import androidx.room.RoomDatabase
import androidx.room.coroutines.createFlow
import androidx.room.util.getColumnIndexOrThrow
import androidx.room.util.performSuspending
import androidx.sqlite.SQLiteStatement
import javax.`annotation`.processing.Generated
import kotlin.Boolean
import kotlin.Int
import kotlin.String
import kotlin.Suppress
import kotlin.Unit
import kotlin.collections.List
import kotlin.collections.MutableList
import kotlin.collections.mutableListOf
import kotlin.reflect.KClass
import kotlinx.coroutines.flow.Flow

@Generated(value = ["androidx.room.RoomProcessor"])
@Suppress(names = ["UNCHECKED_CAST", "DEPRECATION", "REDUNDANT_PROJECTION", "REMOVAL"])
public class WhitelistDao_Impl(
  __db: RoomDatabase,
) : WhitelistDao {
  private val __db: RoomDatabase

  private val __insertAdapterOfWhitelistEntity: EntityInsertAdapter<WhitelistEntity>
  init {
    this.__db = __db
    this.__insertAdapterOfWhitelistEntity = object : EntityInsertAdapter<WhitelistEntity>() {
      protected override fun createQuery(): String = "INSERT OR REPLACE INTO `whitelist` (`phoneNumber`,`name`,`label`) VALUES (?,?,?)"

      protected override fun bind(statement: SQLiteStatement, entity: WhitelistEntity) {
        statement.bindText(1, entity.phoneNumber)
        statement.bindText(2, entity.name)
        statement.bindText(3, entity.label)
      }
    }
  }

  public override suspend fun insert(entity: WhitelistEntity): Unit = performSuspending(__db, false, true) { _connection ->
    __insertAdapterOfWhitelistEntity.insert(_connection, entity)
  }

  public override fun getAll(): Flow<List<WhitelistEntity>> {
    val _sql: String = "SELECT * FROM whitelist"
    return createFlow(__db, false, arrayOf("whitelist")) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        val _columnIndexOfPhoneNumber: Int = getColumnIndexOrThrow(_stmt, "phoneNumber")
        val _columnIndexOfName: Int = getColumnIndexOrThrow(_stmt, "name")
        val _columnIndexOfLabel: Int = getColumnIndexOrThrow(_stmt, "label")
        val _result: MutableList<WhitelistEntity> = mutableListOf()
        while (_stmt.step()) {
          val _item: WhitelistEntity
          val _tmpPhoneNumber: String
          _tmpPhoneNumber = _stmt.getText(_columnIndexOfPhoneNumber)
          val _tmpName: String
          _tmpName = _stmt.getText(_columnIndexOfName)
          val _tmpLabel: String
          _tmpLabel = _stmt.getText(_columnIndexOfLabel)
          _item = WhitelistEntity(_tmpPhoneNumber,_tmpName,_tmpLabel)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override fun `get`(phoneNumber: String): Flow<WhitelistEntity?> {
    val _sql: String = "SELECT * FROM whitelist WHERE phoneNumber = ?"
    return createFlow(__db, false, arrayOf("whitelist")) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindText(_argIndex, phoneNumber)
        val _columnIndexOfPhoneNumber: Int = getColumnIndexOrThrow(_stmt, "phoneNumber")
        val _columnIndexOfName: Int = getColumnIndexOrThrow(_stmt, "name")
        val _columnIndexOfLabel: Int = getColumnIndexOrThrow(_stmt, "label")
        val _result: WhitelistEntity?
        if (_stmt.step()) {
          val _tmpPhoneNumber: String
          _tmpPhoneNumber = _stmt.getText(_columnIndexOfPhoneNumber)
          val _tmpName: String
          _tmpName = _stmt.getText(_columnIndexOfName)
          val _tmpLabel: String
          _tmpLabel = _stmt.getText(_columnIndexOfLabel)
          _result = WhitelistEntity(_tmpPhoneNumber,_tmpName,_tmpLabel)
        } else {
          _result = null
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun exists(number: String): Boolean {
    val _sql: String = "SELECT EXISTS(SELECT 1 FROM whitelist WHERE phoneNumber = ?)"
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindText(_argIndex, number)
        val _result: Boolean
        if (_stmt.step()) {
          val _tmp: Int
          _tmp = _stmt.getLong(0).toInt()
          _result = _tmp != 0
        } else {
          _result = false
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun delete(number: String) {
    val _sql: String = "DELETE FROM whitelist WHERE phoneNumber = ?"
    return performSuspending(__db, false, true) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindText(_argIndex, number)
        _stmt.step()
      } finally {
        _stmt.close()
      }
    }
  }

  public companion object {
    public fun getRequiredConverters(): List<KClass<*>> = emptyList()
  }
}
