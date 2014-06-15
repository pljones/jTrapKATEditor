/**
 * **************************************************************************
 *                                                                          *
 *   (C) Copyright 2014 by Peter L Jones                                    *
 *   pljones@users.sf.net                                                   *
 *                                                                          *
 *   This file is part of jTrapKATEditor.                                   *
 *                                                                          *
 *   jTrapKATEditor is free software; you can redistribute it and/or modify *
 *   it under the terms of the GNU General Public License as published by   *
 *   the Free Software Foundation; either version 3 of the License, or      *
 *   (at your option) any later version.                                    *
 *                                                                          *
 *   jTrapKATEditor is distributed in the hope that it will be useful,      *
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of         *
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the          *
 *   GNU General Public License for more details.                           *
 *                                                                          *
 *   You should have received a copy of the GNU General Public License      *
 *   along with jTrapKATEditor.  If not, see http://www.gnu.org/licenses/   *
 *                                                                          *
 * **************************************************************************
 */

package info.drealm.scala.model

import java.io._
import collection.mutable

class PadDynamics(idx: Int, padDynamicsContainer: PadDynamicsContainer) {
    def lowLevel: Byte = padDynamicsContainer.lowLevel(idx)
    def lowLevel_=(value: Byte): Unit = padDynamicsContainer.lowLevel(idx, value)
    def highLevel: Byte = padDynamicsContainer.highLevel(idx)
    def highLevel_=(value: Byte): Unit = padDynamicsContainer.highLevel(idx, value)
    def userMargin: Byte = padDynamicsContainer.userMargin(idx)
    def userMargin_=(value: Byte): Unit = padDynamicsContainer.userMargin(idx, value)
    def internalMargin: Byte = padDynamicsContainer.internalMargin(idx)
    def internalMargin_=(value: Byte): Unit = padDynamicsContainer.internalMargin(idx, value)
    def thresholdManual: Byte = padDynamicsContainer.thresholdManual(idx)
    def thresholdManual_=(value: Byte): Unit = padDynamicsContainer.thresholdManual(idx, value)
    def thresholdActual: Byte = padDynamicsContainer.thresholdActual(idx)
    def thresholdActual_=(value: Byte): Unit = padDynamicsContainer.thresholdActual(idx, value)
}

protected[model] class PadDynamicsContainer(_padLevels: Array[Byte], _userMargin: Array[Byte], _internalMargin: Array[Byte], _thresholdManual: Array[Byte], _thresholdActual: Array[Byte])
    extends DataItem with Seq[PadDynamics] {

    def lowLevel(idx: Int): Byte = _padLevels.apply(idx * 2)
    def lowLevel(idx: Int, value: Byte): Unit = if (_padLevels.apply(idx * 2) != value) update(_padLevels.update(idx * 2, value)) else {}
    def highLevel(idx: Int): Byte = _padLevels.apply(idx * 2 + 1)
    def highLevel(idx: Int, value: Byte): Unit = if (_padLevels.apply(idx * 2 + 1) != value) update(_padLevels.update(idx * 2 + 1, value)) else {}
    def userMargin(idx: Int): Byte = _userMargin.apply(idx)
    def userMargin(idx: Int, value: Byte): Unit = if (_userMargin.apply(idx) != value) update(_userMargin.update(idx, value)) else {}
    def internalMargin(idx: Int): Byte = _internalMargin.apply(idx)
    def internalMargin(idx: Int, value: Byte): Unit = if (_internalMargin.apply(idx) != value) update(_internalMargin.update(idx, value)) else {}
    def thresholdManual(idx: Int): Byte = _thresholdManual.apply(idx)
    def thresholdManual(idx: Int, value: Byte): Unit = if (_thresholdManual.apply(idx) != value) update(_thresholdManual.update(idx, value)) else {}
    def thresholdActual(idx: Int): Byte = _thresholdActual.apply(idx)
    def thresholdActual(idx: Int, value: Byte): Unit = if (_thresholdActual.apply(idx) != value) update(_thresholdActual.update(idx, value)) else {}

    def iterator = _padDynamics.iterator
    def length = _padDynamics.length
    def apply(idx: Int): PadDynamics = _padDynamics.apply(idx)

    def deserialize(in: FileInputStream): Unit = throw new UnsupportedOperationException("This should never happen")
    def serialize(out: FileOutputStream, saving: Boolean): Unit = throw new UnsupportedOperationException("This should never happen")

    private[this] val _padDynamics: Seq[PadDynamics] = (0 to 24) map (x => new PadDynamics(x, this))

}
