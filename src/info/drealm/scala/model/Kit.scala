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

abstract class Kit[T <: PadSeq](f: => T, g: Array[SoundControl]) extends DataItem with mutable.Seq[Pad] {
    private[this] var _pads: T = f
    private[this] var _curve: Byte = 0
    private[this] var _gate: Byte = 0
    private[this] var _channel: Byte = 9
    private[this] var _minVelocity: Byte = 1
    private[this] var _maxVelocity: Byte = 127
    private[this] var _fcFunction: Byte = 3
    private[this] var _bcFunction: Byte = 0
    // V3 has prgChg, prgChgTxnChn, volume here
    private[this] val _hhPads = new Array[Byte](4)
    // V3 has bank here -> not in SoundControl, assumed deprecated
    private[this] var _fcChannel: Byte = 16
    private[this] var _fcCurve: Byte = 0
    // V3 has bankMSB, bankLSB, unused here
    // V4 has Sound Control 1 to 4 here
    private[this] val _soundControls: Array[SoundControl] = g
    // Strictly this is an array of bytes; C# just uses String...
    private[this] val _kitName: Array[Char] = "New kit     ".toArray

    protected def from(kit: Kit[_]) = {
        _curve = kit.curve
        _gate = kit.gate
        _channel = kit.channel
        _minVelocity = kit.minVelocity
        _maxVelocity = kit.maxVelocity
        _fcFunction = kit.fcFunction
        _bcFunction = kit.bcFunction
        (0 to 3) foreach (idx => _hhPads(idx) = kit.hhPads(idx))
        _fcChannel = kit.fcChannel
        _fcCurve = kit.fcCurve
        (0 to (_kitName.length - 1)) zip kit.kitName foreach (x => _kitName(x._1) = x._2)
    }

    protected def _deserializeKit(in: DataInputStream): Unit
    protected def _deserialize(in: DataInputStream): Unit = {
        _curve = in.readByte()
        _gate = in.readByte()
        _channel = in.readByte()
        _minVelocity = in.readByte()
        _maxVelocity = in.readByte()
        _fcFunction = in.readByte()
        _bcFunction = in.readByte()
    }
    protected def _deserializeHH(in: DataInputStream): Unit = in.read(_hhPads, 0, 4)
    protected def _deserializeFC(in: DataInputStream): Unit = {
        _fcChannel = in.readByte()
        _fcCurve = in.readByte()
    }
    override def deserialize(in: DataInputStream): Unit = {
        deafTo(_pads)
        _soundControls foreach (x => deafTo(x))

        _pads.deserialize(in)
        _deserializeKit(in)

        listenTo(_pads)
        _soundControls foreach (x => listenTo(x))
    }
    def deserializeKitName(in: DataInputStream) = (0 to 11) foreach (x => _kitName(x) = in.readByte().toChar)

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
    protected def _serializeHH(out: DataOutputStream) = out.write(_hhPads, 0, 4)
    protected def _serializeFC(out: DataOutputStream) = {
        out.writeByte(_fcChannel)
        out.writeByte(_fcCurve)
    }
    def serializeKitName(out: DataOutputStream) = (0 to 11) foreach (x => out.writeByte(_kitName(x).toByte))

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
            (0 to 11) zip update foreach (x => _kitName(x._1) = x._2)
            dataItemChanged
        }
        case _ => {}
    }

    listenTo(_pads)
    _soundControls foreach (x => listenTo(x))
}

class KitV3 private (f: => PadV3Seq, g: => Array[SoundControl]) extends Kit[PadV3Seq](f, g) {
    def this() = this(new PadV3Seq, Seq(new SoundControl).toArray)
    def this(in: DataInputStream) = {
        this(new PadV3Seq(in), new Array[SoundControl](1))
        _deserializeKit(in)
    }

    def this(kitV4: KitV4) = {
        this(new PadV3Seq(kitV4 map (x => x.asInstanceOf[PadV4])), Seq(kitV4.soundControls(0).clone).toArray)
        from(kitV4)

        _bank = kitV4.soundControls(0).bankLSB
        // leave _unused at initial value

        // Curve needs fixing
        kitV4.curve match {
            case 21 => // Alternating
                // At kit level, we cannot resolve this.
                // Set to Curve 1 and let the pads sort themselves out.
                curve = 0
            case 22 => // Control + 3 Notes
                // The pads will sort the note numbers and set to curve Layer 4.
                curve = 20
            case _ => {}
        }

        // set state to dirty if it isn't already
        dataItemChanged
    }

    protected var _bank: Byte = 128.toByte
    protected var _unused: Byte = 0

    override def _deserializeKit(in: DataInputStream): Unit = {
        _deserialize(in)

        val prgChg = in.readByte()
        val prgChgTxnChn = in.readByte()
        val volume = in.readByte()

        _deserializeHH(in)

        _bank = in.readByte()

        _deserializeFC(in)

        val bankMSB = in.readByte()
        val bankLSB = in.readByte()

        _unused = in.readByte()

        soundControls(0) = new SoundControl(prgChg, prgChgTxnChn, volume, bankMSB, bankLSB)
    }

    override def serialize(out: DataOutputStream, saving: Boolean): Unit = {
        _serialize(out, saving)

        out.writeByte(soundControls(0).prgChg)
        out.writeByte(soundControls(0).prgChgTxnChn)
        out.writeByte(soundControls(0).volume)

        _serializeHH(out)

        out.writeByte(_bank)

        _serializeFC(out)

        out.writeByte(soundControls(0).bankMSB)
        out.writeByte(soundControls(0).bankLSB)

        out.writeByte(_unused)

        // I knew there was a reason to allow serialize(out, flag) to be overridden...
        if (saving) soundControls(0).save(null.asInstanceOf[DataOutputStream])
    }

    def bank: Byte = _bank
    def bank_=(value: Byte): Unit = if (_bank != value) update(_bank = value) else {}
    def unused: Byte = _unused
    def unused_=(value: Byte): Unit = if (_unused != value) update(_unused = value) else {}
}

class KitV4 private (f: => PadV4Seq, g: => Array[SoundControl]) extends Kit[PadV4Seq](f, g) {
    def this() = this(new PadV4Seq, Stream.continually(new SoundControl).take(4).toArray)
    def this(in: DataInputStream) = {
        this(new PadV4Seq(in), new Array[SoundControl](4))
        _deserializeKit(in)
    }

    def this(kitV3: KitV3) = {
        this(new PadV4Seq(kitV3 map (x => x.asInstanceOf[PadV3])), (Seq(kitV3.soundControls(0).clone) ++ Stream.continually(new SoundControl).take(4)).take(4).toArray)
        from(kitV3)

        // Curve needs fixing - pads can sort the details
        kitV3.curve match {
            case 21 => {} // Alternate 1,2
            case 22 => // Alternate 1,2,3
                curve = 21
            case 23 => // Alternate 1,2,3,4
                curve = 21
            case _ => {}
        }
        
        // set state to dirty if it isn't already
        dataItemChanged
    }

    override def _deserializeKit(in: DataInputStream): Unit = {
        _deserialize(in)
        _deserializeHH(in)
        _deserializeFC(in)

        (0 to 3) foreach (x => soundControls(x) = new SoundControl(in))
    }

    override def serialize(out: DataOutputStream, saving: Boolean): Unit = {
        _serialize(out, saving)
        _serializeHH(out)
        _serializeFC(out)

        soundControls foreach (x => if (saving) x.save(out) else x.serialize(out, saving))
    }
}
