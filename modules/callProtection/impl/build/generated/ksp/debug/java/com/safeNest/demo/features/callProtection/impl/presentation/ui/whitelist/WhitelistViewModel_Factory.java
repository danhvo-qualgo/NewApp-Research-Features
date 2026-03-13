package com.safeNest.demo.features.callProtection.impl.presentation.ui.whitelist;

import com.safeNest.demo.features.callProtection.impl.domain.usecase.AddNumberToWhiteListUseCase;
import com.safeNest.demo.features.callProtection.impl.domain.usecase.EnableWhiteListUseCase;
import com.safeNest.demo.features.callProtection.impl.domain.usecase.GetWhiteListUseCase;
import com.safeNest.demo.features.callProtection.impl.domain.usecase.RemoveWhiteListUseCase;
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
public final class WhitelistViewModel_Factory implements Factory<WhitelistViewModel> {
  private final Provider<AddNumberToWhiteListUseCase> addNumberToWhiteListUseCaseProvider;

  private final Provider<GetWhiteListUseCase> getWhitelistUseCaseProvider;

  private final Provider<RemoveWhiteListUseCase> removeWhiteListUseCaseProvider;

  private final Provider<EnableWhiteListUseCase> enableWhiteListUseCaseProvider;

  private WhitelistViewModel_Factory(
      Provider<AddNumberToWhiteListUseCase> addNumberToWhiteListUseCaseProvider,
      Provider<GetWhiteListUseCase> getWhitelistUseCaseProvider,
      Provider<RemoveWhiteListUseCase> removeWhiteListUseCaseProvider,
      Provider<EnableWhiteListUseCase> enableWhiteListUseCaseProvider) {
    this.addNumberToWhiteListUseCaseProvider = addNumberToWhiteListUseCaseProvider;
    this.getWhitelistUseCaseProvider = getWhitelistUseCaseProvider;
    this.removeWhiteListUseCaseProvider = removeWhiteListUseCaseProvider;
    this.enableWhiteListUseCaseProvider = enableWhiteListUseCaseProvider;
  }

  @Override
  public WhitelistViewModel get() {
    return newInstance(addNumberToWhiteListUseCaseProvider.get(), getWhitelistUseCaseProvider.get(), removeWhiteListUseCaseProvider.get(), enableWhiteListUseCaseProvider.get());
  }

  public static WhitelistViewModel_Factory create(
      Provider<AddNumberToWhiteListUseCase> addNumberToWhiteListUseCaseProvider,
      Provider<GetWhiteListUseCase> getWhitelistUseCaseProvider,
      Provider<RemoveWhiteListUseCase> removeWhiteListUseCaseProvider,
      Provider<EnableWhiteListUseCase> enableWhiteListUseCaseProvider) {
    return new WhitelistViewModel_Factory(addNumberToWhiteListUseCaseProvider, getWhitelistUseCaseProvider, removeWhiteListUseCaseProvider, enableWhiteListUseCaseProvider);
  }

  public static WhitelistViewModel newInstance(
      AddNumberToWhiteListUseCase addNumberToWhiteListUseCase,
      GetWhiteListUseCase getWhitelistUseCase, RemoveWhiteListUseCase removeWhiteListUseCase,
      EnableWhiteListUseCase enableWhiteListUseCase) {
    return new WhitelistViewModel(addNumberToWhiteListUseCase, getWhitelistUseCase, removeWhiteListUseCase, enableWhiteListUseCase);
  }
}
