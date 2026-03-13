package com.safeNest.demo.features.callProtection.impl.data.repository;

import com.safeNest.demo.features.callProtection.impl.data.local.BlacklistPatternDao;
import com.safeNest.demo.features.callProtection.impl.data.local.CallDeviceStore;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Provider;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;

@ScopeMetadata
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
public final class BlacklistPatternRepositoryImpl_Factory implements Factory<BlacklistPatternRepositoryImpl> {
  private final Provider<BlacklistPatternDao> daoProvider;

  private final Provider<CallDeviceStore> storeProvider;

  private BlacklistPatternRepositoryImpl_Factory(Provider<BlacklistPatternDao> daoProvider,
      Provider<CallDeviceStore> storeProvider) {
    this.daoProvider = daoProvider;
    this.storeProvider = storeProvider;
  }

  @Override
  public BlacklistPatternRepositoryImpl get() {
    return newInstance(daoProvider.get(), storeProvider.get());
  }

  public static BlacklistPatternRepositoryImpl_Factory create(
      Provider<BlacklistPatternDao> daoProvider, Provider<CallDeviceStore> storeProvider) {
    return new BlacklistPatternRepositoryImpl_Factory(daoProvider, storeProvider);
  }

  public static BlacklistPatternRepositoryImpl newInstance(BlacklistPatternDao dao,
      CallDeviceStore store) {
    return new BlacklistPatternRepositoryImpl(dao, store);
  }
}
