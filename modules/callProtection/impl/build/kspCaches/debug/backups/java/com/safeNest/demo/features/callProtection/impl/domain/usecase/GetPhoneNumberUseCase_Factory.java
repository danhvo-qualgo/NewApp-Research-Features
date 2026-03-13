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
public final class GetPhoneNumberUseCase_Factory implements Factory<GetPhoneNumberUseCase> {
  private final Provider<WhitelistRepository> repoProvider;

  private GetPhoneNumberUseCase_Factory(Provider<WhitelistRepository> repoProvider) {
    this.repoProvider = repoProvider;
  }

  @Override
  public GetPhoneNumberUseCase get() {
    return newInstance(repoProvider.get());
  }

  public static GetPhoneNumberUseCase_Factory create(Provider<WhitelistRepository> repoProvider) {
    return new GetPhoneNumberUseCase_Factory(repoProvider);
  }

  public static GetPhoneNumberUseCase newInstance(WhitelistRepository repo) {
    return new GetPhoneNumberUseCase(repo);
  }
}
