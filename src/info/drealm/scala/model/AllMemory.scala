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

//abstract class AllMemory[TKit <% Kit[_], TGlobal <% Global[_]](k: => Int => TKit, kn: (Int, TKit) => Unit, u: () => Array[Byte], g: => () => TGlobal)(implicit TKit: Manifest[TKit], TGlobal: Manifest[TGlobal])
//    extends DataItem with mutable.Seq[TKit] {
abstract class AllMemory(k: => Int => Kit[_ <: Pad], kn: (Int, Kit[_ <: Pad]) => Unit, u: () => Array[Byte], g: => () => Global[_])
    extends DataItem with mutable.Seq[Kit[_ <: Pad]] {

    def iterator = _kits.iterator
    def length = _kits.length
//    def update(idx: Int, value: TKit): Unit = {
    def update(idx: Int, value: Kit[_ <: Pad]): Unit = {
        if (null == value)
            throw new IllegalArgumentException("Kit must not be null.")
        if (_kits(idx) != value) {
            deafTo(_kits.apply(idx))
            update(_kits.update(idx, value))
            listenTo(_kits.apply(idx))
            dataItemChanged
        }
    }
//    def apply(idx: Int): TKit = _kits.apply(idx)
    def apply(idx: Int): Kit[_ <: Pad] = _kits.apply(idx)

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

//    private[this] val _kits: Array[TKit] = ((0 to 23) map (x => k(x))).toArray
    private[this] val _kits: Array[Kit[_ <: Pad]] = ((0 to 23) map (x => k(x))).toArray
    (0 to 23) foreach (x => kn(x, _kits(x)))

    private[this] val _unused: Array[Byte] = u()
//    private[this] var _global: TGlobal = g()
    private[this] var _global: Global[_] = g()

//    def global: TGlobal = _global
//    def global_=(value: TGlobal) = update(_global = value)
    def global: Global[_] = _global
    def global_=(value: Global[_]) = if (_global != value) update(_global = value)

    (0 to 23) foreach (x => listenTo(_kits(x)))
    listenTo(_global)
}

class AllMemoryV3 private (k: Int => KitV3, kn: (Int, Kit[_]) => Unit, u: => () => Array[Byte], g: => () => GlobalV3) extends AllMemory(k, kn, u, g) {
    def this() = this(x => new KitV3, (i, x) => {}, () => new Array(540), () => new GlobalV3)
    def this(in: FileInputStream) = this(
        x => new KitV3(in),
        (i, x) => x.deserializeKitName(in),
        () => Stream.continually(in.read().toByte).take(540).toArray,
        () => new GlobalV3(in))
    def this(allMemoryV4: AllMemoryV4) = this(x => new KitV3(allMemoryV4(x).asInstanceOf[KitV4]), (i, x) => x.kitName = allMemoryV4(i).kitName, () => new Array(540), () => new GlobalV3(allMemoryV4.global.asInstanceOf[GlobalV4]))
}

class AllMemoryV4 private (k: Int => KitV4, kn: (Int, Kit[_]) => Unit, u: () => Array[Byte], g: () => GlobalV4) extends AllMemory(k, kn, u, g) {
    def this() = this(x => new KitV4, (i, x) => {}, () => new Array(195), () => new GlobalV4)
    def this(in: FileInputStream) = this(
        x => new KitV4(in),
        (i, x) => x.deserializeKitName(in),
        () => Stream.continually(in.read().toByte).take(195).toArray,
        () => new GlobalV4(in))
    def this(allMemoryV3: AllMemoryV3) = this(x => new KitV4(allMemoryV3(x).asInstanceOf[KitV3]), (i, x) => x.kitName = allMemoryV3(i).kitName, () => new Array(195), () => new GlobalV4(allMemoryV3.global.asInstanceOf[GlobalV3]))
}
