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
import scala._
import collection.mutable
import scala.language.implicitConversions

object DumpType extends Enumeration {
    type DumpType = Value
    val Global = Value(0)
    val AllMemory = Value(1)
    val Motif = Value(2) //Not used in TrapKAT
    val Kit = Value(3)
    val NotSet = Value(0xff)

    implicit def byteToDumpType(value: Byte): DumpType = if (value != 0xff.toByte) DumpType(0x00000000 & value) else throw new NoSuchElementException(f"${0x00000000 & value} is not a valid DumpType")
    implicit def dumpTypeToByte(value: DumpType): Byte = value.id.toByte
}

object CompanyId {
    implicit def companyIdToString(companyId: CompanyId): String = if ((companyId._companyId & 0x01000000) > 0) f"${companyId._companyId & 0xff}%02x" else f"${companyId._companyId & 0xffff}%04x"
    implicit def companyIdToInt(companyId: CompanyId): Int = companyId._companyId
    implicit def intToCompanyId(value: Int): CompanyId = if ((value & 0x01000000) > 0) new CompanyId((value & 0xff).toByte) else new CompanyId((value & 0xffff).toShort)
    implicit def byteToCompanyId(value: Byte): CompanyId = new CompanyId(value)
    implicit def shortToCompanyId(value: Short): CompanyId = new CompanyId(value)
}
class CompanyId private (value: Int) extends DataItem {
    def this(value: Byte) = this(0x01000000 & value)
    def this(value: Short) = this(0x00000000 & value)
    var _companyId: Int = value

    def deserialize(in: FileInputStream): Unit = {
        val b: Byte = in.read().toByte
        _companyId = if (b == 0) ((0x00000000 & (in.read().toByte << 8)) & in.read().toByte) else (0x01000000 & b)
    }
    def serialize(out: FileOutputStream, saving: Boolean): Unit = {
        if ((_companyId & 0x01000000) > 0) {
            out.write(0.toByte)
            out.write(((_companyId & 0xff00) >> 8).toByte)
        }
        out.write((_companyId & 0xff).toByte)
    }
}

object SysexDump {
    val sysexStart: Byte = 0xF0.toByte
    val sysexEnd: Byte = 0xF7.toByte
}
abstract class SysexDump(companyId: CompanyId) { // extends DataItem - why?
    import SysexDump._

    protected def _deserialize(in: FileInputStream): Unit
    def deserialize(in: FileInputStream): Unit = {
        _sysexStart = in.read().toByte
        if (_sysexStart != sysexStart)
            throw new IOException(f"Sysex file does not have expected start byte 0x${sysexStart}%02x, read 0x${_sysexStart}%02x.")

        _companyId.deserialize(in)
        if (_companyId != companyId)
            throw new IOException(f"Sysex file does not have expected CompanyId ${companyId}, read ${_companyId}.")

        _deserialize(in)

        if (_sysexEnd != sysexEnd)
            throw new IOException(f"Sysex file does not have expected end byte 0x${sysexEnd}%02x, read 0x${_sysexEnd}%02x.")
    }

    protected def _serialize(out: FileOutputStream, saving: Boolean): Unit
    def serialize(out: FileOutputStream, saving: Boolean): Unit = {
        out.write(sysexStart)
        if (saving) companyId.save(out) else companyId.serialize(out, saving)

        _serialize(out, saving)

        out.write(sysexEnd)
    }

    private[this] var _sysexStart: Byte = sysexStart
    private[this] val _companyId: CompanyId = companyId
    private[this] var _sysexEnd: Byte = sysexEnd
}

abstract class AlternateModeSysexDump extends SysexDump(new CompanyId(0x0015.toShort)) {
    import DumpType._

    def _deserialize(in: FileInputStream): Unit = {
        _instrumentType = in.read().toByte
        if (_instrumentType != instrumentType)
            throw new IOException(f"Sysex file does not have expected instrumentType 0x${instrumentType}%02x, read 0x${_instrumentType}%02x.")

        _dumpType = in.read().toByte
        // Seriously, this is always zero.  The C# checks for "Enum.IsDefined" .. 0 is ..
        // However, rather than always write zero, we preserve the value read.
        // The default is zero, though, not the value passed into the class constructor, which is ignored.
        //if (_dumpType != dumpType && _dumpType != DumpType.NotSet)
        //    throw new IOException(f"Sysex file does not have expected dumpType 0x${dumpType}%02x, read 0x${_dumpType}%02x.")

        _instrumentId = in.read().toByte

        _version = in.read().toByte
        if (_version != version)
            throw new IOException(f"Sysex file does not have expected version 0x${version}%02x, read 0x${_version}%02x.")

        _auxType = in.read().toByte

        readSysexData(new AlternateModeSysexInputStream(in))
    }
    protected def readSysexData(in: FileInputStream): Unit

