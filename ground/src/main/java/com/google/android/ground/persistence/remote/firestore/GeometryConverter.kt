/*
 * Copyright 2022 Google LLC
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

package com.google.android.ground.persistence.remote.firestore

import com.google.android.ground.persistence.remote.DataStoreException
import com.google.firebase.firestore.GeoPoint
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import org.locationtech.jts.geom.Geometry
import org.locationtech.jts.io.geojson.GeoJsonReader
import org.locationtech.jts.io.geojson.GeoJsonWriter
import kotlin.Result.Companion.failure
import kotlin.Result.Companion.success

/**
 * Converts between Geometry model objects and their equivalent representation in Firestore.
 *
 * Specifically, geometries represented in Firestore as follows:
 *
 * * Geometries are persisted using a modified GeoJSON representation.
 * * The GeoJSON map hierarchy is converted to a Firestore nested map.
 * * Since Firestore does not allow nested arrays, arrays are replaced with nested maps keyed by
 *   the array index.
 * * All coordinates (two-element double arrays) are represented as GeoPoint in Firestore.
 *
 * `Point` and `MultiPolygon` are the only supported `Geometry` types. Behavior for other types is
 * undefined.
 */
object GeometryConverter {
    // Reify fromJson() to create type token from generics.
    private inline fun <reified T> Gson.fromJson(json: String) =
        fromJson<T>(json, object : TypeToken<T>() {}.type)

    /**
     * Convert a `Geometry` to a `Map` which may be used to persist
     * the provided geometry in Firestore.
     */
    fun toFirestoreMap(geometry: Geometry): Result<Map<String, Any>> {
        return try {
            val writer = GeoJsonWriter()
            writer.setEncodeCRS(false)
            val jsonString = writer.write(geometry)
            val jsonMap = Gson().fromJson<MutableMap<String, Any>>(jsonString)
            success(toFirestoreValue(jsonMap))
        } catch (e: Throwable) {
            failure(e)
        }
    }

    private fun toFirestoreValue(value: Map<String, Any>): Map<String, Any> {
        return value.mapValues {
            toFirestoreValue(it.value)
        }
    }

    private fun toFirestoreValue(value: Any): Any {
        return when (value) {
            is ArrayList<*> -> {
                if (value.size == 2 && value.all { it is Double }) {
                    GeoPoint(value[0] as Double, value[1] as Double)
                } else {
                    indexedMap(value)
                }
            }
            is Map<*, *> -> {
                toFirestoreValue(value)
            }
            else -> {
                value
            }
        }
    }

    private fun indexedMap(list: List<Any>): Map<Int, Any> =
        list.mapIndexed { index, value -> index to toFirestoreValue(value) }.toMap()

    /**
     * Converts a `Map` deserialized from Firestore into a `Geometry` instance.
     */
    fun fromFirestoreMap(map: Map<String, *>?): Result<Geometry> {
        return try {
            if (map == null) throw DataStoreException("Null geometry")
            val jsonMap = fromFirestoreValue(map)
            val jsonString = Gson().toJson(jsonMap)
            val reader = GeoJsonReader()
            val geometry = reader.read(jsonString)
            if (geometry.coordinates.isEmpty()) {
                throw DataStoreException("Empty coordinates in $geometry")
            }
            success(geometry)
        } catch (e: Throwable) {
            failure(e)
        }
    }

    private fun fromFirestoreValue(value: Any?): Any {
        if (value == null) {
            throw DataStoreException("null value in geometry")
        }
        return when (value) {
            is Map<*, *> -> {
                fromFirestoreMapValue(value)
            }
            is GeoPoint -> {
                arrayOf(value.latitude, value.longitude)
            }
            else -> {
                value
            }
        }
    }

    @Suppress("UNCHECKED_CAST")
    private fun fromFirestoreMapValue(map: Map<*, *>): Any {
        // If all keys are non-null Ints, assume this refers to an indexed map.
        // If heuristic breaks, we may also want to check keys are in order starting at 0.
        return if (map.entries.all { it.key is Int }) {
            indexedMapToList(map as Map<Int, *>).map(::fromFirestoreValue)
        } else {
            map.mapValues { fromFirestoreValue(it.value) }
        }
    }

    /**
     * Converts map representation used to store nested arrays in Firestore into a List. Assumes
     * keys are consecutive ints starting from 0.
     */
    private fun indexedMapToList(map: Map<Int, *>): List<*> {
        return map.entries.sortedBy { it.key }.map { it.value }
    }
}