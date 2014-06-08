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

    def deserialize(in: DataInputStream): Unit = {
        val b: Byte = in.readByte
        _companyId = if (b == 0) ((0x00000000 & (in.readByte << 8)) & in.readByte) else (0x01000000 & b)
    }
    def serialize(out: DataOutputStream, saving: Boolean): Unit = {
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

    protected def _deserialize(in: DataInputStream): Unit
    def deserialize(in: DataInputStream): Unit = {
        _sysexStart = in.readByte
        if (_sysexStart != sysexStart)
            throw new IOException(f"Sysex file does not have expected start byte 0x${sysexStart}%02x, read 0x${_sysexStart}%02x.")

        _companyId.deserialize(in)
        if (_companyId != companyId)
            throw new IOException(f"Sysex file does not have expected CompanyId ${companyId}, read ${_companyId}.")

        _deserialize(in)

        if (_sysexEnd != sysexEnd)
            throw new IOException(f"Sysex file does not have expected end byte 0x${sysexEnd}%02x, read 0x${_sysexEnd}%02x.")
    }

    protected def _serialize(out: DataOutputStream, saving: Boolean): Unit
    def serialize(out: DataOutputStream, saving: Boolean): Unit = {
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

    def _deserialize(in: DataInputStream): Unit = {
        _instrumentType = in.readByte
        _dumpType = in.readByte
        _instrumentId = in.readByte
        _version = in.readByte
        _auxType = in.readByte

        readSysexData(new AlternateModeSysexInputStream(in))
    }
    protected def readSysexData(in: AlternateModeSysexInputStream): Unit

    def _serialize(out: DataOutputStream, saving: Boolean): Unit = {
        out.write(_instrumentType)
        out.write(_dumpType.toByte)
        out.write(_instrumentId)
        out.write(_version)
        out.write(_auxType)

        writeSysexData(out, saving)
    }
    protected def writeSysexData(out: DataOutputStream, saving: Boolean): Unit

    private[this] var _instrumentType: Byte = 0
    private[this] var _dumpType: DumpType = NotSet
    private[this] var _instrumentId: Byte = 0
    private[this] var _version: Byte = 0
    private[this] var _auxType: Byte = 0
}

class AlternateModeSysexInputStream(in: DataInputStream) {
    def readByte: Byte = {
        val lo: Byte = in.readByte()
        val hi: Byte = in.readByte()
        (0x00 | (hi << 4) | lo).toByte
    }
    def read(value: Array[Byte]): Int = {
        val buffer: Array[Byte] = new Array[Byte](value.length * 2)
        val result = in.read(buffer)
        (0 to value.length - 1) foreach (x => value(x) = (0x00 | (buffer(x * 2 + 1) << 4) | buffer(x * 2)).toByte)
        result >> 1
    }
}

class AlternateModeSysexOutputStream(out: DataOutputStream) {
    def write(value: Byte): Unit = {
        out.write((0x0f & value).toByte)
        out.write(((0xf0 & value) >> 4).toByte)
    }
    def write(value: Array[Byte]): Unit = value foreach (x => write(x))
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
    def save(out: FileOutputStream): Unit = serialize(new DataOutputStream(out), true)
}

class AllMemoryV3Dump extends TrapKATSysexDump {
    def this(in: FileInputStream) = { this(); deserialize(new DataInputStream(in)) }
    protected def readSysexData(in: AlternateModeSysexInputStream): Unit = {
        Console.println("AllMemoryV3Dump readSysexData")
    }
    protected def writeSysexData(out: DataOutputStream, saving: Boolean): Unit = {}
}

class AllMemoryV4Dump extends TrapKATSysexDump {
    def this(in: FileInputStream) = { this(); deserialize(new DataInputStream(in)) }
    protected def readSysexData(in: AlternateModeSysexInputStream): Unit = {
        Console.println("AllMemoryV4Dump readSysexData")
    }
    protected def writeSysexData(out: DataOutputStream, saving: Boolean): Unit = {}
}

class GlobalV3Dump extends TrapKATSysexDump {
    def this(in: FileInputStream) = { this(); deserialize(new DataInputStream(in)) }
    protected def readSysexData(in: AlternateModeSysexInputStream): Unit = {
        Console.println("GlobalV3Dump readSysexData")
    }
    protected def writeSysexData(out: DataOutputStream, saving: Boolean): Unit = {}
}

class GlobalV4Dump extends TrapKATSysexDump {
    def this(in: FileInputStream) = { this(); deserialize(new DataInputStream(in)) }
    protected def readSysexData(in: AlternateModeSysexInputStream): Unit = {}
    protected def writeSysexData(out: DataOutputStream, saving: Boolean): Unit = {
        Console.println("GlobalV4Dump readSysexData")
    }
}

class KitV3Dump extends TrapKATSysexDump {
    def this(in: FileInputStream) = { this(); deserialize(new DataInputStream(in)) }
    protected def readSysexData(in: AlternateModeSysexInputStream): Unit = {}
    protected def writeSysexData(out: DataOutputStream, saving: Boolean): Unit = {
        Console.println("KitV3Dump readSysexData")
    }
}

class KitV4Dump extends TrapKATSysexDump {
    def this(in: FileInputStream) = { this(); deserialize(new DataInputStream(in)) }
    protected def readSysexData(in: AlternateModeSysexInputStream): Unit = {
        Console.println("KitV4Dump readSysexData")
    }
    protected def writeSysexData(out: DataOutputStream, saving: Boolean): Unit = {}
}
