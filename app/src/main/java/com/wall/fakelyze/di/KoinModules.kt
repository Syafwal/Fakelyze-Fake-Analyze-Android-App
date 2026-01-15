package com.wall.fakelyze.di

import org.koin.core.module.Module

/**
 * List of all Koin modules used in the application
 */
val koinModules = listOf<Module>(
    appModule,
    repositoryModule,
    viewModelModule
)
