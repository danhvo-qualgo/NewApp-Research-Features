package com.safeNest.demo.features.callProtection.impl.data.di;

import com.uney.core.storage.api.DeviceStorage;
import com.uney.core.storage.api.StorageManager;
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
public final class StoreModule_CallDeviceStoreFactory implements Factory<DeviceStorage> {
  private final Provider<StorageManager> storageManagerProvider;

  private StoreModule_CallDeviceStoreFactory(Provider<StorageManager> storageManagerProvider) {
    this.storageManagerProvider = storageManagerProvider;
  }

  @Override
  public DeviceStorage get() {
    return callDeviceStore(storageManagerProvider.get());
  }

  public static StoreModule_CallDeviceStoreFactory create(
      Provider<StorageManager> storageManagerProvider) {
    return new StoreModule_CallDeviceStoreFactory(storageManagerProvider);
  }

  public static DeviceStorage callDeviceStore(StorageManager storageManager) {
    return Preconditions.checkNotNullFromProvides(StoreModule.INSTANCE.callDeviceStore(storageManager));
  }
}
