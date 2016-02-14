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

protected object CompanyId {
    implicit def companyIdToString(companyId: CompanyId): String = if ((companyId._companyId & 0x01000000) > 0) f"${companyId._companyId & 0xff}%02x" else f"${companyId._companyId & 0xffff}%04x"
    implicit def companyIdToInt(companyId: CompanyId): Int = companyId._companyId
    implicit def intToCompanyId(value: Int): CompanyId = if ((value & 0x01000000) > 0) new CompanyId((value & 0xff).toByte) else new CompanyId((value & 0xffff).toShort)
    implicit def byteToCompanyId(value: Byte): CompanyId = new CompanyId(value)
    implicit def shortToCompanyId(value: Short): CompanyId = new CompanyId(value)
}
protected class CompanyId private[CompanyId] (value: Int) extends DataItem {
    def this(value: Byte) = this(0x01000000 | value)
    def this(value: Short) = this(0x00000000 | value)
    var _companyId: Int = value

    def deserialize(in: InputStream): Unit = {
        val b: Byte = in.read().toByte
        _companyId = if (b == 0) ((0x00000000 | (in.read().toByte << 8)) | in.read().toByte) else (0x01000000 | b)
    }
    def serialize(out: OutputStream, saving: Boolean): Unit = {
        if ((_companyId & 0x01000000) == 0) {
            out.write(0.toByte)
            out.write(((_companyId & 0xff00) >> 8).toByte)
        }
        out.write((_companyId & 0xff).toByte)
    }

    def changed = false
}

protected object SysexDump {
    val sysexStart: Byte = 0xF0.toByte
    val sysexEnd: Byte = 0xF7.toByte
}
protected abstract class SysexDump(companyId: CompanyId) {
    import SysexDump._
    import CompanyId._

    protected def _deserialize(in: InputStream): Unit
    def deserialize(in: InputStream): Unit = {
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

    protected def _serialize(out: OutputStream, saving: Boolean): Unit
    def serialize(out: OutputStream, saving: Boolean): Unit = {
        out.write(sysexStart)

        if (saving) _companyId.save(out) else _companyId.serialize(out, saving)

        _serialize(out, saving)

        out.write(sysexEnd)
    }

    private[this] var _sysexStart: Byte = sysexStart
    private[this] val _companyId: CompanyId = companyId
    private[this] var _sysexEnd: Byte = sysexEnd
}

object AlternateModeSysexDump {
    val companyId: CompanyId = new CompanyId(0x0015.toShort)
}
protected abstract class AlternateModeSysexDump(instrumentType: Byte, dumpType: DumpType.DumpType, version: Byte, var auxType: Byte) extends SysexDump(AlternateModeSysexDump.companyId) {
    import DumpType._
    var instrumentId: Byte = 0

    def _deserialize(in: InputStream): Unit = {
        val _instrumentType = in.read().toByte
        if (_instrumentType != instrumentType)
            throw new IOException(f"Sysex file does not have expected instrumentType 0x${instrumentType}%02x, read 0x${_instrumentType}%02x.")

        // The C# checks for "Enum.IsDefined" .. 0 is ..
        val _dumpType = in.read().toByte
        if (_dumpType != dumpType.toByte)
            throw new IOException(f"Sysex file does not have expected dumpType 0x${dumpType}%02x, read 0x${_dumpType}%02x.")

        instrumentId = in.read().toByte

        val _version = in.read().toByte
        if (_version != version)
            throw new IOException(f"Sysex file does not have expected version 0x${version}%02x, read 0x${_version}%02x.")

        auxType = in.read().toByte

        readSysexData(new AlternateModeSysexInputStream(in))
    }
    protected def readSysexData(in: InputStream): Unit

    def _serialize(out: OutputStream, saving: Boolean): Unit = {
        out.write(instrumentType)
        out.write(dumpType.toByte)
        out.write(instrumentId)
        out.write(version)
        out.write(auxType)

        writeSysexData(new AlternateModeSysexOutputStream(out), saving)
    }
    protected def writeSysexData(out: OutputStream, saving: Boolean): Unit
}

protected class AlternateModeSysexInputStream(in: InputStream) extends InputStream {
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
protected class AlternateModeSysexOutputStream(out: OutputStream) extends OutputStream {
    override def write(value: Int): Unit = {
        out.write(0x0f & value)
        out.write(0x0f & (value >> 4))
    }
    override def write(value: Array[Byte]): Unit = write(value, 0, value.length)
    override def write(value: Array[Byte], off: Int, len: Int): Unit = (off to (off + len - 1)) foreach (x => write(value(x)))
}

object TrapKATSysexDump {
    val trapKATID: Byte = 0x63 // instrumentType
    val supportedVersion: Byte = 0x40 // version

