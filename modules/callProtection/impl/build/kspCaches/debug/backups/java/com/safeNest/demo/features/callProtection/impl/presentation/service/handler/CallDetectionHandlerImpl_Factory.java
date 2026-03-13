package com.safeNest.demo.features.callProtection.impl.presentation.service.handler;

import com.safeNest.demo.features.callProtection.impl.domain.usecase.EnableBlackListUseCase;
import com.safeNest.demo.features.callProtection.impl.domain.usecase.EnableWhiteListUseCase;
import com.safeNest.demo.features.callProtection.impl.domain.usecase.GetBlacklistPatternsUseCase;
import com.safeNest.demo.features.callProtection.impl.domain.usecase.GetPhoneNumberUseCase;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
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
public final class CallDetectionHandlerImpl_Factory implements Factory<CallDetectionHandlerImpl> {
  private final Provider<GetPhoneNumberUseCase> getPhoneNumberUseCaseProvider;

  private final Provider<GetBlacklistPatternsUseCase> getBlacklistPatternsUseCaseProvider;

  private final Provider<EnableBlackListUseCase> enableBlackListUseCaseProvider;

  private final Provider<EnableWhiteListUseCase> enableWhiteListUseCaseProvider;

  private CallDetectionHandlerImpl_Factory(
      Provider<GetPhoneNumberUseCase> getPhoneNumberUseCaseProvider,
      Provider<GetBlacklistPatternsUseCase> getBlacklistPatternsUseCaseProvider,
      Provider<EnableBlackListUseCase> enableBlackListUseCaseProvider,
      Provider<EnableWhiteListUseCase> enableWhiteListUseCaseProvider) {
    this.getPhoneNumberUseCaseProvider = getPhoneNumberUseCaseProvider;
    this.getBlacklistPatternsUseCaseProvider = getBlacklistPatternsUseCaseProvider;
    this.enableBlackListUseCaseProvider = enableBlackListUseCaseProvider;
    this.enableWhiteListUseCaseProvider = enableWhiteListUseCaseProvider;
  }

  @Override
  public CallDetectionHandlerImpl get() {
    return newInstance(getPhoneNumberUseCaseProvider.get(), getBlacklistPatternsUseCaseProvider.get(), enableBlackListUseCaseProvider.get(), enableWhiteListUseCaseProvider.get());
  }

  public static CallDetectionHandlerImpl_Factory create(
      Provider<GetPhoneNumberUseCase> getPhoneNumberUseCaseProvider,
      Provider<GetBlacklistPatternsUseCase> getBlacklistPatternsUseCaseProvider,
      Provider<EnableBlackListUseCase> enableBlackListUseCaseProvider,
      Provider<EnableWhiteListUseCase> enableWhiteListUseCaseProvider) {
    return new CallDetectionHandlerImpl_Factory(getPhoneNumberUseCaseProvider, getBlacklistPatternsUseCaseProvider, enableBlackListUseCaseProvider, enableWhiteListUseCaseProvider);
  }

  public static CallDetectionHandlerImpl newInstance(GetPhoneNumberUseCase getPhoneNumberUseCase,
      GetBlacklistPatternsUseCase getBlacklistPatternsUseCase,
      EnableBlackListUseCase enableBlackListUseCase,
      EnableWhiteListUseCase enableWhiteListUseCase) {
    return new CallDetectionHandlerImpl(getPhoneNumberUseCase, getBlacklistPatternsUseCase, enableBlackListUseCase, enableWhiteListUseCase);
  }
}
