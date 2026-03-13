package com.safeNest.demo.features.callProtection.impl.presentation.di;

import com.safeNest.demo.features.callProtection.impl.presentation.router.CallDetectionRouter;
import com.uney.core.router.Router;
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
public final class AppModule_ProviderCallDetectionRouterFactory implements Factory<Router> {
  private final AppModule module;

  private final Provider<CallDetectionRouter> implProvider;

  private AppModule_ProviderCallDetectionRouterFactory(AppModule module,
      Provider<CallDetectionRouter> implProvider) {
    this.module = module;
    this.implProvider = implProvider;
  }

  @Override
  public Router get() {
    return providerCallDetectionRouter(module, implProvider.get());
  }

  public static AppModule_ProviderCallDetectionRouterFactory create(AppModule module,
      Provider<CallDetectionRouter> implProvider) {
    return new AppModule_ProviderCallDetectionRouterFactory(module, implProvider);
  }

  public static Router providerCallDetectionRouter(AppModule instance, CallDetectionRouter impl) {
    return Preconditions.checkNotNullFromProvides(instance.providerCallDetectionRouter(impl));
  }
}
