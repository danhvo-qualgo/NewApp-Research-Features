package com.safeNest.demo.features.callProtection.impl.presentation.service.call;

import com.safeNest.demo.features.callProtection.impl.presentation.service.handler.CallDetectionHandler;
import dagger.MembersInjector;
import dagger.internal.DaggerGenerated;
import dagger.internal.InjectedFieldSignature;
import dagger.internal.Provider;
import dagger.internal.QualifierMetadata;
import javax.annotation.processing.Generated;

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
public final class MyConnectionService_MembersInjector implements MembersInjector<MyConnectionService> {
  private final Provider<CallDetectionHandler> callDetectionHandlerProvider;

  private MyConnectionService_MembersInjector(
      Provider<CallDetectionHandler> callDetectionHandlerProvider) {
    this.callDetectionHandlerProvider = callDetectionHandlerProvider;
  }

  @Override
  public void injectMembers(MyConnectionService instance) {
    injectCallDetectionHandler(instance, callDetectionHandlerProvider.get());
  }

  public static MembersInjector<MyConnectionService> create(
      Provider<CallDetectionHandler> callDetectionHandlerProvider) {
    return new MyConnectionService_MembersInjector(callDetectionHandlerProvider);
  }

  @InjectedFieldSignature("com.safeNest.demo.features.callProtection.impl.presentation.service.call.MyConnectionService.callDetectionHandler")
  public static void injectCallDetectionHandler(MyConnectionService instance,
      CallDetectionHandler callDetectionHandler) {
    instance.callDetectionHandler = callDetectionHandler;
  }
}
