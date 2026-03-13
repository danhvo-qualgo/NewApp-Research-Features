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
public final class RemoveWhiteListUseCase_Factory implements Factory<RemoveWhiteListUseCase> {
  private final Provider<WhitelistRepository> repoProvider;

  private RemoveWhiteListUseCase_Factory(Provider<WhitelistRepository> repoProvider) {
    this.repoProvider = repoProvider;
  }

  @Override
  public RemoveWhiteListUseCase get() {
    return newInstance(repoProvider.get());
  }

  public static RemoveWhiteListUseCase_Factory create(Provider<WhitelistRepository> repoProvider) {
    return new RemoveWhiteListUseCase_Factory(repoProvider);
  }

  public static RemoveWhiteListUseCase newInstance(WhitelistRepository repo) {
    return new RemoveWhiteListUseCase(repo);
  }
}
