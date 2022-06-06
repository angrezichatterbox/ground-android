/*
 * Copyright 2020 Google LLC
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

package com.google.android.gnd.persistence.remote.firestore.schema;

import static com.google.android.gnd.util.Localization.getLocalizedMessage;

import com.google.android.gnd.model.layer.Layer;
import timber.log.Timber;

/**
 * Converts between Firestore documents and {@link Layer} instances.
 */
class LayerConverter {

  static Layer toLayer(String id, LayerNestedObject obj) {
    Layer.Builder layer = Layer.newBuilder();
    layer.setId(id).setName(getLocalizedMessage(obj.getName()));
    if (obj.getForms() != null && !obj.getForms().isEmpty()) {
      if (obj.getForms().size() > 1) {
        Timber.e("Multiple forms not supported");
      }
      String taskId = obj.getForms().keySet().iterator().next();
      layer.setTask(FormConverter.toForm(taskId, obj.getForms().get(taskId)));
    }
    return layer.build();
  }
}
