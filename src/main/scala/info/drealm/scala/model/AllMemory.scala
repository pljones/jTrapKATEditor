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

abstract class AllMemory(k: => Int => Kit[_ <: Pad], kn: (Int, Kit[_ <: Pad]) => Unit, u: () => Array[Byte], g: => () => Global[_ <: Pad], il: Boolean)
    extends DataItem with mutable.Seq[Kit[_ <: Pad]] {

    def iterator = _kits.iterator
    def length = _kits.length
    def update(idx: Int, value: Kit[_ <: Pad]): Unit = {
        if (null == value)
            throw new IllegalArgumentException("Kit must not be null.")
        if (_kits(idx) != value) update(u = { _kits.update(idx, value) })
    }
    def apply(idx: Int): Kit[_ <: Pad] = _kits.apply(idx)

    def deserialize(in: InputStream): Unit = {
        if (_namesInLine) {
            _kits foreach (x => {
                x.deserialize(in)
                x.deserializeKitName(in)
            })
        } else {
            _kits foreach (x => x.deserialize(in))
            _kits foreach (x => x.deserializeKitName(in))
        }
        in.read(_unused)
        _global.deserialize(in)
    }
    def serialize(out: OutputStream, saving: Boolean): Unit = {
        if (_namesInLine) {
            if (saving) {
                _kits foreach (x => {
                    x.save(out)
                    x.serializeKitName(out)
                })
            } else {
                _kits foreach (x => {
                    x.serialize(out, saving)
                    x.serializeKitName(out)
                })
            }
        } else {
            if (saving) _kits foreach (x => x.save(out)) else _kits foreach (x => x.serialize(out, saving))
            _kits foreach (x => x.serializeKitName(out))
        }
        out.write(_unused)
        if (saving) _global.save(out) else _global.serialize(out, saving)
    }

    private[this] val _namesInLine: Boolean = il
    private[this] val _kits: Array[Kit[_ <: Pad]] = ((0 to 23) map (x => null)).toArray
    if (_namesInLine) {
        (0 to 23) foreach (x => {
            _kits(x) = k(x)
            kn(x, _kits(x))
        })
    } else {
        (0 to 23) foreach (x => _kits(x) = k(x))
        (0 to 23) foreach (x => kn(x, _kits(x)))
    }

    private[this] val _unused: Array[Byte] = u()
    private[this] var _global: Global[_ <: Pad] = g()

    def global: Global[_ <: Pad] = _global
    def global_=(value: Global[_ <: Pad]) = if (_global != value) update(u = { _global = value })

    def changed = _changed || _kits.foldLeft(false)(_ || _.changed) || _global.changed
}

class AllMemoryV3 private (k: Int => KitV3, kn: (Int, Kit[_]) => Unit, u: => () => Array[Byte], g: => () => GlobalV3) extends AllMemory(k, kn, u, g, false) {
    def this() = this(x => new KitV3, (i, x) => {}, () => new Array(540), () => new GlobalV3)
    def this(in: InputStream) = this(
        x => new KitV3(in),
        (i, x) => x.deserializeKitName(in),
        () => LazyList.continually(in.read().toByte).take(540).toArray,
        () => new GlobalV3(in))
    def this(allMemoryV4: AllMemoryV4) = {
        this(
            x => new KitV3(allMemoryV4(x).asInstanceOf[KitV4]),
            (i, x) => x.kitName = allMemoryV4(i).kitName,
            () => new Array(540),
            () => new GlobalV3(allMemoryV4.global.asInstanceOf[GlobalV4]))
        update(u = { /*It's all been done*/ })
    }
    def this(allMemoryV5: AllMemoryV5) = {
        this(
            x => new KitV3(allMemoryV5(x).asInstanceOf[KitV5]),
            (i, x) => x.kitName = allMemoryV5(i).kitName,
            () => new Array(540),
            () => new GlobalV3(allMemoryV5.global.asInstanceOf[GlobalV5]))
        update(u = { /*It's all been done*/ })
    }
}

class AllMemoryV4 private (k: Int => KitV4, kn: (Int, Kit[_]) => Unit, u: () => Array[Byte], g: () => GlobalV4) extends AllMemory(k, kn, u, g, false) {
    def this() = this(x => new KitV4, (i, x) => {}, () => new Array(195), () => new GlobalV4)
    def this(in: InputStream) = this(
        x => new KitV4(in),
        (i, x) => x.deserializeKitName(in),
        () => LazyList.continually(in.read().toByte).take(195).toArray,
        () => new GlobalV4(in))
    def this(allMemoryV3: AllMemoryV3) = {
        this(
            x => new KitV4(allMemoryV3(x).asInstanceOf[KitV3]),
            (i, x) => x.kitName = allMemoryV3(i).kitName,
            () => new Array(195),
            () => new GlobalV4(allMemoryV3.global.asInstanceOf[GlobalV3]))
        update(u = { /*It's all been done*/ })
    }
    def this(allMemoryV5: AllMemoryV5) = {
        this(
            x => new KitV4(allMemoryV5(x).asInstanceOf[KitV5]),
            (i, x) => x.kitName = allMemoryV5(i).kitName,
            () => new Array(195),
            () => new GlobalV4(allMemoryV5.global.asInstanceOf[GlobalV5]))
        update(u = { /*It's all been done*/ })
    }
}

class AllMemoryV5 private (k: Int => KitV5, kn: (Int, Kit[_]) => Unit, u: () => Array[Byte], g: () => GlobalV5) extends AllMemory(k, kn, u, g, true) {
    def this() = this(x => new KitV5, (i, x) => {}, () => new Array(39), () => new GlobalV5)
    def this(in: InputStream) = this(
        x => new KitV5(in),
        (i, x) => x.deserializeKitName(in),
        () => LazyList.continually(in.read().toByte).take(39).toArray,
        () => new GlobalV5(in))
    def this(allMemoryV3: AllMemoryV3) = {
        this(
            x => new KitV5(allMemoryV3(x).asInstanceOf[KitV3]),
            (i, x) => x.kitName = allMemoryV3(i).kitName,
            () => new Array(39),
            () => new GlobalV5(allMemoryV3.global.asInstanceOf[GlobalV3]))
        update(u = { /*It's all been done*/ })
    }
    def this(allMemoryV4: AllMemoryV4) = {
        this(
            x => new KitV5(allMemoryV4(x).asInstanceOf[KitV4]),
            (i, x) => x.kitName = allMemoryV4(i).kitName,
            () => new Array(39),
            () => new GlobalV5(allMemoryV4.global.asInstanceOf[GlobalV4]))
        update(u = { /*It's all been done*/ })
    }
}
