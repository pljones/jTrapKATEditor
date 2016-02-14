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

package info.drealm.scala

import java.awt.datatransfer._
import scala.swing._
import info.drealm.scala.{ Localization => L }

object Clipboard extends ClipboardOwner with Publisher {

    def lostOwnership(clipboard: Clipboard, contents: Transferable) = {}

    private[this] val clipboard = java.awt.Toolkit.getDefaultToolkit().getSystemClipboard()

    private[this] object Empty extends Transferable {
        val dataFlavors = new Array[DataFlavor](0)
        def getTransferDataFlavors(): Array[DataFlavor] = dataFlavors
        def isDataFlavorSupported(flavor: DataFlavor) = false
        def getTransferData(flavor: DataFlavor) = throw new UnsupportedFlavorException(flavor)
    }
    private[this] trait Clippable extends Transferable with Serializable {
        protected def dataFlavour: DataFlavor
        private[this] val dataFlavors = Seq(dataFlavour).toArray
        def getTransferDataFlavors(): Array[DataFlavor] = dataFlavors
        def isDataFlavorSupported(flavor: DataFlavor) = flavor.equals(dataFlavour)
        def getTransferData(flavor: DataFlavor) = if (!isDataFlavorSupported(flavor)) throw new UnsupportedFlavorException(flavor) else this
    }
    private[this] case class CopyPadV3(var pad: model.PadV3, var hhPad: Boolean) extends Clippable {
        override def dataFlavour = dfPadV3
        private def writeObject(out: java.io.ObjectOutputStream): Unit = {
            pad.serialize(out)
            out.writeBoolean(hhPad)
        }
        private def readObject(in: java.io.ObjectInputStream): Unit = {
            pad = new model.PadV3(in)
            hhPad = in.readBoolean()
        }
    }
    private[this] case class CopyPadV4(var pad: model.PadV4, var hhPad: Boolean, var padNoWas: Byte) extends Clippable {
        override def dataFlavour = dfPadV4
        private def writeObject(out: java.io.ObjectOutputStream): Unit = {
            pad.serialize(out)
            out.writeBoolean(hhPad)
            out.writeByte(padNoWas)
        }
        private def readObject(in: java.io.ObjectInputStream): Unit = {
            pad = new model.PadV4(in)
            hhPad = in.readBoolean()
            padNoWas = in.readByte()
        }
    }
    private[this] case class SwapPads(kit: Int, pad: Int) extends Clippable { override def dataFlavour = dfSwapPads }
    private[this] case class CopyKitV3(var kit: model.KitV3, var kitNoWas: Byte) extends Clippable {
        override def dataFlavour = dfKitV3
        private def writeObject(out: java.io.ObjectOutputStream): Unit = {
            out.writeByte(kitNoWas)
            kit.serialize(out)
            kit.serializeKitName(out)
        }
        private def readObject(in: java.io.ObjectInputStream): Unit = {
            kitNoWas = in.readByte()
            kit = new model.KitV3(in)
            kit.deserializeKitName(in)
        }
    }
    private[this] case class CopyKitV4(var kit: model.KitV4, var kitNoWas: Byte) extends Clippable {
        override def dataFlavour = dfKitV4
        private def writeObject(out: java.io.ObjectOutputStream): Unit = {
            out.writeByte(kitNoWas)
            kit.serialize(out)
            kit.serializeKitName(out)
        }
        private def readObject(in: java.io.ObjectInputStream): Unit = {
            kitNoWas = in.readByte()
            kit = new model.KitV4(in)
            kit.deserializeKitName(in)
        }
    }
    private[this] case class SwapKits(kit: Int) extends Clippable { override def dataFlavour = dfSwapKits }

    private[this] val dfPadV3 = new DataFlavor(classOf[CopyPadV3], DataFlavor.javaSerializedObjectMimeType)
    private[this] val dfPadV4 = new DataFlavor(classOf[CopyPadV4], DataFlavor.javaSerializedObjectMimeType)
    private[this] val dfSwapPads = new DataFlavor(classOf[SwapPads], DataFlavor.javaJVMLocalObjectMimeType)
    private[this] val dfKitV3 = new DataFlavor(classOf[CopyKitV3], DataFlavor.javaSerializedObjectMimeType)
    private[this] val dfKitV4 = new DataFlavor(classOf[CopyKitV4], DataFlavor.javaSerializedObjectMimeType)
    private[this] val dfSwapKits = new DataFlavor(classOf[SwapKits], DataFlavor.javaJVMLocalObjectMimeType)

