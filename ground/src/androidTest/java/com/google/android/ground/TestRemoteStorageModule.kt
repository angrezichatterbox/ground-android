/*
 * Copyright 2021 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.google.android.ground

import com.google.android.ground.persistence.remote.RemoteDataStore
import com.google.android.ground.persistence.remote.RemotePersistenceModule
import com.google.android.ground.persistence.remote.RemoteStorageManager
import com.google.android.ground.persistence.uuid.OfflineUuidGenerator
import com.sharedtest.persistence.remote.FakeRemoteDataStore
import com.sharedtest.persistence.remote.FakeRemoteStorageManager
import com.sharedtest.persistence.uuid.FakeUuidGenerator
import dagger.Binds
import dagger.Module
import dagger.hilt.components.SingletonComponent
import dagger.hilt.testing.TestInstallIn
import javax.inject.Singleton

@Module
@TestInstallIn(
  components = [SingletonComponent::class],
  replaces = [RemotePersistenceModule::class],
)
abstract class TestRemoteStorageModule {
  @Binds
  @Singleton
  abstract fun bindRemoteDataStore(remoteDataStore: FakeRemoteDataStore): RemoteDataStore

  @Binds
  @Singleton
  abstract fun bindRemoteStorageManager(
    remoteStorageManager: FakeRemoteStorageManager
  ): RemoteStorageManager

  @Binds
  @Singleton
  abstract fun offlineUuidGenerator(uuidGenerator: FakeUuidGenerator): OfflineUuidGenerator
}
