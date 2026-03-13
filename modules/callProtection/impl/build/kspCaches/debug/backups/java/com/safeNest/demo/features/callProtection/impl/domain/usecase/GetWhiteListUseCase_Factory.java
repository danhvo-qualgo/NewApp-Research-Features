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
public final class GetWhiteListUseCase_Factory implements Factory<GetWhiteListUseCase> {
  private final Provider<WhitelistRepository> repoProvider;

  private GetWhiteListUseCase_Factory(Provider<WhitelistRepository> repoProvider) {
    this.repoProvider = repoProvider;
  }

  @Override
  public GetWhiteListUseCase get() {
    return newInstance(repoProvider.get());
  }

  public static GetWhiteListUseCase_Factory create(Provider<WhitelistRepository> repoProvider) {
    return new GetWhiteListUseCase_Factory(repoProvider);
  }

  public static GetWhiteListUseCase newInstance(WhitelistRepository repo) {
    return new GetWhiteListUseCase(repo);
  }
}
