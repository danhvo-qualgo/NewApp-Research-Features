package com.safeNest.demo.features.callProtection.impl.data.di;

import com.safeNest.demo.features.callProtection.impl.data.local.BlacklistPatternDao;
import com.safeNest.demo.features.callProtection.impl.data.local.CallDataBase;
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
public final class RoomModule_BlacklistPatternDaoFactory implements Factory<BlacklistPatternDao> {
  private final Provider<CallDataBase> databaseProvider;

  private RoomModule_BlacklistPatternDaoFactory(Provider<CallDataBase> databaseProvider) {
    this.databaseProvider = databaseProvider;
  }

  @Override
  public BlacklistPatternDao get() {
    return blacklistPatternDao(databaseProvider.get());
  }

  public static RoomModule_BlacklistPatternDaoFactory create(
      Provider<CallDataBase> databaseProvider) {
    return new RoomModule_BlacklistPatternDaoFactory(databaseProvider);
  }

  public static BlacklistPatternDao blacklistPatternDao(CallDataBase database) {
    return Preconditions.checkNotNullFromProvides(RoomModule.INSTANCE.blacklistPatternDao(database));
  }
}
