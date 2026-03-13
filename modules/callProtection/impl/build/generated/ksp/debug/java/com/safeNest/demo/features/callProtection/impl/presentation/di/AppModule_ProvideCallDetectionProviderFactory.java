package com.safeNest.demo.features.callProtection.impl.presentation.di;

import com.safeNest.demo.features.callProtection.api.CallDetectionProvider;
import com.safeNest.demo.features.callProtection.impl.presentation.CallDetectionProviderImpl;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
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
public final class AppModule_ProvideCallDetectionProviderFactory implements Factory<CallDetectionProvider> {
  private final AppModule module;

  private final Provider<CallDetectionProviderImpl> implProvider;

  private AppModule_ProvideCallDetectionProviderFactory(AppModule module,
      Provider<CallDetectionProviderImpl> implProvider) {
    this.module = module;
    this.implProvider = implProvider;
  }

  @Override
  public CallDetectionProvider get() {
    return provideCallDetectionProvider(module, implProvider.get());
  }

  public static AppModule_ProvideCallDetectionProviderFactory create(AppModule module,
      Provider<CallDetectionProviderImpl> implProvider) {
    return new AppModule_ProvideCallDetectionProviderFactory(module, implProvider);
  }

  public static CallDetectionProvider provideCallDetectionProvider(AppModule instance,
      CallDetectionProviderImpl impl) {
    return Preconditions.checkNotNullFromProvides(instance.provideCallDetectionProvider(impl));
  }
}