    def fromFile(file: File): TrapKATSysexDump[_] = {
        val in = new FileInputStream(file)
        try {
            in.available() match {
                case 21364   => new AllMemoryV3Dump(in)
                case 35570   => new AllMemoryV4Dump(in)
                case 2620    => new GlobalV3Dump(in)
                case 2108    => new GlobalV4Dump(in)
                case 746     => new KitV3Dump(in)
                case 1388    => new KitV4Dump(in)
                // Unknown!
                case unknown => throw new IllegalArgumentException(f"${file.getAbsolutePath()} has unknown length of ${in.available()}.")
            }
        }
        finally {
            in.close()
        }
    }

    def toFile(file: File, data: DataItem, saving: Boolean): Unit = {
        val trapKATSysexDump: TrapKATSysexDump[_] = data match {
            case allMemoryV3: AllMemoryV3 => new AllMemoryV3Dump(allMemoryV3)
            case allMemoryV4: AllMemoryV4 => new AllMemoryV4Dump(allMemoryV4)
            case globalV3: GlobalV3       => new GlobalV3Dump(globalV3)
            case globalV4: GlobalV4       => new GlobalV4Dump(globalV4)
            // Unknown!
            case unknown                  => throw new IllegalArgumentException(f"data has unknown type of ${data.getClass().getSimpleName()}.")
        }
        val out = new FileOutputStream(file)
        try {
            trapKATSysexDump.serialize(out, saving)
        }
        finally {
            out.close()
        }
    }

    def toFile(file: File, data: DataItem, kitNumber: Int, saving: Boolean): Unit = {
        val trapKATSysexDump: TrapKATSysexDump[_] = data match {
            case kitV3: KitV3 => new KitV3Dump(kitV3, kitNumber.toByte)
            case kitV4: KitV4 => new KitV4Dump(kitV4, kitNumber.toByte)
            // Unknown!
            case unknown      => throw new IllegalArgumentException(f"data has unknown type of ${data.getClass().getSimpleName()}.")
        }
        val out = new FileOutputStream(file)
        try {
            trapKATSysexDump.serialize(out, saving)
        }
        finally {
            out.close()
        }
    }
}

protected abstract class TrapKATSysexDump[T <: DataItem](newT: => T, readT: InputStream => T, dumpType: DumpType.DumpType, auxType: Byte)(implicit T: Manifest[T])
    extends AlternateModeSysexDump(TrapKATSysexDump.trapKATID, dumpType, TrapKATSysexDump.supportedVersion, auxType) {
    var _self: T = newT
    def save(out: OutputStream): Unit = serialize(out, true)
    protected def readSysexData(in: InputStream): Unit = _self = readT(in)
    protected def writeSysexData(out: OutputStream, saving: Boolean): Unit = if (saving) _self.save(out) else _self.serialize(out, saving)

    def self: T = _self
}

class AllMemoryV3Dump(allMemoryV3: AllMemoryV3) extends TrapKATSysexDump[AllMemoryV3](allMemoryV3, in => new AllMemoryV3(in), DumpType.AllMemory, 0) {
    def this(in: InputStream) = { this(null.asInstanceOf[AllMemoryV3]); deserialize(in) }
}
class AllMemoryV4Dump(allMemoryV4: AllMemoryV4) extends TrapKATSysexDump[AllMemoryV4](allMemoryV4, in => new AllMemoryV4(in), DumpType.AllMemory, 0) {
    def this(in: InputStream) = { this(null.asInstanceOf[AllMemoryV4]); deserialize(in) }
}

class GlobalV3Dump(globalV3: GlobalV3) extends TrapKATSysexDump[GlobalV3](globalV3, in => new GlobalV3(in), DumpType.Global, 0) {
    def this(in: InputStream) = { this(null.asInstanceOf[GlobalV3]); deserialize(in) }
}

class GlobalV4Dump(globalV4: GlobalV4) extends TrapKATSysexDump[GlobalV4](globalV4, in => new GlobalV4(in), DumpType.Global, 0) {
    def this(in: InputStream) = { this(null.asInstanceOf[GlobalV4]); deserialize(in) }
}

class KitV3Dump(kitV3: KitV3, auxType: Byte) extends TrapKATSysexDump[KitV3](kitV3, in => { val self = new KitV3(in); self.deserializeKitName(in); self }, DumpType.Kit, auxType) {
    def this(in: InputStream) = { this(null.asInstanceOf[KitV3], 0); deserialize(in) }
    override protected def writeSysexData(out: OutputStream, saving: Boolean): Unit = {
        _self.serialize(out, saving)
        _self.serializeKitName(out)
    }
}

class KitV4Dump(kitV4: KitV4, auxType: Byte) extends TrapKATSysexDump[KitV4](kitV4, in => { val self = new KitV4(in); self.deserializeKitName(in); self }, DumpType.Kit, auxType) {
    def this(in: InputStream) = { this(null.asInstanceOf[KitV4], 0); deserialize(in) }
    override protected def writeSysexData(out: OutputStream, saving: Boolean): Unit = {
        _self.serialize(out, saving)
        _self.serializeKitName(out)
    }
}
