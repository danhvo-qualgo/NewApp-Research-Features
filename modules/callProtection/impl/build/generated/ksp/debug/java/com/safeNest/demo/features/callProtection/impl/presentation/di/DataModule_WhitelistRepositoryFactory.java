package com.safeNest.demo.features.callProtection.impl.presentation.di;

import com.safeNest.demo.features.callProtection.impl.data.repository.WhitelistRepositoryImpl;
import com.safeNest.demo.features.callProtection.impl.domain.repository.WhitelistRepository;
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
public final class DataModule_WhitelistRepositoryFactory implements Factory<WhitelistRepository> {
  private final DataModule module;

  private final Provider<WhitelistRepositoryImpl> implProvider;

  private DataModule_WhitelistRepositoryFactory(DataModule module,
      Provider<WhitelistRepositoryImpl> implProvider) {
    this.module = module;
    this.implProvider = implProvider;
  }

  @Override
  public WhitelistRepository get() {
    return whitelistRepository(module, implProvider.get());
  }

  public static DataModule_WhitelistRepositoryFactory create(DataModule module,
      Provider<WhitelistRepositoryImpl> implProvider) {
    return new DataModule_WhitelistRepositoryFactory(module, implProvider);
  }

  public static WhitelistRepository whitelistRepository(DataModule instance,
      WhitelistRepositoryImpl impl) {
    return Preconditions.checkNotNullFromProvides(instance.whitelistRepository(impl));
  }
}
