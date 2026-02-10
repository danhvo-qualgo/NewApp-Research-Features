package net.qualgo.safeNest.core.signIn.impl.presentation.di

import net.qualgo.safeNest.core.signIn.impl.data.repository.SignInRepositoryImpl
import net.qualgo.safeNest.core.signIn.impl.domain.repository.SignInRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityRetainedComponent
import dagger.hilt.android.scopes.ActivityRetainedScoped

@Module
@InstallIn(ActivityRetainedComponent::class)
internal abstract class DataModule {

    @Binds
    @ActivityRetainedScoped
    abstract fun signInRepository(impl: SignInRepositoryImpl): SignInRepository
}