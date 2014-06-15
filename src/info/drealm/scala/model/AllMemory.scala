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

abstract class AllMemory[TKit <: Kit[_], TGlobal <: Global[_]](k: => Int => TKit, kn: (Int, TKit) => Unit, u: () => Array[Byte], g: => () => TGlobal)(implicit TKit: Manifest[TKit], TGlobal: Manifest[TGlobal])
    extends DataItem with mutable.Seq[TKit] {

    def iterator = _kits.iterator
    def length = _kits.length
    def update(idx: Int, value: TKit): Unit = {
        if (null == value)
            throw new IllegalArgumentException("Kit must not be null.")
        if (_kits(idx) != value) {
            deafTo(_kits.apply(idx))
            update(_kits.update(idx, value))
            listenTo(_kits.apply(idx))
            dataItemChanged
        }
    }
    def apply(idx: Int): TKit = _kits.apply(idx)

    def deserialize(in: FileInputStream): Unit = {
        _kits foreach (x => x.deserialize(in))
        _kits foreach (x => x.deserializeKitName(in))
        in.read(_unused)
        _global.deserialize(in)
    }
    def serialize(out: FileOutputStream, saving: Boolean): Unit = {
        if (saving) _kits foreach (x => x.save(out)) else _kits foreach (x => x.serialize(out, saving))
        _kits foreach (x => x.serializeKitName(out))
        out.write(_unused)
        if (saving) _global.save(out) else _global.serialize(out, saving)
    }

    private[this] val _kits: Array[TKit] = ((0 to 23) map (x => k(x))).toArray
    (0 to 23) foreach (x => kn(x, _kits(x)))

    private[this] val _unused: Array[Byte] = u()
    private[this] val _global: TGlobal = g()

    def global: TGlobal = _global

    (0 to 23) foreach (x => listenTo(_kits(x)))
    listenTo(_global)
}

class AllMemoryV3 private (k: Int => KitV3, kn: (Int, KitV3) => Unit, u: => () => Array[Byte], g: => () => GlobalV3) extends AllMemory[KitV3, GlobalV3](k, kn, u, g) {
    def this() = this(x => new KitV3, (i, x) => x.kitName = i + " New Kit", () => new Array(540), () => new GlobalV3)
    def this(in: FileInputStream) = this(
        x => new KitV3(in),
        (i, x) => x.deserializeKitName(in),
        () => Stream.continually(in.read().toByte).take(540).toArray,
        () => new GlobalV3(in))
    def this(allMemoryV4: AllMemoryV4) = this(x => new KitV3(allMemoryV4(x)), (i, x) => x.kitName = allMemoryV4(i).kitName, () => new Array(540), () => new GlobalV3(allMemoryV4.global))
}

class AllMemoryV4 private (k: Int => KitV4, kn: (Int, KitV4) => Unit, u: () => Array[Byte], g: () => GlobalV4) extends AllMemory[KitV4, GlobalV4](k, kn, u, g) {
    def this() = this(x => new KitV4, (i, x) => x.kitName = i + " New Kit", () => new Array(195), () => new GlobalV4)
    def this(in: FileInputStream) = this(
        x => new KitV4(in),
        (i, x) => x.deserializeKitName(in),
        () => Stream.continually(in.read().toByte).take(195).toArray,
        () => new GlobalV4(in))
    def this(allMemoryV3: AllMemoryV3) = this(x => new KitV4(allMemoryV3(x)), (i, x) => x.kitName = allMemoryV3(i).kitName, () => new Array(195), () => new GlobalV4(allMemoryV3.global))
}
