package com.safeNest.demo.features.callProtection.`impl`.`data`.local

import androidx.room.InvalidationTracker
import androidx.room.RoomOpenDelegate
import androidx.room.migration.AutoMigrationSpec
import androidx.room.migration.Migration
import androidx.room.util.TableInfo
import androidx.room.util.TableInfo.Companion.read
import androidx.room.util.dropFtsSyncTriggers
import androidx.sqlite.SQLiteConnection
import androidx.sqlite.execSQL
import javax.`annotation`.processing.Generated
import kotlin.Lazy
import kotlin.String
import kotlin.Suppress
import kotlin.collections.List
import kotlin.collections.Map
import kotlin.collections.MutableList
import kotlin.collections.MutableMap
import kotlin.collections.MutableSet
import kotlin.collections.Set
import kotlin.collections.mutableListOf
import kotlin.collections.mutableMapOf
import kotlin.collections.mutableSetOf
import kotlin.reflect.KClass

@Generated(value = ["androidx.room.RoomProcessor"])
@Suppress(names = ["UNCHECKED_CAST", "DEPRECATION", "REDUNDANT_PROJECTION", "REMOVAL"])
public class CallDataBase_Impl : CallDataBase() {
  private val _whitelistDao: Lazy<WhitelistDao> = lazy {
    WhitelistDao_Impl(this)
  }

  private val _blacklistPatternDao: Lazy<BlacklistPatternDao> = lazy {
    BlacklistPatternDao_Impl(this)
  }

  protected override fun createOpenDelegate(): RoomOpenDelegate {
    val _openDelegate: RoomOpenDelegate = object : RoomOpenDelegate(2, "efcddfe7ebfcd091e46134a9da0ac339", "e2ba9eb5882b1b0f35ebd6c653249ee9") {
      public override fun createAllTables(connection: SQLiteConnection) {
        connection.execSQL("CREATE TABLE IF NOT EXISTS `whitelist` (`phoneNumber` TEXT NOT NULL, `name` TEXT NOT NULL, `label` TEXT NOT NULL, PRIMARY KEY(`phoneNumber`))")
        connection.execSQL("CREATE TABLE IF NOT EXISTS `blacklist_pattern` (`pattern` TEXT NOT NULL, `description` TEXT NOT NULL, PRIMARY KEY(`pattern`))")
        connection.execSQL("CREATE TABLE IF NOT EXISTS room_master_table (id INTEGER PRIMARY KEY,identity_hash TEXT)")
        connection.execSQL("INSERT OR REPLACE INTO room_master_table (id,identity_hash) VALUES(42, 'efcddfe7ebfcd091e46134a9da0ac339')")
      }

      public override fun dropAllTables(connection: SQLiteConnection) {
        connection.execSQL("DROP TABLE IF EXISTS `whitelist`")
        connection.execSQL("DROP TABLE IF EXISTS `blacklist_pattern`")
      }

      public override fun onCreate(connection: SQLiteConnection) {
      }

      public override fun onOpen(connection: SQLiteConnection) {
        internalInitInvalidationTracker(connection)
      }

      public override fun onPreMigrate(connection: SQLiteConnection) {
        dropFtsSyncTriggers(connection)
      }

      public override fun onPostMigrate(connection: SQLiteConnection) {
      }

      public override fun onValidateSchema(connection: SQLiteConnection): RoomOpenDelegate.ValidationResult {
        val _columnsWhitelist: MutableMap<String, TableInfo.Column> = mutableMapOf()
        _columnsWhitelist.put("phoneNumber", TableInfo.Column("phoneNumber", "TEXT", true, 1, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsWhitelist.put("name", TableInfo.Column("name", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsWhitelist.put("label", TableInfo.Column("label", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        val _foreignKeysWhitelist: MutableSet<TableInfo.ForeignKey> = mutableSetOf()
        val _indicesWhitelist: MutableSet<TableInfo.Index> = mutableSetOf()
        val _infoWhitelist: TableInfo = TableInfo("whitelist", _columnsWhitelist, _foreignKeysWhitelist, _indicesWhitelist)
        val _existingWhitelist: TableInfo = read(connection, "whitelist")
        if (!_infoWhitelist.equals(_existingWhitelist)) {
          return RoomOpenDelegate.ValidationResult(false, """
              |whitelist(com.safeNest.demo.features.callProtection.impl.data.local.WhitelistEntity).
              | Expected:
              |""".trimMargin() + _infoWhitelist + """
              |
              | Found:
              |""".trimMargin() + _existingWhitelist)
        }
        val _columnsBlacklistPattern: MutableMap<String, TableInfo.Column> = mutableMapOf()
        _columnsBlacklistPattern.put("pattern", TableInfo.Column("pattern", "TEXT", true, 1, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsBlacklistPattern.put("description", TableInfo.Column("description", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        val _foreignKeysBlacklistPattern: MutableSet<TableInfo.ForeignKey> = mutableSetOf()
        val _indicesBlacklistPattern: MutableSet<TableInfo.Index> = mutableSetOf()
        val _infoBlacklistPattern: TableInfo = TableInfo("blacklist_pattern", _columnsBlacklistPattern, _foreignKeysBlacklistPattern, _indicesBlacklistPattern)
        val _existingBlacklistPattern: TableInfo = read(connection, "blacklist_pattern")
        if (!_infoBlacklistPattern.equals(_existingBlacklistPattern)) {
          return RoomOpenDelegate.ValidationResult(false, """
              |blacklist_pattern(com.safeNest.demo.features.callProtection.impl.data.local.BlacklistPatternEntity).
              | Expected:
              |""".trimMargin() + _infoBlacklistPattern + """
              |
              | Found:
              |""".trimMargin() + _existingBlacklistPattern)
        }
        return RoomOpenDelegate.ValidationResult(true, null)
      }
    }
    return _openDelegate
  }

  protected override fun createInvalidationTracker(): InvalidationTracker {
    val _shadowTablesMap: MutableMap<String, String> = mutableMapOf()
    val _viewTables: MutableMap<String, Set<String>> = mutableMapOf()
    return InvalidationTracker(this, _shadowTablesMap, _viewTables, "whitelist", "blacklist_pattern")
  }

  public override fun clearAllTables() {
    super.performClear(false, "whitelist", "blacklist_pattern")
  }

  protected override fun getRequiredTypeConverterClasses(): Map<KClass<*>, List<KClass<*>>> {
    val _typeConvertersMap: MutableMap<KClass<*>, List<KClass<*>>> = mutableMapOf()
    _typeConvertersMap.put(WhitelistDao::class, WhitelistDao_Impl.getRequiredConverters())
    _typeConvertersMap.put(BlacklistPatternDao::class, BlacklistPatternDao_Impl.getRequiredConverters())
    return _typeConvertersMap
  }

  public override fun getRequiredAutoMigrationSpecClasses(): Set<KClass<out AutoMigrationSpec>> {
    val _autoMigrationSpecsSet: MutableSet<KClass<out AutoMigrationSpec>> = mutableSetOf()
    return _autoMigrationSpecsSet
  }

  public override fun createAutoMigrations(autoMigrationSpecs: Map<KClass<out AutoMigrationSpec>, AutoMigrationSpec>): List<Migration> {
    val _autoMigrations: MutableList<Migration> = mutableListOf()
    return _autoMigrations
  }

  public override fun whitelistDao(): WhitelistDao = _whitelistDao.value

  public override fun blacklistPatternDao(): BlacklistPatternDao = _blacklistPatternDao.value
}