    private[this] def okayToConvert(thing: String, from: String, to: String): Boolean = Dialog.showConfirmation(tpnMain,
        L.G("PasteThing", to, from, thing),
        L.G("PasteThingCaption", thing),
        Dialog.Options.YesNo, Dialog.Message.Question, null) == Dialog.Result.Yes

    object ClipboardType extends Enumeration {
        type ClipboardType = Value
        val Pad = Value(0)
        val PadSwap = Value(1)
        val Kit = Value(2)
        val KitSwap = Value(3)
        val NotSet = Value(-1)
    }
    import ClipboardType._

    def clipboardType: ClipboardType = clipboard.getAvailableDataFlavors().headOption match {
        case Some(null)                             => { Console.println("Got a null data flavor!"); ClipboardType.NotSet }
        case Some(pad) if pad == dfPadV3            => ClipboardType.Pad
        case Some(pad) if pad == dfPadV4            => ClipboardType.Pad
        case Some(padSwap) if padSwap == dfSwapPads => ClipboardType.PadSwap
        case Some(kit) if kit == dfKitV3            => ClipboardType.Kit
        case Some(kit) if kit == dfKitV4            => ClipboardType.Kit
        case Some(kitSwap) if kitSwap == dfSwapKits => ClipboardType.KitSwap
        case otherwise                              => ClipboardType.NotSet
    }

    private[this] def getContentPadV3() = clipboard.getContents(this).getTransferData(dfPadV3).asInstanceOf[CopyPadV3]
    private[this] def getContentPadV4() = clipboard.getContents(this).getTransferData(dfPadV4).asInstanceOf[CopyPadV4]
    private[this] def getContentSwapPads() = clipboard.getContents(this).getTransferData(dfSwapPads).asInstanceOf[SwapPads]
    private[this] def getContentKitV3() = clipboard.getContents(this).getTransferData(dfKitV3).asInstanceOf[CopyKitV3]
    private[this] def getContentKitV4() = clipboard.getContents(this).getTransferData(dfKitV4).asInstanceOf[CopyKitV4]
    private[this] def getContentSwapKits() = clipboard.getContents(this).getTransferData(dfSwapKits).asInstanceOf[SwapKits]

    def copyPad(source: Component) = clipboard.setContents(jTrapKATEditor.doV3V4(
        CopyPadV3(jTrapKATEditor.currentPadV3, !jTrapKATEditor.currentKit.hhPadNos((jTrapKATEditor.currentPadNumber + 1).toByte).isEmpty),
        CopyPadV4(jTrapKATEditor.currentPadV4, !jTrapKATEditor.currentKit.hhPadNos((jTrapKATEditor.currentPadNumber + 1).toByte).isEmpty, jTrapKATEditor.currentPadNumber.toByte)), this)

    def pastePad(source: Component) = clipboard.getAvailableDataFlavors().headOption match {
        case Some(pad) if (pad == dfPadV3 || pad == dfPadV4) && frmTrapkatSysexEditor.okayToSplat(jTrapKATEditor.currentPad, s"Pad ${jTrapKATEditor.currentPadNumber + 1}") => {
            pad match {
                case v3 if v3 == dfPadV3 => pastePadV3(source)
                case v4 if v4 == dfPadV4 => pastePadV4(source)
            }
        }
        case otherwise => {}
    }

