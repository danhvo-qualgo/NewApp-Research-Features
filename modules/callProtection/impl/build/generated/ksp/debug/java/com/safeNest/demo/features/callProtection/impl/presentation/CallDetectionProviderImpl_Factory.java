package com.safeNest.demo.features.callProtection.impl.presentation;

import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
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
public final class CallDetectionProviderImpl_Factory implements Factory<CallDetectionProviderImpl> {
  @Override
  public CallDetectionProviderImpl get() {
    return newInstance();
  }

  public static CallDetectionProviderImpl_Factory create() {
    return InstanceHolder.INSTANCE;
  }

  public static CallDetectionProviderImpl newInstance() {
    return new CallDetectionProviderImpl();
  }

  private static final class InstanceHolder {
    static final CallDetectionProviderImpl_Factory INSTANCE = new CallDetectionProviderImpl_Factory();
  }
}
