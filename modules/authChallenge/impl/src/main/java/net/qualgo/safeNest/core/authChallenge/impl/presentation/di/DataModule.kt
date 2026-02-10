package net.qualgo.safeNest.core.authChallenge.impl.presentation.di

import net.qualgo.safeNest.core.authChallenge.impl.data.repository.AuthChallengeRepositoryImpl
import net.qualgo.safeNest.core.authChallenge.impl.domain.repository.AuthChallengeRepository
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
    abstract fun authChallengeRepository(impl: AuthChallengeRepositoryImpl): AuthChallengeRepository
}