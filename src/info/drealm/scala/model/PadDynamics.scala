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

class PadDynamics(__lowLevel: Byte, __highLevel: Byte, __userMargin: Byte, __internalMargin: Byte, __thresholdManual: Byte, __thresholdActual: Byte) extends DataItem {
    def this() = this(0, 0, 0, 0, 0, 0)
    def this(that: PadDynamics) = this(that.lowLevel, that.highLevel, that.userMargin, that.internalMargin, that.thresholdManual, that.thresholdActual)

    private[this] var _lowLevel: Byte = __lowLevel // DynLow
    private[this] var _highLevel: Byte = __highLevel // DynHigh
    private[this] var _userMargin: Byte = __userMargin
    private[this] var _internalMargin: Byte = __internalMargin // Idle Level
    private[this] var _thresholdManual: Byte = __thresholdManual // Threshold
    private[this] var _thresholdActual: Byte = __thresholdActual

    def deserialize(in: DataInputStream): Unit = throw new UnsupportedOperationException("This should never happen")
    def serialize(out: DataOutputStream, saving: Boolean): Unit = throw new UnsupportedOperationException("This should never happen")

    override def clone = new PadDynamics(this)

    def lowLevel: Byte = _lowLevel
    def lowLevel_=(value: Byte): Unit = if (_lowLevel != value) update(_lowLevel = value) else {}
    def highLevel: Byte = _highLevel
    def highLevel_=(value: Byte): Unit = if (_highLevel != value) update(_highLevel = value) else {}
    def userMargin: Byte = _userMargin
    def userMargin_=(value: Byte): Unit = if (_userMargin != value) update(_userMargin = value) else {}
    def internalMargin: Byte = _internalMargin
    def internalMargin_=(value: Byte): Unit = if (_internalMargin != value) update(_internalMargin = value) else {}
    def thresholdManual: Byte = _thresholdManual
    def thresholdManual_=(value: Byte): Unit = if (_thresholdManual != value) update(_thresholdManual = value) else {}
    def thresholdActual: Byte = _thresholdActual
    def thresholdActual_=(value: Byte): Unit = if (_thresholdActual != value) update(_thresholdActual = value) else {}
}
