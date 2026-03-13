package com.safeNest.demo.features.callProtection.impl.presentation.di;

import com.safeNest.demo.features.callProtection.impl.data.repository.BlacklistPatternRepositoryImpl;
import com.safeNest.demo.features.callProtection.impl.domain.repository.BlacklistPatternRepository;
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
public final class DataModule_BlacklistPatternRepositoryFactory implements Factory<BlacklistPatternRepository> {
  private final DataModule module;

  private final Provider<BlacklistPatternRepositoryImpl> implProvider;

  private DataModule_BlacklistPatternRepositoryFactory(DataModule module,
      Provider<BlacklistPatternRepositoryImpl> implProvider) {
    this.module = module;
    this.implProvider = implProvider;
  }

  @Override
  public BlacklistPatternRepository get() {
    return blacklistPatternRepository(module, implProvider.get());
  }

  public static DataModule_BlacklistPatternRepositoryFactory create(DataModule module,
      Provider<BlacklistPatternRepositoryImpl> implProvider) {
    return new DataModule_BlacklistPatternRepositoryFactory(module, implProvider);
  }

  public static BlacklistPatternRepository blacklistPatternRepository(DataModule instance,
      BlacklistPatternRepositoryImpl impl) {
    return Preconditions.checkNotNullFromProvides(instance.blacklistPatternRepository(impl));
  }
}
