package com.safeNest.demo.features.callProtection.impl.data.di;

import android.content.Context;
import com.safeNest.demo.features.callProtection.impl.data.local.CallDataBase;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
import dagger.internal.Provider;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;

@ScopeMetadata("javax.inject.Singleton")
@QualifierMetadata("dagger.hilt.android.qualifiers.ApplicationContext")
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
public final class RoomModule_CallDetectionDatabaseFactory implements Factory<CallDataBase> {
  private final Provider<Context> applicationContextProvider;

  private RoomModule_CallDetectionDatabaseFactory(Provider<Context> applicationContextProvider) {
    this.applicationContextProvider = applicationContextProvider;
  }

  @Override
  public CallDataBase get() {
    return callDetectionDatabase(applicationContextProvider.get());
  }

  public static RoomModule_CallDetectionDatabaseFactory create(
      Provider<Context> applicationContextProvider) {
    return new RoomModule_CallDetectionDatabaseFactory(applicationContextProvider);
  }

  public static CallDataBase callDetectionDatabase(Context applicationContext) {
    return Preconditions.checkNotNullFromProvides(RoomModule.INSTANCE.callDetectionDatabase(applicationContext));
  }
}