    private[this] def isPadCurveKit(kit: model.Kit[_], pad: model.Pad): Boolean = pad.curve == kit.curve
    private[this] def padToKitCurve(kit: model.Kit[_], pad: model.Pad): Unit = pad.curve = kit.curve
    private[this] def isPadGateKit(kit: model.Kit[_], pad: model.Pad): Boolean = pad.gate == kit.gate
    private[this] def padToKitGate(kit: model.Kit[_], pad: model.Pad): Unit = pad.gate = kit.gate
    private[this] def isPadChannelKit(kit: model.Kit[_], pad: model.Pad): Boolean = pad.channel == kit.channel
    private[this] def padToKitChannel(kit: model.Kit[_], pad: model.Pad): Unit = pad.channel = kit.channel
    private[this] def isPadMinVelKit(kit: model.Kit[_], pad: model.Pad): Boolean = pad.minVelocity == kit.minVelocity
    private[this] def padToKitMinVel(kit: model.Kit[_], pad: model.Pad): Unit = pad.minVelocity = kit.minVelocity
    private[this] def isPadMaxVelKit(kit: model.Kit[_], pad: model.Pad): Boolean = pad.maxVelocity == kit.maxVelocity
    private[this] def padToKitMaxVel(kit: model.Kit[_], pad: model.Pad): Unit = pad.maxVelocity = kit.maxVelocity

    private[this] def padKitStatus(kit: model.Kit[_], pad: model.Pad) = Seq(
        ("Curve", () => kit.isKitCurve, isPadCurveKit _, padToKitCurve _),
        ("Gate", () => kit.isKitGate, isPadGateKit _, padToKitGate _),
        ("Channel", () => kit.isKitChannel, isPadChannelKit _, padToKitChannel _),
        ("MinVel", () => kit.isKitMinVel, isPadMinVelKit _, padToKitMinVel _),
        ("MaxVel", () => kit.isKitMaxVel, isPadMaxVelKit _, padToKitMaxVel _)) map (t => {
            (!t._2() || t._3(kit, pad), L.G("PadKitStatus", t._1, L.G(if (!t._2() || t._3(kit, pad)) "PadKitStatusOK" else "PadKitStatusBad")), () => t._4(kit, pad))
        })

    // Fortunately, this is a copy of a pad, so we can mess around with it regardless
    private[this] def pasteIfVarious(pad: model.Pad): Boolean = {
        val incoming = padKitStatus(jTrapKATEditor.currentKit, pad)

        if (!incoming.forall(t => t._1)) {
            // At least one of the pads has a value that does not match its kit (for which the kit is not in Various)
            Dialog.showConfirmation(tpnMain,
                L.G("PastePadOKToVarious", jTrapKATEditor.currentKit.kitName, incoming.map(t => t._2).mkString("\n")),
                L.G("PastePadOKToVariousCaption"),
                Dialog.Options.YesNoCancel, Dialog.Message.Question, null) match {
                    case Dialog.Result.No  => { incoming.foreach(t => t._3()); true } //... retain the kit values and then paste
                    case Dialog.Result.Yes => true //... just paste (go to Various)
                    case _                 => false //... do nothing
                }
        } else true
    }
    private[this] def setPad(source: Component, kitNo: Int, padNo: Int, pad: model.Pad) = {
        val kit = jTrapKATEditor.currentAllMemory(kitNo)
        jTrapKATEditor.currentPad match {
            case v3: model.PadV3 => kit.asInstanceOf[model.KitV3].update(padNo, pad.asInstanceOf[model.PadV3])
            case v4: model.PadV4 => kit.asInstanceOf[model.KitV4].update(padNo, pad.asInstanceOf[model.PadV4])
        }

        val p = (padNo + 1).toByte
        kit.hhPadNos(p).foreach(kit.hhPads(_, 0))
        if ((pad.flags & 0x80) != 0) kit.hhPadNos(0).headOption.foreach(kit.hhPads(_, p))

        if (kitNo == jTrapKATEditor.currentKitNumber) jTrapKATEditor.padChangedBy(source)
    }
    private[this] class PastePadHistoryAction(source: Component, pad: model.Pad) extends HistoryAction {
        val kitNoWas = jTrapKATEditor.currentKitNumber
        val padNoWas = jTrapKATEditor.currentPadNumber

        def clonePad(pad: model.Pad) = {
            val stream = new java.io.ByteArrayOutputStream()
            pad.serialize(stream)
            stream.flush()
            val in = new java.io.ByteArrayInputStream(stream.toByteArray())
            stream.close()
            val newPad = pad match {
                case v3: model.PadV3 => new model.PadV3(in)
                case v4: model.PadV4 => new model.PadV4(in)
            }
            in.close
            newPad
        }

        val padBefore = clonePad(jTrapKATEditor.currentPad)
        val padAfter = clonePad(pad)

        val actionName: String = "actionEditPastePad"
        def undoAction(): Unit = setPad(source, kitNoWas, padNoWas, padBefore)
        def redoAction(): Unit = setPad(source, kitNoWas, padNoWas, padAfter)
    }

