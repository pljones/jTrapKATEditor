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

abstract class Kit[T <: PadSeq[_ <: Pad]](n: Int) extends DataItem with mutable.Seq[Pad] {
    protected class SoundControl extends DataItem {
        def this(in: DataInputStream) = {
            this()
            deserialize(in)
        }

        var _prgChg: Byte = 0
        var _prgChgTxnChn: Byte = 0
        var _volume: Byte = 0
        var _bankMSB: Byte = 0
        var _bankLSB: Byte = 0

        override def deserialize(in: DataInputStream): Unit = {
            _prgChg = in.readByte()
            _prgChgTxnChn = in.readByte()
            _volume = in.readByte()
            _bankMSB = in.readByte()
            _bankLSB = in.readByte()
        }
        override def serialize(out: DataOutputStream, saving: Boolean): Unit = {
            // because KitV3 abuses SoundControl...
            if (saving && out == null) {
                // Simply do nothing
            }
            else {
                out.writeByte(_prgChg)
                out.writeByte(_prgChgTxnChn)
                out.writeByte(_volume)
                out.writeByte(_bankMSB)
                out.writeByte(_bankLSB)
            }
        }

        def prgChg: Byte = _prgChg
        def prgChg_=(value: Byte): Unit = if (_prgChg != value) update(_prgChg = value) else {}
        def prgChgTxnChn: Byte = _prgChgTxnChn
        def prgChgTxnChn_=(value: Byte): Unit = if (_prgChgTxnChn != value) update(_prgChgTxnChn = value) else {}
        def volume: Byte = _volume
        def volume_=(value: Byte): Unit = if (_volume != value) update(_volume = value) else {}
        def bankMSB: Byte = _bankMSB
        def bankMSB_=(value: Byte): Unit = if (_bankMSB != value) update(_bankMSB = value) else {}
        def bankLSB: Byte = _bankLSB
        def bankLSB_=(value: Byte): Unit = if (_bankLSB != value) update(_bankLSB = value) else {}
    }

    protected var _pads: T
    protected var _curve: Byte = 0
    protected var _gate: Byte = 0
    protected var _channel: Byte = 9
    protected var _minVelocity: Byte = 1
    protected var _maxVelocity: Byte = 127
    protected var _fcFunction: Byte = 3
    protected var _bcFunction: Byte = 0
    // V3 has prgChg, prgChgTxnChn, volume here
    protected val _hhPads = new Array[Byte](4)
    // V3 has bank here -> not in SoundControl, assumed deprecated
    protected var _fcChannel: Byte = 16
    protected var _fcCurve: Byte = 0
    // V3 has bankMSB, bankLSB, unused here
    // V4 has Sound Control 1 to 4 here
    protected val _kitName: Array[Char] = "New kit     ".toArray

    protected val _soundControls: Array[SoundControl] = new Array[SoundControl](n)

    protected def _deserialize(in: DataInputStream, newPadSeq: (DataInputStream => T)): Unit = {
        _pads = newPadSeq(in)
        _curve = in.readByte()
        _gate = in.readByte()
        _channel = in.readByte()
        _minVelocity = in.readByte()
        _maxVelocity = in.readByte()
        _fcFunction = in.readByte()
        _bcFunction = in.readByte()
    }
    protected def _serialize(out: DataOutputStream, saving: Boolean): Unit = {
        if (saving) _pads.save(out) else _pads.serialize(out, saving)
        out.writeByte(_curve)
        out.writeByte(_gate)
        out.writeByte(_channel)
        out.writeByte(_minVelocity)
        out.writeByte(_maxVelocity)
        out.writeByte(_fcFunction)
        out.writeByte(_bcFunction)
    }
    def deserializeKitName(in: DataInputStream) {
        (0 to 11) foreach (x => _kitName(x) = in.readByte().toChar)
    }
    def serializeKitName(out: DataOutputStream) {
        (0 to 11) foreach (x => out.writeByte(_kitName(x).toByte))
    }

    def iterator = _pads.iterator
    def length = _pads.length
    def update(idx: Int, pad: Pad) = _pads.update(idx, pad)
    def apply(idx: Int): Pad = _pads.apply(idx)

    def soundControls: Array[SoundControl] = _soundControls

