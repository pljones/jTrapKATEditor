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

abstract class Pad protected (f: => Array[Byte]) extends DataItem with mutable.Seq[Byte] {
    // Subclasses fix up _slots and _curve, so leave as protected
    protected val _slots: Array[Byte] = f
    protected var _curve: Byte = 0
    // For the rest, use the API to read and make them nicely private
    private[this] var _gate: Byte = 22
    private[this] var _channel: Byte = 9
    private[this] var _minVelocity: Byte = 1
    private[this] var _maxVelocity: Byte = 127
    private[this] var _flags: Byte = 0 // bit7: HiHat pad; bit6: motif pad

    protected def from(pad: Pad) = {
        _curve = pad.curve
        _gate = pad.gate
        _channel = pad.channel
        _minVelocity = pad.minVelocity
        _maxVelocity = pad.maxVelocity
        _flags = pad.flags
    }

    def deserialize(in: InputStream): Unit = {
        in.read(_slots, 0, 2)
        _curve = in.read().toByte
        _gate = in.read().toByte
        _channel = in.read().toByte
        _minVelocity = in.read().toByte
        _maxVelocity = in.read().toByte
        in.read(_slots, 2, 4)
        _flags = in.read().toByte
    }
    def serialize(out: OutputStream, saving: Boolean): Unit = {
        out.write(_slots, 0, 2)
        out.write(_curve)
        out.write(_gate)
        out.write(_channel)
        out.write(_minVelocity)
        out.write(_maxVelocity)
        out.write(_slots, 2, 4)
        out.write(_flags)
    }

    def iterator = _slots.iterator
    def length = _slots.length
    def update(idx: Int, value: Byte): Unit = if (_slots(idx) != value) update(_slots.update(idx, value))
    def apply(idx: Int): Byte = _slots.apply(idx)

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
    def flags: Byte = _flags
    def flags_=(value: Byte): Unit = if (_flags != value) update(_flags = value) else {}

    override def canEqual(that: Any): Boolean
    override def equals(that: Any): Boolean = canEqual(that) && {
        val thatPad = that.asInstanceOf[Pad]
        ((_slots zip thatPad._slots) forall (x => x._1 == x._2)) &&
            _curve == thatPad.curve &&
            _gate == thatPad.gate &&
            _channel == thatPad.channel &&
            _minVelocity == thatPad.minVelocity &&
            _maxVelocity == thatPad.maxVelocity &&
            _flags == thatPad.flags
    }
    
    def changed = _changed
}

class PadV3 private (f: => Array[Byte]) extends Pad(f) {
    def this() = this((Seq(42.toByte) ++ Stream.continually(128.toByte).take(5)).take(6).toArray)
    def this(in: InputStream) = {
        this(new Array[Byte](6))
        deserialize(in)
    }

    def this(padV4: PadV4) = {
        this(padV4.take(6).toArray)
        /* V4 "extra" notes are (128 upwards)
         *  Off
         *  Sequencer Start
         *  Sequencer Stop
         *  Sequencer Continue
         *  Alternate Reset (=132)
         *  Alternate Freeze
         *  Next Kit
         *  Previous Kit
         *  Pitchwheel
         * For now, just leave the value alone
         */

        from(padV4)

        // Curve needs fixing
        padV4.curve match {
            case 21 => // Alternating
                // Depending on where/whether there is an AlternateReset,
                // set to Alternate 1,2 or 1,2,3 or 1,2,3,4
                _curve = padV4.indexOf(132.toByte) match {
                    case 2 => 21 // This is Alternate 1,2 - curve 21
                    case 3 => 22 // This is Alternate 1,2,3 - curve 22
                    case 4 => 23 // This is Alternate 1,2,3,4 - curve 23
                    case _ => 23 // It came from Alternating - best we can do is curve 23
                }
            case 22 => // Control + 3 Notes
                // No conversion possible for Controller but the +3 notes could be Layer 4 with
                // first note of Off.
                _slots(0) = 128.toByte;
                _curve = 20
            case _ => {}
        }

        // set state to dirty
        _changed = true
    }

