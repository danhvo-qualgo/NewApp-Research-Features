package com.safeNest.demo.features.callProtection.impl.data.di;

import com.safeNest.demo.features.callProtection.impl.data.local.CallDeviceStore;
import com.safeNest.demo.features.callProtection.impl.data.local.CallDeviceStoreImpl;
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
public final class StoreModule_CreateCallDeviceStoreFactory implements Factory<CallDeviceStore> {
  private final Provider<CallDeviceStoreImpl> callDeviceStoreImplProvider;

  private StoreModule_CreateCallDeviceStoreFactory(
      Provider<CallDeviceStoreImpl> callDeviceStoreImplProvider) {
    this.callDeviceStoreImplProvider = callDeviceStoreImplProvider;
  }

  @Override
  public CallDeviceStore get() {
    return createCallDeviceStore(callDeviceStoreImplProvider.get());
  }

  public static StoreModule_CreateCallDeviceStoreFactory create(
      Provider<CallDeviceStoreImpl> callDeviceStoreImplProvider) {
    return new StoreModule_CreateCallDeviceStoreFactory(callDeviceStoreImplProvider);
  }

  public static CallDeviceStore createCallDeviceStore(CallDeviceStoreImpl callDeviceStoreImpl) {
    return Preconditions.checkNotNullFromProvides(StoreModule.INSTANCE.createCallDeviceStore(callDeviceStoreImpl));
  }
}
