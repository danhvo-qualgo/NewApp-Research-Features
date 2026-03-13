package com.safeNest.demo.features.callProtection.impl.presentation.di;

import com.safeNest.demo.features.callProtection.impl.data.repository.CallDetectionRepositoryImpl;
import com.safeNest.demo.features.callProtection.impl.domain.repository.CallDetectionRepository;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
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
public final class DataModule_CallDetectionRepositoryFactory implements Factory<CallDetectionRepository> {
  private final DataModule module;

  private final Provider<CallDetectionRepositoryImpl> implProvider;

  private DataModule_CallDetectionRepositoryFactory(DataModule module,
      Provider<CallDetectionRepositoryImpl> implProvider) {
    this.module = module;
    this.implProvider = implProvider;
  }

  @Override
  public CallDetectionRepository get() {
    return callDetectionRepository(module, implProvider.get());
  }

  public static DataModule_CallDetectionRepositoryFactory create(DataModule module,
      Provider<CallDetectionRepositoryImpl> implProvider) {
    return new DataModule_CallDetectionRepositoryFactory(module, implProvider);
  }

  public static CallDetectionRepository callDetectionRepository(DataModule instance,
      CallDetectionRepositoryImpl impl) {
    return Preconditions.checkNotNullFromProvides(instance.callDetectionRepository(impl));
  }
}
