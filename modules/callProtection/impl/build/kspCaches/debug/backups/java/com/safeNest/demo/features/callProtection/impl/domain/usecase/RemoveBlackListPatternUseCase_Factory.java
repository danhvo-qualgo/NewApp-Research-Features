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
public final class RemoveBlackListPatternUseCase_Factory implements Factory<RemoveBlackListPatternUseCase> {
  private final Provider<BlacklistPatternRepository> repoProvider;

  private RemoveBlackListPatternUseCase_Factory(Provider<BlacklistPatternRepository> repoProvider) {
    this.repoProvider = repoProvider;
  }

  @Override
  public RemoveBlackListPatternUseCase get() {
    return newInstance(repoProvider.get());
  }

  public static RemoveBlackListPatternUseCase_Factory create(
      Provider<BlacklistPatternRepository> repoProvider) {
    return new RemoveBlackListPatternUseCase_Factory(repoProvider);
  }

  public static RemoveBlackListPatternUseCase newInstance(BlacklistPatternRepository repo) {
    return new RemoveBlackListPatternUseCase(repo);
  }
}