    def curve: Byte = _curve
    def curve_=(value: Byte): Unit = if (_curve != value) update(_curve = value) else {}
    def gate: Byte = _gate
    def gate_=(value: Byte): Unit = if (_gate != value) update(_gate = value) else {}
    def channel: Byte = _channel
    def channel_=(value: Byte): Unit = if (_channel != value) update(_channel = value) else {}
    def minVelocity: Byte = _minVelocity
    def minVelocity_=(value: Byte): Unit = if (_minVelocity != value) update(_minVelocity = value) else {}
    def maxVelocity: Byte = _maxVelocity
    def maxVelocity_=(value: Byte): Unit = if (_maxVelocity != value) update(_maxVelocity = value) else {}
    def fcFunction: Byte = _fcFunction
    def fcFunction_=(value: Byte): Unit = if (_fcFunction != value) update(_fcFunction = value) else {}
    def bcFunction: Byte = _bcFunction
    def bcFunction_=(value: Byte): Unit = if (_bcFunction != value) update(_bcFunction = value) else {}
    def hhPads(idx: Int): Byte = _hhPads.apply(idx)
    def hhPads(idx: Int, value: Byte) = if (_hhPads.apply(idx) != value) update(_hhPads.update(idx, value)) else {}
    def fcChannel: Byte = _fcChannel
    def fcChannel_=(value: Byte): Unit = if (_fcChannel != value) update(_fcChannel = value) else {}
    def fcCurve: Byte = _fcCurve
    def fcCurve_=(value: Byte): Unit = if (_fcCurve != value) update(_fcCurve = value) else {}
    def kitName: String = "" + (_kitName.toSeq)
    def kitName_=(value: String): Unit = f"${value.trim()}%12s" match {
        case tooLong if tooLong.length() > 12 =>
            throw new IllegalArgumentException("KitName must be 12 characters or fewer.")
        case update if update != _kitName => {
            (0 to 11) zip update map (x => _kitName(x._1) = x._2)
            dataItemChanged
        }
        case _ => {}
    }

    listenTo(_pads)
    _soundControls foreach (x => listenTo(x))

    reactions += {
        case e: info.drealm.scala.eventX.DataItemChanged => {
            deafTo(this)
            dataItemChanged
            listenTo(this)
        }
    }
}

class KitV3 extends Kit[PadV3Seq](1) {
    def this(in: DataInputStream) = {
        this()
        deserialize(in)
    }

    protected var _pads: PadV3Seq = new PadV3Seq
    protected var _bank: Byte = 0
    protected var _unused: Byte = 0

    override def deserialize(in: DataInputStream): Unit = {
        deafTo(_pads)
        _soundControls foreach (x => deafTo(x))

        _deserialize(in, x => new PadV3Seq(x))

        _soundControls(0)._prgChg = in.readByte()
        _soundControls(0)._prgChgTxnChn = in.readByte()
        _soundControls(0)._volume = in.readByte()

        in.read(_hhPads, 0, 4)

        _bank = in.readByte()

        _fcChannel = in.readByte()
        _fcCurve = in.readByte()

        _soundControls(0)._bankMSB = in.readByte()
        _soundControls(0)._bankLSB = in.readByte()

        _unused = in.readByte()

        listenTo(_pads)
        _soundControls foreach (x => listenTo(x))
    }
    override def serialize(out: DataOutputStream, saving: Boolean): Unit = {
        _serialize(out, saving)

        out.writeByte(_soundControls(0)._prgChg)
        out.writeByte(_soundControls(0)._prgChgTxnChn)
        out.writeByte(_soundControls(0)._volume)

        out.write(_hhPads, 0, 4)

        out.writeByte(_bank)

        out.writeByte(_fcChannel)
        out.writeByte(_fcCurve)

        out.writeByte(_soundControls(0)._bankMSB)
        out.writeByte(_soundControls(0)._bankLSB)

        out.writeByte(_unused)

        // I knew there was a reason to allow serialize(out, flag) to be overridden...
        _soundControls(0).save(null.asInstanceOf[DataOutputStream])
    }

    def bank: Byte = _bank
    def bank_=(value: Byte): Unit = if (_bank != value) update(_bank = value) else {}
    def unused: Byte = _unused
    def unused_=(value: Byte): Unit = if (_unused != value) update(_unused = value) else {}
}

class KitV4 extends Kit[PadV4Seq](4) {
    def this(in: DataInputStream) = {
        this()
        deserialize(in)
    }

    protected var _pads: PadV4Seq = null

    _soundControls foreach (x => listenTo(x))

    override def deserialize(in: DataInputStream): Unit = {
        deafTo(_pads)
        _soundControls foreach (x => deafTo(x))

        _deserialize(in, x => new PadV4Seq(x))

        in.read(_hhPads, 0, 4)
        _fcChannel = in.readByte()
        _fcCurve = in.readByte()

        (0 to 3) foreach (x => _soundControls(x) = new SoundControl(in))

        listenTo(_pads)
        _soundControls foreach (x => listenTo(x))
    }
    override def serialize(out: DataOutputStream, saving: Boolean): Unit = {
        _serialize(out, saving)

        out.write(_hhPads, 0, 4)

        out.writeByte(_fcChannel)
        out.writeByte(_fcCurve)

        _soundControls foreach (x => if (saving) x.save(out) else x.serialize(out, saving))
    }
}
