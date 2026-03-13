package com.safeNest.demo.features.callProtection.impl.presentation.router;

import android.content.Context;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Provider;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;

@ScopeMetadata
@QualifierMetadata("dagger.hilt.android.qualifiers.ApplicationContext")
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
public final class CallDetectionRouter_Factory implements Factory<CallDetectionRouter> {
  private final Provider<Context> contextProvider;

  private CallDetectionRouter_Factory(Provider<Context> contextProvider) {
    this.contextProvider = contextProvider;
  }

  @Override
  public CallDetectionRouter get() {
    return newInstance(contextProvider.get());
  }

  public static CallDetectionRouter_Factory create(Provider<Context> contextProvider) {
    return new CallDetectionRouter_Factory(contextProvider);
  }

  public static CallDetectionRouter newInstance(Context context) {
    return new CallDetectionRouter(context);
  }
}
