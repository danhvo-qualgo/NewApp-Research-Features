package com.safeNest.demo.features.callProtection.impl.data.di;

import com.uney.core.storage.api.StorageManager;
import com.uney.core.storage.api.UserStorage;
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
public final class StoreModule_CallUserStoreFactory implements Factory<UserStorage> {
  private final Provider<StorageManager> storageManagerProvider;

  private StoreModule_CallUserStoreFactory(Provider<StorageManager> storageManagerProvider) {
    this.storageManagerProvider = storageManagerProvider;
  }

  @Override
  public UserStorage get() {
    return callUserStore(storageManagerProvider.get());
  }

  public static StoreModule_CallUserStoreFactory create(
      Provider<StorageManager> storageManagerProvider) {
    return new StoreModule_CallUserStoreFactory(storageManagerProvider);
  }

  public static UserStorage callUserStore(StorageManager storageManager) {
    return Preconditions.checkNotNullFromProvides(StoreModule.INSTANCE.callUserStore(storageManager));
  }
}
