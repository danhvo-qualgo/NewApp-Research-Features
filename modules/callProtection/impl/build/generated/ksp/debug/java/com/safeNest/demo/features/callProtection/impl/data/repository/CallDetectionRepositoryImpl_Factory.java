package com.safeNest.demo.features.callProtection.impl.data.repository;

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
public final class CallDetectionRepositoryImpl_Factory implements Factory<CallDetectionRepositoryImpl> {
  @Override
  public CallDetectionRepositoryImpl get() {
    return newInstance();
  }

  public static CallDetectionRepositoryImpl_Factory create() {
    return InstanceHolder.INSTANCE;
  }

  public static CallDetectionRepositoryImpl newInstance() {
    return new CallDetectionRepositoryImpl();
  }

  private static final class InstanceHolder {
    static final CallDetectionRepositoryImpl_Factory INSTANCE = new CallDetectionRepositoryImpl_Factory();
  }
}
