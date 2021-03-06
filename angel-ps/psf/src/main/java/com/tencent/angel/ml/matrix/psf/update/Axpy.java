/*
 * Tencent is pleased to support the open source community by making Angel available.
 *
 * Copyright (C) 2017 THL A29 Limited, a Tencent company. All rights reserved.
 *
 * Licensed under the BSD 3-Clause License (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 *
 * https://opensource.org/licenses/BSD-3-Clause
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 *
 */

package com.tencent.angel.ml.matrix.psf.update;

import com.tencent.angel.ml.matrix.psf.update.enhance.MMUpdateFunc;
import com.tencent.angel.ps.impl.matrix.ServerDenseDoubleRow;
import com.tencent.angel.ps.impl.matrix.ServerSparseDoubleLongKeyRow;
import it.unimi.dsi.fastutil.longs.Long2DoubleOpenHashMap;

import java.nio.DoubleBuffer;
import java.util.Map;

/**
 * `Axpy` function is a implement of blas.axpy. That is `yId` += a * `xId`
 */
public class Axpy extends MMUpdateFunc {

  public Axpy(int matrixId, int xId, int yId, double a) {
    super(matrixId, new int[]{xId, yId}, new double[]{a});
  }

  public Axpy() {
    super();
  }

  @Override
  protected void doUpdate(ServerDenseDoubleRow[] rows, double[] scalars) {
    try {
      rows[1].getLock().writeLock().lock();
      DoubleBuffer xData = rows[0].getData();
      DoubleBuffer yData = rows[1].getData();
      double a = scalars[0];
      int size = rows[0].size();
      for (int i = 0; i < size; i++) {
        yData.put(i, a * xData.get(i) + yData.get(i));
      }
    } finally {
      rows[1].getLock().writeLock().unlock();
    }
  }

  @Override
  protected void doUpdate(ServerSparseDoubleLongKeyRow[] rows, double[] scalars) {
    double a = scalars[0];
    Long2DoubleOpenHashMap xData = rows[0].getData();

    Long2DoubleOpenHashMap yData = xData.clone();
    for (Map.Entry<Long, Double> entry: yData.entrySet()) {
      entry.setValue(entry.getValue() * a);
    }
    rows[1].mergeIndexValueMap(yData);
  }
}
