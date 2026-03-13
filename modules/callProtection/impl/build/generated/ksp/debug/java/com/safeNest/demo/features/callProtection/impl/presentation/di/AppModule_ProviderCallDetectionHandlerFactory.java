package com.safeNest.demo.features.callProtection.impl.presentation.di;

import com.safeNest.demo.features.callProtection.impl.presentation.service.handler.CallDetectionHandler;
import com.safeNest.demo.features.callProtection.impl.presentation.service.handler.CallDetectionHandlerImpl;
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
public final class AppModule_ProviderCallDetectionHandlerFactory implements Factory<CallDetectionHandler> {
  private final AppModule module;

  private final Provider<CallDetectionHandlerImpl> implProvider;

  private AppModule_ProviderCallDetectionHandlerFactory(AppModule module,
      Provider<CallDetectionHandlerImpl> implProvider) {
    this.module = module;
    this.implProvider = implProvider;
  }

  @Override
  public CallDetectionHandler get() {
    return providerCallDetectionHandler(module, implProvider.get());
  }

  public static AppModule_ProviderCallDetectionHandlerFactory create(AppModule module,
      Provider<CallDetectionHandlerImpl> implProvider) {
    return new AppModule_ProviderCallDetectionHandlerFactory(module, implProvider);
  }

  public static CallDetectionHandler providerCallDetectionHandler(AppModule instance,
      CallDetectionHandlerImpl impl) {
    return Preconditions.checkNotNullFromProvides(instance.providerCallDetectionHandler(impl));
  }
}
