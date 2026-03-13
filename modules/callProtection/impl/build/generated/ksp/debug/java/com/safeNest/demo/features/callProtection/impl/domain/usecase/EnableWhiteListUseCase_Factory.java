package com.safeNest.demo.features.callProtection.impl.domain.usecase;

import com.safeNest.demo.features.callProtection.impl.domain.repository.WhitelistRepository;
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
public final class EnableWhiteListUseCase_Factory implements Factory<EnableWhiteListUseCase> {
  private final Provider<WhitelistRepository> repoProvider;

  private EnableWhiteListUseCase_Factory(Provider<WhitelistRepository> repoProvider) {
    this.repoProvider = repoProvider;
  }

  @Override
  public EnableWhiteListUseCase get() {
    return newInstance(repoProvider.get());
  }

  public static EnableWhiteListUseCase_Factory create(Provider<WhitelistRepository> repoProvider) {
    return new EnableWhiteListUseCase_Factory(repoProvider);
  }

  public static EnableWhiteListUseCase newInstance(WhitelistRepository repo) {
    return new EnableWhiteListUseCase(repo);
  }
}
