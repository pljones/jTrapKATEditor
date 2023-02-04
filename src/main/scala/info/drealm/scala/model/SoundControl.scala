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

class SoundControl protected[model] (__prgChg: Byte, __sndCtlTxmChn: Byte, __bankMSB: Byte, __bankLSB: Byte, __volume: Byte) extends DataItem with Cloneable {
    protected[model] def this(in: InputStream) = this(in.read().toByte, in.read().toByte, in.read().toByte, in.read().toByte, in.read().toByte)
    protected[model] def this() = this(0, 9, 128.toByte, 128.toByte, 128.toByte)
    protected[model] def this(soundControl: SoundControl) = this(soundControl.prgChg, soundControl.sndCtlTxmChn, soundControl.bankMSB, soundControl.bankLSB, soundControl.volume)

    private[this] var _prgChg: Byte = __prgChg // 1-128, 0=off
    private[this] var _sndCtlTxmChn: Byte = __sndCtlTxmChn // 0-15, no off; displayed as value + 1
    private[this] var _bankMSB: Byte = __bankMSB // 0-127, 128=off
    private[this] var _bankLSB: Byte = __bankLSB // 0-127, 128=off
    private[this] var _volume: Byte = __volume // 0-127, 128=off

    override def deserialize(in: InputStream): Unit = {
        _prgChg = in.read().toByte
        _sndCtlTxmChn = in.read().toByte
        _bankMSB = in.read().toByte
        _bankLSB = in.read().toByte
        _volume = in.read().toByte
    }
    override def serialize(out: OutputStream, saving: Boolean): Unit = {
        // because KitV3 abuses SoundControl...
        if (saving && out == null) {
            // Simply do nothing
        }
        else {
            out.write(_prgChg)
            out.write(_sndCtlTxmChn)
            out.write(_bankMSB)
            out.write(_bankLSB)
            out.write(_volume)
        }
    }

    override def clone: SoundControl = new SoundControl(this)

    def prgChg: Byte = _prgChg
    def prgChg_=(value: Byte): Unit = if (_prgChg != value) update({_prgChg = value}) else {}
    def sndCtlTxmChn: Byte = _sndCtlTxmChn
    def sndCtlTxmChn_=(value: Byte): Unit = if (_sndCtlTxmChn != value) update({_sndCtlTxmChn = value}) else {}
    def bankMSB: Byte = _bankMSB
    def bankMSB_=(value: Byte): Unit = if (_bankMSB != value) update({_bankMSB = value}) else {}
    def bankLSB: Byte = _bankLSB
    def bankLSB_=(value: Byte): Unit = if (_bankLSB != value) update({_bankLSB = value}) else {}
    def volume: Byte = _volume
    def volume_=(value: Byte): Unit = if (_volume != value) update({_volume = value}) else {}

    def changed = _changed
}
