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

import java.io.{ Console => _, _ }
import collection.mutable

abstract class Kit[TPad <: Pad](f: => PadSeq[TPad], g: Array[SoundControl])(implicit TPad: Manifest[TPad]) extends DataItem with mutable.Seq[TPad] {
    private[this] var _pads: PadSeq[TPad] = f
    private[this] var _curve: Byte = 0
    private[this] var _gate: Byte = 22
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
    protected val _soundControls: Array[SoundControl] = g
    // Strictly this is an array of bytes; C# just uses String...
    private[this] val _kitName: Array[Char] = "New kit     ".toCharArray()

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
        Array.copy(kit.kitName.padTo(_kitName.length, ' ').toCharArray(), 0, _kitName, 0, _kitName.length)
    }

    protected def _deserializeKit(in: InputStream): Unit
    protected def _deserialize(in: InputStream): Unit = {
        _curve = in.read().toByte
        _gate = in.read().toByte
        _channel = in.read().toByte
        _minVelocity = in.read().toByte
        _maxVelocity = in.read().toByte
        _fcFunction = in.read().toByte
        _bcFunction = in.read().toByte
    }
    protected def _deserializeHH(in: InputStream): Unit = in.read(_hhPads, 0, 4)
    protected def _deserializeFC(in: InputStream): Unit = {
        _fcChannel = in.read().toByte
        _fcCurve = in.read().toByte
    }
    override def deserialize(in: InputStream): Unit = {
        _pads.deserialize(in)
        _deserializeKit(in)
    }
    def deserializeKitName(in: InputStream) = (0 to 11) foreach (x => _kitName(x) = in.read().toByte.toChar)

    protected def _serialize(out: OutputStream, saving: Boolean): Unit = {
        if (saving) _pads.save(out) else _pads.serialize(out, saving)
        out.write(_curve)
        out.write(_gate)
        out.write(_channel)
        out.write(_minVelocity)
        out.write(_maxVelocity)
        out.write(_fcFunction)
        out.write(_bcFunction)
    }
    protected def _serializeHH(out: OutputStream) = out.write(_hhPads, 0, 4)
    protected def _serializeFC(out: OutputStream) = {
        out.write(_fcChannel)
        out.write(_fcCurve)
    }
    def serializeKitName(out: OutputStream) = (0 to 11) foreach (x => out.write(_kitName(x).toByte))

    def iterator = _pads.iterator
    def length = _pads.length
    def update(idx: Int, pad: TPad) = _pads.update(idx, pad)
    def apply(idx: Int): TPad = _pads.apply(idx)

    // Prevent "soundControls(x) = y"...
    object soundControls {
        def apply(index: Int) = _soundControls(index)
    }

    def curve: Byte = _curve
    def curve_=(value: Byte): Unit = if (_curve != value) update(_curve = value) else {}
    def isKitCurve: Boolean = forall(p => p.curve == _curve)
    def toKitCurve(): Unit = update(foreach (p => p.curve = _curve))

    def gate: Byte = _gate
    def gate_=(value: Byte): Unit = if (_gate != value) update(_gate = value) else {}
    def isKitGate: Boolean = forall(p => p.gate == _gate)
    def toKitGate(): Unit = update(foreach (p => p.gate = _gate))
    
    def channel: Byte = _channel
    def channel_=(value: Byte): Unit = if (_channel != value) update(_channel = value) else {}
    def isKitChannel: Boolean = forall(p => p.channel == _channel)
    def toKitChannel(): Unit = update(foreach (p => p.channel = _channel))

    def minVelocity: Byte = _minVelocity
    def minVelocity_=(value: Byte): Unit = if (_minVelocity != value) update(_minVelocity = value) else {}
    def isKitMinVel: Boolean = forall(p => p.minVelocity == _minVelocity)
    def toKitMinVel(): Unit = update(foreach (p => p.minVelocity = _minVelocity))

    def maxVelocity: Byte = _maxVelocity
    def maxVelocity_=(value: Byte): Unit = if (_maxVelocity != value) update(_maxVelocity = value) else {}
    def isKitMaxVel: Boolean = forall(p => p.maxVelocity == _maxVelocity)
    def toKitMaxVel(): Unit = update(foreach (p => p.maxVelocity = _maxVelocity))

    def fcFunction: Byte = _fcFunction
    def fcFunction_=(value: Byte): Unit = if (_fcFunction != value) update(_fcFunction = value) else {}
    def bcFunction: Byte = _bcFunction
    def bcFunction_=(value: Byte): Unit = if (_bcFunction != value) update(_bcFunction = value) else {}
    def hhPads(idx: Int): Byte = _hhPads.apply(idx)
    def hhPads(idx: Int, value: Byte) = if (_hhPads.apply(idx) != value) update(_hhPads.update(idx, value)) else {}
    def hhPadNos(value: Byte): Seq[Int] = (0 to 3).filter(idx => _hhPads(idx) == value)
    def fcChannel: Byte = _fcChannel
    def fcChannel_=(value: Byte): Unit = if (_fcChannel != value) update(_fcChannel = value) else {}
    def fcCurve: Byte = _fcCurve
    def fcCurve_=(value: Byte): Unit = if (_fcCurve != value) update(_fcCurve = value) else {}
    def kitName: String = new String(_kitName)
    def kitName_=(value: String): Unit = value.trim().padTo(12, ' ') match {
        case newName if !newName.equals(kitName) => update(Array.copy(newName.toCharArray(), 0, _kitName, 0, _kitName.length))
        case _                                   => {}
    }


    def changed = _changed || _pads.changed || _soundControls.foldLeft(false)(_ || _.changed)
}