    private[this] def pastePadV3(source: Component) {
        val padV3Clip = getContentPadV3()
        val padV3 = padV3Clip.pad
        val hhPad = padV3Clip.hhPad
        padV3.flags = ((if (hhPad) 0x80 else 0) | padV3.flags).toByte
        if (pasteIfVarious(padV3)) jTrapKATEditor.doV3V4({
            // v3 -> v3
            EditHistory.add(new PastePadHistoryAction(source, padV3))
            setPad(source, jTrapKATEditor.currentKitNumber, jTrapKATEditor.currentPadNumber, padV3)
        }, if (okayToConvert(L.G("Pad"), L.G("V3"), L.G("V4"))) {
            // v3 -> v4
            val padV4 = new model.PadV4(padV3, jTrapKATEditor.currentPadNumber.toByte)
            EditHistory.add(new PastePadHistoryAction(source, padV4))
            setPad(source, jTrapKATEditor.currentKitNumber, jTrapKATEditor.currentPadNumber, padV4)
        })
    }
    private[this] def pastePadV4(source: Component) {
        val padV4Clip = getContentPadV4()
        val padV4 = padV4Clip.pad
        val hhPad = padV4Clip.hhPad
        val padNoWas = (padV4Clip.padNoWas + 1).toByte
        padV4.flags = ((if (hhPad) 0x80 else 0) | padV4.flags).toByte
        if (pasteIfVarious(padV4)) jTrapKATEditor.doV3V4(if (okayToConvert(L.G("Pad"), L.G("V4"), L.G("V3"))) {
            // v4 -> v3
            val padV3 = new model.PadV3(padV4)
            EditHistory.add(new PastePadHistoryAction(source, padV3))
            setPad(source, jTrapKATEditor.currentKitNumber, jTrapKATEditor.currentPadNumber, padV3)
        }, {
            // v4 -> v4
            def padName(p: Byte, l: Byte) = if (p == l) L.G("PastePadV4NotLinked") else L.G("PastePadV4Linked", s"${l}")
            val linkTo = jTrapKATEditor.currentPadV4.linkTo
            val padNoIs = (jTrapKATEditor.currentPadNumber + 1).toByte

            if ((padNoIs != linkTo || padNoWas != padV4.linkTo) && padNoIs != padV4.linkTo) {
                val entries = Seq(L.G("PastePadV4DoNotLink")) ++
                    (if (padNoIs == linkTo) Seq() else Seq(L.G("PastePadV4LinkToPad", s"${linkTo}"))) ++
                    (if (padNoWas == padV4.linkTo) Seq() else Seq(L.G("PastePadV4LinkToPad", s"${padV4.linkTo}")))
                if (Dialog.showOptions(tpnMain, message = L.G("PastePadV4Link", padName(padNoIs, linkTo), padName(padNoWas, padV4.linkTo)),
                    title = L.G("PastePadV4Caption"), entries = entries, initial = 0) match {
                        case Dialog.Result.Yes    => { padV4.linkTo = padNoIs; true } // Do Not Link
                        case Dialog.Result.No     => { padV4.linkTo = linkTo; true } // Link to currentPad.LinkTo
                        case Dialog.Result.Cancel => true // Link to oldPad.LinkTo
                        case _                    => false // Closed the window, so abort
                    }) {
                    EditHistory.add(new PastePadHistoryAction(source, padV4))
                    setPad(source, jTrapKATEditor.currentKitNumber, jTrapKATEditor.currentPadNumber, padV4)
                }
            } else {
                padV4.linkTo = padNoIs
                EditHistory.add(new PastePadHistoryAction(source, padV4))
                setPad(source, jTrapKATEditor.currentKitNumber, jTrapKATEditor.currentPadNumber, padV4)
            }

        })

    }

