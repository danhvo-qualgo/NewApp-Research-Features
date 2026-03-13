package com.safeNest.demo.features.callProtection.impl.presentation.ui.blacklist;

import com.safeNest.demo.features.callProtection.impl.domain.usecase.AddBlacklistPatternUseCase;
import com.safeNest.demo.features.callProtection.impl.domain.usecase.EnableBlackListUseCase;
import com.safeNest.demo.features.callProtection.impl.domain.usecase.GetBlacklistPatternsUseCase;
import com.safeNest.demo.features.callProtection.impl.domain.usecase.RemoveBlackListPatternUseCase;
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
public final class BlocklistViewModel_Factory implements Factory<BlocklistViewModel> {
  private final Provider<AddBlacklistPatternUseCase> addBlacklistPatternUseCaseProvider;

  private final Provider<GetBlacklistPatternsUseCase> getBlacklistPatternsUseCaseProvider;

  private final Provider<RemoveBlackListPatternUseCase> removeBlackListPatternUseCaseProvider;

  private final Provider<EnableBlackListUseCase> enableBlackListUseCaseProvider;

  private BlocklistViewModel_Factory(
      Provider<AddBlacklistPatternUseCase> addBlacklistPatternUseCaseProvider,
      Provider<GetBlacklistPatternsUseCase> getBlacklistPatternsUseCaseProvider,
      Provider<RemoveBlackListPatternUseCase> removeBlackListPatternUseCaseProvider,
      Provider<EnableBlackListUseCase> enableBlackListUseCaseProvider) {
    this.addBlacklistPatternUseCaseProvider = addBlacklistPatternUseCaseProvider;
    this.getBlacklistPatternsUseCaseProvider = getBlacklistPatternsUseCaseProvider;
    this.removeBlackListPatternUseCaseProvider = removeBlackListPatternUseCaseProvider;
    this.enableBlackListUseCaseProvider = enableBlackListUseCaseProvider;
  }

  @Override
  public BlocklistViewModel get() {
    return newInstance(addBlacklistPatternUseCaseProvider.get(), getBlacklistPatternsUseCaseProvider.get(), removeBlackListPatternUseCaseProvider.get(), enableBlackListUseCaseProvider.get());
  }

  public static BlocklistViewModel_Factory create(
      Provider<AddBlacklistPatternUseCase> addBlacklistPatternUseCaseProvider,
      Provider<GetBlacklistPatternsUseCase> getBlacklistPatternsUseCaseProvider,
      Provider<RemoveBlackListPatternUseCase> removeBlackListPatternUseCaseProvider,
      Provider<EnableBlackListUseCase> enableBlackListUseCaseProvider) {
    return new BlocklistViewModel_Factory(addBlacklistPatternUseCaseProvider, getBlacklistPatternsUseCaseProvider, removeBlackListPatternUseCaseProvider, enableBlackListUseCaseProvider);
  }

  public static BlocklistViewModel newInstance(
      AddBlacklistPatternUseCase addBlacklistPatternUseCase,
      GetBlacklistPatternsUseCase getBlacklistPatternsUseCase,
      RemoveBlackListPatternUseCase removeBlackListPatternUseCase,
      EnableBlackListUseCase enableBlackListUseCase) {
    return new BlocklistViewModel(addBlacklistPatternUseCase, getBlacklistPatternsUseCase, removeBlackListPatternUseCase, enableBlackListUseCase);
  }
}
