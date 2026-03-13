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
public final class MyCallRedirectionService_MembersInjector implements MembersInjector<MyCallRedirectionService> {
  private final Provider<CallDetectionHandler> callDetectionHandlerProvider;

  private MyCallRedirectionService_MembersInjector(
      Provider<CallDetectionHandler> callDetectionHandlerProvider) {
    this.callDetectionHandlerProvider = callDetectionHandlerProvider;
  }

  @Override
  public void injectMembers(MyCallRedirectionService instance) {
    injectCallDetectionHandler(instance, callDetectionHandlerProvider.get());
  }

  public static MembersInjector<MyCallRedirectionService> create(
      Provider<CallDetectionHandler> callDetectionHandlerProvider) {
    return new MyCallRedirectionService_MembersInjector(callDetectionHandlerProvider);
  }

  @InjectedFieldSignature("com.safeNest.demo.features.callProtection.impl.presentation.service.call.MyCallRedirectionService.callDetectionHandler")
  public static void injectCallDetectionHandler(MyCallRedirectionService instance,
      CallDetectionHandler callDetectionHandler) {
    instance.callDetectionHandler = callDetectionHandler;
  }
}