    private[this] def swapIfVarious(thatKit: model.Kit[model.Pad], thatPadNo: Int): Boolean = {
        val thisKit = jTrapKATEditor.currentAllMemory(jTrapKATEditor.currentKitNumber).asInstanceOf[model.Kit[model.Pad]]

        val incoming = padKitStatus(thisKit, thatKit(thatPadNo))
        val outgoing = padKitStatus(thatKit, jTrapKATEditor.currentPad)

        if (!incoming.forall(t => t._1) || !outgoing.forall(t => t._1)) {
            // At least one of the pads has a value that does not match its kit (for which the kit is not in Various)
            Dialog.showConfirmation(tpnMain,
                L.G("SwapPadOKToVarious", thisKit.kitName, incoming.map(t => t._2).mkString("\n"), thatKit.kitName, outgoing.map(t => t._2).mkString("\n")),
                L.G("SwapPadOKToVariousCaption"),
                Dialog.Options.YesNoCancel, Dialog.Message.Question, null) match {
                    case Dialog.Result.No => {
                        // Retain the kit values
                        incoming.foreach(t => t._3())
                        outgoing.foreach(t => t._3())
                        true //... and then swap
                    }
                    case Dialog.Result.Yes => true //... just swap (go to Various)
                    case _                 => false //... do nothing
                }
        } else true
    }
    def swapPads(source: Component) = clipboardType match {
        case PadSwap => {
            val content = getContentSwapPads()
            if (content.kit != jTrapKATEditor.currentKitNumber || content.pad != jTrapKATEditor.currentPadNumber) {

                // If it's the same kit, we're not changing the isKit-ness, otherwise we have to worry about it for both kits
                if (content.kit == jTrapKATEditor.currentKitNumber || swapIfVarious(jTrapKATEditor.currentAllMemory(content.kit).asInstanceOf[model.Kit[model.Pad]], content.pad)) {
                    val leftKitNo = content.kit
                    val rightKitNo = jTrapKATEditor.currentKitNumber
                    val leftPadNo = content.pad
                    val rightPadNo = jTrapKATEditor.currentPadNumber
                    EditHistory.add(new HistoryAction {
                        val actionName: String = "actionEditSwapPads"
                        def undoAction(): Unit = jTrapKATEditor.swapPads(source, rightKitNo, rightPadNo, leftKitNo, leftPadNo)
                        def redoAction(): Unit = jTrapKATEditor.swapPads(source, leftKitNo, leftPadNo, rightKitNo, rightPadNo)
                    })
                    jTrapKATEditor.swapPads(source, leftKitNo, leftPadNo, rightKitNo, rightPadNo)
                    clipboard.setContents(Empty, this)
                }

            }
        }
        case _ => clipboard.setContents(SwapPads(jTrapKATEditor.currentKitNumber, jTrapKATEditor.currentPadNumber), this)
    }

    // Because we may never want it, use a method to get the kit
    def setKit(source: Component, kitNo: Int, kitName: String, getKit: () => model.Kit[_ <: model.Pad]): Unit = {
        if ((kitNo == jTrapKATEditor.currentKitNumber || frmTrapkatSysexEditor.okayToRenumber(jTrapKATEditor.currentKitNumber + 1, jTrapKATEditor.currentKit.kitName, kitNo + 1, kitName))) {

            jTrapKATEditor.currentAllMemory.update(jTrapKATEditor.currentKitNumber, getKit())

            jTrapKATEditor.publish(new eventX.SelectedKitChanged)
            jTrapKATEditor.kitChangedBy(source)
        }
    }

    def copyKit(source: Component) = clipboard.setContents(jTrapKATEditor.doV3V4(
        CopyKitV3(jTrapKATEditor.currentKitV3, jTrapKATEditor.currentKitNumber.toByte),
        CopyKitV4(jTrapKATEditor.currentKitV4, jTrapKATEditor.currentKitNumber.toByte)), this)

