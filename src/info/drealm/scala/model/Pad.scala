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

abstract class Pad extends DataItem with mutable.Seq[Byte] {
    protected var _curve: Byte = 0
    protected var _gate: Byte = 22
    protected var _channel: Byte = 9
    protected var _minVelocity: Byte = 1
    protected var _maxVelocity: Byte = 127
    protected var _flags: Byte = 0 // bit7: HiHat pad; bit6: motif pad

    protected val _slots: Array[Byte]

    protected def from(pad: Pad) = {
        _curve = pad._curve
        _gate = pad._gate
        _channel = pad._channel
        _minVelocity = pad._minVelocity
        _maxVelocity = pad._maxVelocity
        _flags = pad._flags
    }

    def deserialize(in: DataInputStream): Unit = {
        in.read(_slots, 0, 2)
        _curve = in.readByte()
        _gate = in.readByte()
        _channel = in.readByte()
        _minVelocity = in.readByte()
        _maxVelocity = in.readByte()
        in.read(_slots, 2, 4)
        _flags = in.readByte()
    }
    def serialize(out: DataOutputStream, saving: Boolean): Unit = {
        out.write(_slots, 0, 2)
        out.writeByte(_curve)
        out.writeByte(_gate)
        out.writeByte(_channel)
        out.writeByte(_minVelocity)
        out.writeByte(_maxVelocity)
        out.write(_slots, 2, 4)
        out.writeByte(_flags)
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
        _curve == thatPad._curve &&
            _gate == thatPad._gate &&
            _channel == thatPad._channel &&
            _minVelocity == thatPad._minVelocity &&
            _maxVelocity == thatPad._maxVelocity &&
            _flags == thatPad._flags
    }
}

class PadV3 extends Pad {
    def this(in: DataInputStream) = {
        this()
        deserialize(in)
    }

    def this(padV4: PadV4) = {
        this()
        from(padV4)

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
        (0 to 5) zip padV4 foreach (x => _slots.update(x._1, x._2))

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
        dataItemChanged
    }

    protected val _slots: Array[Byte] = (Seq(42.toByte) ++ Stream.continually(128.toByte).take(5)).toArray

    override def canEqual(that: Any): Boolean = that.isInstanceOf[PadV3]
    override def equals(that: Any): Boolean = canEqual(that) && super.equals(that) && _slots.equals(that.asInstanceOf[PadV3]._slots)
}

class PadV4(self: Byte) extends Pad {
    def this(in: DataInputStream) = {
        this(0.toByte) // Next line overwrites linkTo
        deserialize(in)
    }

    def this(self: Byte, padV3: PadV3) = {
        this(self) // We want to make sure linkTo is set to a good initial value
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
        dataItemChanged
    }

    override def deserialize(in: DataInputStream): Unit = {
        super.deserialize(in)
        in.read(_slots, 6, 10)
        _linkTo = in.readByte()
    }
    override def serialize(out: DataOutputStream, saving: Boolean): Unit = {
        super.serialize(out, saving)
        out.write(_slots, 6, 10)
        out.writeByte(_linkTo)
    }

    protected val _slots: Array[Byte] = (Seq(42.toByte) ++ Stream.continually(128.toByte).take(15)).toArray
    private[this] var _linkTo: Byte = self

    def linkTo: Byte = _linkTo
    def linkTo_=(value: Byte): Unit = if (_linkTo != value) update(_linkTo = value) else {}

    override def canEqual(that: Any): Boolean = that.isInstanceOf[PadV4]
    override def equals(that: Any): Boolean = canEqual(that) && super.equals(that) && _slots.equals(that.asInstanceOf[PadV4]._slots)
}

abstract class PadSeq[T <: Pad] extends DataItem with mutable.Seq[Pad] {
    protected val _pads: Array[Pad] = new Array[Pad](28)

    def iterator = _pads.iterator
    def length = _pads.length
    def update(idx: Int, value: Pad): Unit = {
        if (null == value)
            throw new IllegalArgumentException("Pad must not be null.")
        if (_pads(idx) != value)
            update(_pads.update(idx, value))
    }
    def apply(idx: Int): T = _pads.apply(idx).asInstanceOf[T]

    override def deserialize(in: DataInputStream): Unit = _pads foreach (x => x.deserialize(in))
    override def serialize(out: DataOutputStream, saving: Boolean): Unit = _pads foreach (x => if (saving) x.save(out) else x.serialize(out, saving))

    reactions += {
        case e: info.drealm.scala.eventX.DataItemChanged => {
            deafTo(this)
            dataItemChanged
            listenTo(this)
        }
    }
}

class PadV3Seq private (f: (Int => PadV3)) extends PadSeq[PadV3] {
    def this() = this(x => new PadV3)
    def this(in: DataInputStream) = this(x => new PadV3(in))
    def this(padV4seq: PadV4Seq) = {
        this(x => new PadV3(padV4seq(x)))
        dataItemChanged
    }
    (0 to 27) foreach (x => {
        _pads(x) = f(x)
        listenTo(_pads(x))
    })

    override def update(idx: Int, value: Pad): Unit = {
        if (!value.isInstanceOf[PadV3])
            throw new IllegalArgumentException("Pad must be PadV3.")
        super.update(idx, value)
    }
}

class PadV4Seq private (f: (Int => PadV4)) extends PadSeq[PadV4] {
    def this() = this(x => new PadV4((x + 1).toByte))
    def this(in: DataInputStream) = this(x => new PadV4(in))
    def this(padV3seq: PadV3Seq) = {
        this(x => new PadV4((x + 1).toByte, padV3seq(x)))
        dataItemChanged
    }
    (0 to 27) foreach (x => {
        _pads(x) = f(x)
        listenTo(_pads(x))
    })

    override def update(idx: Int, value: Pad): Unit = {
        if (!value.isInstanceOf[PadV4])
            throw new IllegalArgumentException("Pad must be PadV4.")
        super.update(idx, value)
    }
}
