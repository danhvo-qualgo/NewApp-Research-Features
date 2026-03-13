package com.safeNest.demo.features.callProtection.`impl`.`data`.local

import androidx.room.EntityInsertAdapter
import androidx.room.RoomDatabase
import androidx.room.coroutines.createFlow
import androidx.room.util.getColumnIndexOrThrow
import androidx.room.util.performSuspending
import androidx.sqlite.SQLiteStatement
import javax.`annotation`.processing.Generated
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
public class BlacklistPatternDao_Impl(
  __db: RoomDatabase,
) : BlacklistPatternDao {
  private val __db: RoomDatabase

  private val __insertAdapterOfBlacklistPatternEntity: EntityInsertAdapter<BlacklistPatternEntity>
  init {
    this.__db = __db
    this.__insertAdapterOfBlacklistPatternEntity = object : EntityInsertAdapter<BlacklistPatternEntity>() {
      protected override fun createQuery(): String = "INSERT OR REPLACE INTO `blacklist_pattern` (`pattern`,`description`) VALUES (?,?)"

      protected override fun bind(statement: SQLiteStatement, entity: BlacklistPatternEntity) {
        statement.bindText(1, entity.pattern)
        statement.bindText(2, entity.description)
      }
    }
  }

  public override suspend fun insert(entity: BlacklistPatternEntity): Unit = performSuspending(__db, false, true) { _connection ->
    __insertAdapterOfBlacklistPatternEntity.insert(_connection, entity)
  }

  public override fun getAll(): Flow<List<BlacklistPatternEntity>> {
    val _sql: String = "SELECT * FROM blacklist_pattern"
    return createFlow(__db, false, arrayOf("blacklist_pattern")) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        val _columnIndexOfPattern: Int = getColumnIndexOrThrow(_stmt, "pattern")
        val _columnIndexOfDescription: Int = getColumnIndexOrThrow(_stmt, "description")
        val _result: MutableList<BlacklistPatternEntity> = mutableListOf()
        while (_stmt.step()) {
          val _item: BlacklistPatternEntity
          val _tmpPattern: String
          _tmpPattern = _stmt.getText(_columnIndexOfPattern)
          val _tmpDescription: String
          _tmpDescription = _stmt.getText(_columnIndexOfDescription)
          _item = BlacklistPatternEntity(_tmpPattern,_tmpDescription)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override fun `get`(pattern: String): Flow<BlacklistPatternEntity?> {
    val _sql: String = "SELECT * FROM blacklist_pattern WHERE pattern = ?"
    return createFlow(__db, false, arrayOf("blacklist_pattern")) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindText(_argIndex, pattern)
        val _columnIndexOfPattern: Int = getColumnIndexOrThrow(_stmt, "pattern")
        val _columnIndexOfDescription: Int = getColumnIndexOrThrow(_stmt, "description")
        val _result: BlacklistPatternEntity?
        if (_stmt.step()) {
          val _tmpPattern: String
          _tmpPattern = _stmt.getText(_columnIndexOfPattern)
          val _tmpDescription: String
          _tmpDescription = _stmt.getText(_columnIndexOfDescription)
          _result = BlacklistPatternEntity(_tmpPattern,_tmpDescription)
        } else {
          _result = null
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun delete(pattern: String) {
    val _sql: String = "DELETE FROM blacklist_pattern WHERE pattern = ?"
    return performSuspending(__db, false, true) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindText(_argIndex, pattern)
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