    private[this] class PasteKitHistoryAction(source: Component, kitNo: Int, kit: model.Kit[_ <: model.Pad]) extends HistoryAction {
        val kitNoBefore = jTrapKATEditor.currentKitNumber

        def cloneKit(kit: model.Kit[_ <: model.Pad]) = {
            val stream = new java.io.ByteArrayOutputStream()
            kit.serialize(stream)
            kit.serializeKitName(stream)
            stream.flush()
            val in = new java.io.ByteArrayInputStream(stream.toByteArray())
            stream.close()
            val newKit = kit match {
                case v3: model.KitV3 => new model.KitV3(in)
                case v4: model.KitV4 => new model.KitV4(in)
            }
            newKit.deserializeKitName(in)
            in.close()
            newKit
        }

        val kitBefore = cloneKit(jTrapKATEditor.currentKit)
        val kitNameBefore = kitBefore.kitName
        val kitAfter = cloneKit(kit)
        val kitNameAfter = kitAfter.kitName

        val actionName: String = "actionEditPasteKit"
        def undoAction(): Unit = setKit(source, kitNoBefore, kitNameBefore, () => kitBefore)
        def redoAction(): Unit = setKit(source, kitNo, kitNameAfter, () => kitAfter)
    }

    def pasteKit(source: Component) = clipboard.getAvailableDataFlavors().headOption match {
        case Some(v3) if v3 == dfKitV3 && frmTrapkatSysexEditor.okayToSplat(jTrapKATEditor.currentKit, s"Kit ${jTrapKATEditor.currentKitNumber + 1} (${jTrapKATEditor.currentKit.kitName})") => {
            val kitV3Clip = getContentKitV3()
            jTrapKATEditor.doV3V4({
                // v3 -> v3
                val kitV3 = kitV3Clip.kit
                EditHistory.add(new PasteKitHistoryAction(source, kitV3Clip.kitNoWas, kitV3))
                setKit(source, kitV3Clip.kitNoWas, kitV3Clip.kit.kitName, () => kitV3)
            }, if (okayToConvert(L.G("Kit"), L.G("V3"), L.G("V4"))) {
                // v3 -> v4
                val kitV4 = new model.KitV4(kitV3Clip.kit)
                EditHistory.add(new PasteKitHistoryAction(source, kitV3Clip.kitNoWas, kitV4))
                setKit(source, kitV3Clip.kitNoWas, kitV3Clip.kit.kitName, () => kitV4)
            })
        }
        case Some(v4) if v4 == dfKitV4 && frmTrapkatSysexEditor.okayToSplat(jTrapKATEditor.currentKit, s"Kit ${jTrapKATEditor.currentKitNumber + 1} (${jTrapKATEditor.currentKit.kitName})") => {
            val kitV4Clip = getContentKitV4()
            jTrapKATEditor.doV3V4(if (okayToConvert(L.G("Kit"), L.G("V4"), L.G("V3"))) {
                // v4 -> v3
                val kitV3 = new model.KitV3(kitV4Clip.kit)
                EditHistory.add(new PasteKitHistoryAction(source, kitV4Clip.kitNoWas, kitV3))
                setKit(source, kitV4Clip.kitNoWas, kitV4Clip.kit.kitName, () => kitV3)
            }, {
                // v4 -> v4
                val kitV4 = kitV4Clip.kit
                EditHistory.add(new PasteKitHistoryAction(source, kitV4Clip.kitNoWas, kitV4))
                setKit(source, kitV4Clip.kitNoWas, kitV4Clip.kit.kitName, () => kitV4)
            })

        }
        case otherwise => {}
    }

    def swapKits(source: Component) = clipboardType match {
        case KitSwap => {
            val leftKitNo = getContentSwapKits().kit
            val rightKitNo = jTrapKATEditor.currentKitNumber
            if (leftKitNo != rightKitNo) {
                EditHistory.add(new HistoryAction {
                    val actionName: String = "actionEditSwapKits"
                    def undoAction(): Unit = jTrapKATEditor.swapKits(source, leftKitNo, rightKitNo)
                    def redoAction(): Unit = jTrapKATEditor.swapKits(source, rightKitNo, leftKitNo)
                })
                jTrapKATEditor.swapKits(source, leftKitNo, rightKitNo)
                clipboard.setContents(Empty, this)
            }
        }
        case _ => clipboard.setContents(SwapKits(jTrapKATEditor.currentKitNumber), this)
    }

    listenTo(jTrapKATEditor)
    reactions += {
        case e: eventX.SelectedAllMemoryChanged if (clipboardType == ClipboardType.PadSwap || clipboardType == ClipboardType.KitSwap) => clipboard.setContents(Empty, this)
    }

    if (clipboardType == ClipboardType.PadSwap || clipboardType == ClipboardType.KitSwap) clipboard.setContents(Empty, this)
}
