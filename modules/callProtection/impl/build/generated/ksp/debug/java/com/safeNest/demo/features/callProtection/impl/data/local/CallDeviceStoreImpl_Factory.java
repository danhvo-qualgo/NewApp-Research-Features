package com.safeNest.demo.features.callProtection.impl.data.local;

import com.uney.core.storage.api.DeviceStorage;
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
public final class CallDeviceStoreImpl_Factory implements Factory<CallDeviceStoreImpl> {
  private final Provider<DeviceStorage> deviceStorageProvider;

  private CallDeviceStoreImpl_Factory(Provider<DeviceStorage> deviceStorageProvider) {
    this.deviceStorageProvider = deviceStorageProvider;
  }

  @Override
  public CallDeviceStoreImpl get() {
    return newInstance(deviceStorageProvider.get());
  }

  public static CallDeviceStoreImpl_Factory create(Provider<DeviceStorage> deviceStorageProvider) {
    return new CallDeviceStoreImpl_Factory(deviceStorageProvider);
  }

  public static CallDeviceStoreImpl newInstance(DeviceStorage deviceStorage) {
    return new CallDeviceStoreImpl(deviceStorage);
  }
}
