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
public final class CallDetectionService_MembersInjector implements MembersInjector<CallDetectionService> {
  private final Provider<CallDetectionHandler> callDetectionHandlerProvider;

  private CallDetectionService_MembersInjector(
      Provider<CallDetectionHandler> callDetectionHandlerProvider) {
    this.callDetectionHandlerProvider = callDetectionHandlerProvider;
  }

  @Override
  public void injectMembers(CallDetectionService instance) {
    injectCallDetectionHandler(instance, callDetectionHandlerProvider.get());
  }

  public static MembersInjector<CallDetectionService> create(
      Provider<CallDetectionHandler> callDetectionHandlerProvider) {
    return new CallDetectionService_MembersInjector(callDetectionHandlerProvider);
  }

  @InjectedFieldSignature("com.safeNest.demo.features.callProtection.impl.presentation.service.call.CallDetectionService.callDetectionHandler")
  public static void injectCallDetectionHandler(CallDetectionService instance,
      CallDetectionHandler callDetectionHandler) {
    instance.callDetectionHandler = callDetectionHandler;
  }
}