class KitV3 private (f: => PadV3Seq, g: => Array[SoundControl]) extends Kit[PadV3](f, g) {
    def this() = this(new PadV3Seq, Seq(new SoundControl).toArray)
    def this(in: InputStream) = {
        this(new PadV3Seq(in), new Array[SoundControl](1))
        _deserializeKit(in)
    }

    def this(kitV4: KitV4) = {
        this(new PadV3Seq(kitV4), Seq(kitV4.soundControls(0).clone).toArray)
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
        _changed = true
    }

    protected var _bank: Byte = 128.toByte
    protected var _unused: Byte = 0

    override def _deserializeKit(in: InputStream): Unit = {
        _deserialize(in)

        val prgChg = in.read().toByte
        val prgChgTxmChn = in.read().toByte
        val volume = in.read().toByte

        _deserializeHH(in)

        _bank = in.read().toByte

        _deserializeFC(in)

        val bankMSB = in.read().toByte
        val bankLSB = in.read().toByte

        _unused = in.read().toByte

        _soundControls(0) = new SoundControl(prgChg, prgChgTxmChn, volume, bankMSB, bankLSB)
    }

    override def serialize(out: OutputStream, saving: Boolean): Unit = {
        _serialize(out, saving)

        out.write(soundControls(0).prgChg)
        out.write(soundControls(0).prgChgTxmChn)
        out.write(soundControls(0).volume)

        _serializeHH(out)

        out.write(_bank)

        _serializeFC(out)

        out.write(soundControls(0).bankMSB)
        out.write(soundControls(0).bankLSB)

        out.write(_unused)

        // I knew there was a reason to allow serialize(out, flag) to be overridden...
        if (saving) soundControls(0).save(null.asInstanceOf[OutputStream])
    }

    def bank: Byte = _bank
    def bank_=(value: Byte): Unit = if (_bank != value) update(_bank = value) else {}
    def unused: Byte = _unused
    def unused_=(value: Byte): Unit = if (_unused != value) update(_unused = value) else {}
}

class KitV4 private (f: => PadV4Seq, g: => Array[SoundControl]) extends Kit[PadV4](f, g) {
    def this() = this(new PadV4Seq, (Stream.continually(new SoundControl).take(4)).toArray)
    def this(in: InputStream) = {
        this(new PadV4Seq(in), new Array[SoundControl](4))
        _deserializeKit(in)
    }

    def this(kitV3: KitV3) = {
        this(new PadV4Seq(kitV3), (Seq(kitV3.soundControls(0).clone) ++ (Stream.continually(new SoundControl).take(3))).toArray)
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
        _changed = true
    }

    override def _deserializeKit(in: InputStream): Unit = {
        _deserialize(in)
        _deserializeHH(in)
        _deserializeFC(in)

        (0 to 3) foreach (x => _soundControls(x) = new SoundControl(in))
    }

    override def serialize(out: OutputStream, saving: Boolean): Unit = {
        _serialize(out, saving)
        _serializeHH(out)
        _serializeFC(out)

        _soundControls foreach (x => if (saving) x.save(out) else x.serialize(out, saving))
    }
}