    override def canEqual(that: Any): Boolean = that.isInstanceOf[PadV3]
}

class PadV4 private (f: => Array[Byte], self: Byte) extends Pad(f) {
    def this(self: Byte) = this((Seq(42.toByte) ++ Stream.continually(128.toByte).take(16)).take(16).toArray, self)
    def this(in: InputStream) = {
        this(new Array[Byte](16), 0.toByte) // Don't really care but need to initialise
        deserialize(in)
    }

    def this(padV3: PadV3, self: Byte) = {
        this((padV3 ++ Stream.continually(128.toByte).take(16)).take(16).toArray, self)
        from(padV3)

        // 128 to 132 (alt reset) are defined with the same meaning
        (0 to 15) zipAll (padV3, -1, 128.toByte) foreach (x => _slots(x._1) = x._2)

        // Curve needs fixing
        padV3.curve match {
            case 21 => // Alternate 1,2
                _slots(2) = 132.toByte
            case 22 => // Alternate 1,2,3
                _curve = 21;
                _slots(3) = 132.toByte
            case 23 => // Alternate 1,2,3,4
                _curve = 21;
                _slots(4) = 132.toByte
            case _ => {}
        }

        // set state to dirty
        _changed = true
    }

    override def deserialize(in: InputStream): Unit = {
        super.deserialize(in)
        in.read(_slots, 6, 10)
        _linkTo = in.read().toByte
    }
    override def serialize(out: OutputStream, saving: Boolean): Unit = {
        super.serialize(out, saving)
        out.write(_slots, 6, 10)
        out.write(_linkTo)
    }

    override def canEqual(that: Any): Boolean = that.isInstanceOf[PadV4]
    override def equals(that: Any): Boolean = super.equals(that) && _linkTo == that.asInstanceOf[PadV4].linkTo

    private[this] var _linkTo: Byte = self
    def linkTo: Byte = _linkTo
    def linkTo_=(value: Byte): Unit = if (_linkTo != value) update(_linkTo = value) else {}
}

abstract class PadSeq[TPad <: Pad] protected (f: Int => TPad)(implicit TPad: Manifest[TPad]) extends DataItem with mutable.Seq[TPad] {
    private val _pads: Array[TPad] = ((0 to 27) map (f(_))).toArray

    def iterator = _pads.iterator
    def length = _pads.length
    def update(idx: Int, value: TPad): Unit = {
        if (null == value)
            throw new IllegalArgumentException("Pad must not be null.")
        if (_pads(idx) != value) update(_pads.update(idx, value))
    }
    def apply(idx: Int): TPad = _pads.apply(idx)

    // A significant difference from the C# here.
    // The C# creates a new, clean pad when deserialize is called on the container.
    // Here we just deserialize into the existing pad.
    // Seeing as the C# leaves the existing container, it makes more sense, I think,
    // to do the same with the pad itself.
    override def deserialize(in: InputStream): Unit = _pads foreach (x => x.deserialize(in))
    // .. and I could not be asked to optimise the loop to put the "if" outside ...
    override def serialize(out: OutputStream, saving: Boolean): Unit = _pads foreach (x => if (saving) x.save(out) else x.serialize(out, saving))

    def changed = _changed || _pads.foldLeft(false)(_ || _.changed)
}

class PadV3Seq private (f: (Int => PadV3)) extends PadSeq[PadV3](f) {
    def this() = this(x => new PadV3)
    def this(in: InputStream) = this(x => new PadV3(in))
    def this(padV4seq: Seq[PadV4]) = {
        this(x => new PadV3(padV4seq(x)))
        _changed = true
    }
}

class PadV4Seq private (f: (Int => PadV4)) extends PadSeq[PadV4](f) {
    def this() = this(x => new PadV4((x + 1).toByte))
    def this(in: InputStream) = this(x => new PadV4(in))
    def this(padV3seq: Seq[PadV3]) = {
        this(x => new PadV4(padV3seq(x), (x + 1).toByte))
        _changed = true
    }
}
