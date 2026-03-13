package com.safeNest.demo.features.callProtection.impl.domain.usecase;

import com.safeNest.demo.features.callProtection.impl.domain.repository.BlacklistPatternRepository;
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
public final class AddBlacklistPatternUseCase_Factory implements Factory<AddBlacklistPatternUseCase> {
  private final Provider<BlacklistPatternRepository> repoProvider;

  private AddBlacklistPatternUseCase_Factory(Provider<BlacklistPatternRepository> repoProvider) {
    this.repoProvider = repoProvider;
  }

  @Override
  public AddBlacklistPatternUseCase get() {
    return newInstance(repoProvider.get());
  }

  public static AddBlacklistPatternUseCase_Factory create(
      Provider<BlacklistPatternRepository> repoProvider) {
    return new AddBlacklistPatternUseCase_Factory(repoProvider);
  }

  public static AddBlacklistPatternUseCase newInstance(BlacklistPatternRepository repo) {
    return new AddBlacklistPatternUseCase(repo);
  }
}
