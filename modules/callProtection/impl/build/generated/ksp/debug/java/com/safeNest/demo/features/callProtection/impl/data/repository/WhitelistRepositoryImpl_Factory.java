package com.safeNest.demo.features.callProtection.impl.data.repository;

import com.safeNest.demo.features.callProtection.impl.data.local.CallDeviceStore;
import com.safeNest.demo.features.callProtection.impl.data.local.WhitelistDao;
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
public final class WhitelistRepositoryImpl_Factory implements Factory<WhitelistRepositoryImpl> {
  private final Provider<WhitelistDao> daoProvider;

  private final Provider<CallDeviceStore> storeProvider;

  private WhitelistRepositoryImpl_Factory(Provider<WhitelistDao> daoProvider,
      Provider<CallDeviceStore> storeProvider) {
    this.daoProvider = daoProvider;
    this.storeProvider = storeProvider;
  }

  @Override
  public WhitelistRepositoryImpl get() {
    return newInstance(daoProvider.get(), storeProvider.get());
  }

  public static WhitelistRepositoryImpl_Factory create(Provider<WhitelistDao> daoProvider,
      Provider<CallDeviceStore> storeProvider) {
    return new WhitelistRepositoryImpl_Factory(daoProvider, storeProvider);
  }

  public static WhitelistRepositoryImpl newInstance(WhitelistDao dao, CallDeviceStore store) {
    return new WhitelistRepositoryImpl(dao, store);
  }
}
