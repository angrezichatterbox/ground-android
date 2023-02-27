/*
 * Copyright 2019 Google LLC
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
package com.google.android.ground.persistence.local.room.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import com.google.android.ground.persistence.local.room.entity.SurveyEntity
import com.google.android.ground.persistence.local.room.relations.SurveyEntityAndRelations
import io.reactivex.Flowable
import io.reactivex.Maybe

@Dao
interface SurveyDao : BaseDao<SurveyEntity> {
  @Query("SELECT * FROM survey")
  fun findAllOnceAndStream(): Flowable<List<SurveyEntityAndRelations>>

  @Transaction
  @Query("SELECT * FROM survey WHERE id = :id")
  fun getSurveyById(id: String): Maybe<SurveyEntityAndRelations>

  @Query("SELECT * FROM survey WHERE id = :id")
  suspend fun getSurveyByIdSuspend(id: String): SurveyEntityAndRelations?
}
