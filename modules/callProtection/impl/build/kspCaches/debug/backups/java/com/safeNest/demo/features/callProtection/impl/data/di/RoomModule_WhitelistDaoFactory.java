package com.safeNest.demo.features.callProtection.impl.data.di;

import com.safeNest.demo.features.callProtection.impl.data.local.CallDataBase;
import com.safeNest.demo.features.callProtection.impl.data.local.WhitelistDao;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
import dagger.internal.Provider;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;

@ScopeMetadata("javax.inject.Singleton")
@QualifierMetadata
@DaggerGenerated
@Generated(
    value = "dagger.internal.codegen.ComponentProcessor",
    comments = "https://dagger.dev"
)
@SuppressWarnings({
    "unchecked",
    "rawtypes",
    "KotlinInternal",
    "KotlinInternalInJava",
    "cast",
    "deprecation",
    "nullness:initialization.field.uninitialized"
})
public final class RoomModule_WhitelistDaoFactory implements Factory<WhitelistDao> {
  private final Provider<CallDataBase> databaseProvider;

  private RoomModule_WhitelistDaoFactory(Provider<CallDataBase> databaseProvider) {
    this.databaseProvider = databaseProvider;
  }

  @Override
  public WhitelistDao get() {
    return whitelistDao(databaseProvider.get());
  }

  public static RoomModule_WhitelistDaoFactory create(Provider<CallDataBase> databaseProvider) {
    return new RoomModule_WhitelistDaoFactory(databaseProvider);
  }

  public static WhitelistDao whitelistDao(CallDataBase database) {
    return Preconditions.checkNotNullFromProvides(RoomModule.INSTANCE.whitelistDao(database));
  }
}