    def _serialize(out: FileOutputStream, saving: Boolean): Unit = {
        out.write(_instrumentType)
        out.write(_dumpType.toByte)
        out.write(_instrumentId)
        out.write(_version)
        out.write(_auxType)

        writeSysexData(new AlternateModeSysexOutputStream(out), saving)
    }
    protected def writeSysexData(out: FileOutputStream, saving: Boolean): Unit

    private[this] var _instrumentType: Byte = 0
    private[this] var _dumpType: DumpType = NotSet
    private[this] var _instrumentId: Byte = 0
    private[this] var _version: Byte = 0
    private[this] var _auxType: Byte = 0
}

class AlternateModeSysexInputStream(in: FileInputStream) extends FileInputStream(in.getFD()) {
    override def read: Int = {
        val lo = in.read()
        val hi = in.read()
        0x00000000 | (hi << 4) | lo
    }
    override def read(value: Array[Byte]): Int = read(value, 0, value.length)
    override def read(value: Array[Byte], off: Int, len: Int): Int = {
        val buffer: Array[Byte] = new Array[Byte](value.length * 2)
        val result = in.read(buffer, off * 2, len * 2)
        (off to (off + len - 1)) foreach (x => value(x) = (0x00 | (buffer(x * 2 + 1) << 4) | buffer(x * 2)).toByte)
        result >> 1
    }
}

class AlternateModeSysexOutputStream(out: FileOutputStream) extends FileOutputStream(out.getFD()) {
    def write(value: Byte): Unit = {
        out.write((0x0f & value).toByte)
        out.write(((0xf0 & value) >> 4).toByte)
    }
    override def write(value: Array[Byte], off: Int, len: Int): Unit = (off to (off + len - 1)) foreach (x => write(value(x)))
}

object TrapKATSysexDump {
    def fromFile(file: File): TrapKATSysexDump = {
        val in = new FileInputStream(file)
        try {
            in.available() match {
                case 2620  => new GlobalV3Dump(in)
                case 21364 => new AllMemoryV3Dump(in)
                case 746   => new KitV3Dump(in)
                case 2108  => new GlobalV4Dump(in)
                case 35570 => new AllMemoryV4Dump(in)
                case 1388  => new KitV4Dump(in)
                // Unknown!
                case unknown =>
                    throw new IllegalArgumentException(f"${file.getAbsolutePath()} has unknown length of ${in.available()}.")
            }
        }
        finally {
            in.close()
        }
    }

    def toFile(fileName: String, dump: TrapKATSysexDump): Unit = {
        val out = new FileOutputStream(fileName)
        try {
            dump.save(out)
        }
        finally {
            out.close()
        }
    }
}
abstract class TrapKATSysexDump extends AlternateModeSysexDump {
    def save(out: FileOutputStream): Unit = serialize(out, true)
}

class AllMemoryV3Dump extends TrapKATSysexDump {
    def this(in: FileInputStream) = { this(); deserialize(in) }
    protected def readSysexData(in: FileInputStream): Unit = {
        Console.println("AllMemoryV3Dump readSysexData")
    }
}

class AllMemoryV4Dump extends TrapKATSysexDump {
    def this(in: FileInputStream) = { this(); deserialize(in) }
    protected def readSysexData(in: FileInputStream): Unit = {
        Console.println("AllMemoryV4Dump readSysexData")
    }
}

class GlobalV3Dump extends TrapKATSysexDump {
    def this(in: FileInputStream) = { this(); deserialize(in) }
    protected def readSysexData(in: FileInputStream): Unit = {
        Console.println("GlobalV3Dump readSysexData")
    }
}

class GlobalV4Dump extends TrapKATSysexDump {
    def this(in: FileInputStream) = { this(); deserialize(in) }
    protected def readSysexData(in: FileInputStream): Unit = {
        Console.println("GlobalV4Dump readSysexData")
    }
}

class KitV3Dump extends TrapKATSysexDump {
    def this(in: FileInputStream) = { this(); deserialize(in) }
    protected def readSysexData(in: FileInputStream): Unit = {
        Console.println("KitV3Dump readSysexData")
    }
}

class KitV4Dump extends TrapKATSysexDump {
    def this(in: FileInputStream) = { this(); deserialize(in) }
    protected def readSysexData(in: FileInputStream): Unit = {
        Console.println("KitV4Dump readSysexData")
    }
}
