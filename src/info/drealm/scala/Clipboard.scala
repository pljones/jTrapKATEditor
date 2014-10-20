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
        CopyPadV4(jTrapKATEditor.currentPadV4, !jTrapKATEditor.currentKit.hhPadNos((jTrapKATEditor.currentPadNumber + 1).toByte).isEmpty, jTrapKATEditor.currentPadNumber.toByte)
    ), this)

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
        ("MaxVel", () => kit.isKitMaxVel, isPadMaxVelKit _, padToKitMaxVel _)
    ) map (t => {
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
        }
        else true
    }
    private[this] def pastePadV3(source: Component) {
        val padV3Clip = getContentPadV3()
        val padV3 = padV3Clip.pad
        val hhPad = padV3Clip.hhPad
        padV3.flags = ((if (hhPad) 0x80 else 0) | padV3.flags).toByte
        if (pasteIfVarious(padV3)) jTrapKATEditor.doV3V4({
            // v3 -> v3
            jTrapKATEditor.setPadV3(source, padV3)
        }, {
            // v3 -> v4
            if (okayToConvert(L.G("Pad"), L.G("V3"), L.G("V4")))
                jTrapKATEditor.setPadV4(source, new model.PadV4(padV3, jTrapKATEditor.currentPadNumber.toByte))
        })
    }
    private[this] def pastePadV4(source: Component) {
        val padV4Clip = getContentPadV4()
        val padV4 = padV4Clip.pad
        val hhPad = padV4Clip.hhPad
        val padNoWas = (padV4Clip.padNoWas + 1).toByte
        padV4.flags = ((if (hhPad) 0x80 else 0) | padV4.flags).toByte
        if (pasteIfVarious(padV4)) jTrapKATEditor.doV3V4({
            // v4 -> v3
            if (okayToConvert(L.G("Pad"), L.G("V4"), L.G("V3")))
                jTrapKATEditor.setPadV3(source, new model.PadV3(padV4))
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
                    })
                    jTrapKATEditor.setPadV4(source, padV4)
            }
            else {
                padV4.linkTo = padNoIs
                jTrapKATEditor.setPadV4(source, padV4)
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
        }
        else true
    }
    def swapPads(source: Component) = clipboardType match {
        case PadSwap => {
            val content = getContentSwapPads()
            if (content.kit != jTrapKATEditor.currentKitNumber || content.pad != jTrapKATEditor.currentPadNumber) {

                // If it's the same kit, we're not changing the isKit-ness, otherwise we have to worry about it for both kits
                if (content.kit == jTrapKATEditor.currentKitNumber || swapIfVarious(jTrapKATEditor.currentAllMemory(content.kit).asInstanceOf[model.Kit[model.Pad]], content.pad)) {
                    jTrapKATEditor.swapPads(source, content.kit, content.pad)
                    clipboard.setContents(Empty, this)
                }

            }
        }
        case _ => clipboard.setContents(SwapPads(jTrapKATEditor.currentKitNumber, jTrapKATEditor.currentPadNumber), this)
    }

    def copyKit(source: Component) = clipboard.setContents(jTrapKATEditor.doV3V4(
        CopyKitV3(jTrapKATEditor.currentKitV3, jTrapKATEditor.currentKitNumber.toByte),
        CopyKitV4(jTrapKATEditor.currentKitV4, jTrapKATEditor.currentKitNumber.toByte)
    ), this)

    def pasteKit(source: Component) = clipboard.getAvailableDataFlavors().headOption match {
        case Some(v3) if v3 == dfKitV3 && frmTrapkatSysexEditor.okayToSplat(jTrapKATEditor.currentKit, s"Kit ${jTrapKATEditor.currentKitNumber + 1} (${jTrapKATEditor.currentKit.kitName})") => {
            val kitV3Clip = getContentKitV3()
            jTrapKATEditor.doV3V4(
                jTrapKATEditor.setKit(kitV3Clip.kitNoWas, kitV3Clip.kit.kitName, kitV3Clip.kit),
                if (okayToConvert(L.G("Kit"), L.G("V3"), L.G("V4")))
                    jTrapKATEditor.setKit(kitV3Clip.kitNoWas, kitV3Clip.kit.kitName, new model.KitV4(kitV3Clip.kit))
            )
        }
        case Some(v4) if v4 == dfKitV4 && frmTrapkatSysexEditor.okayToSplat(jTrapKATEditor.currentKit, s"Kit ${jTrapKATEditor.currentKitNumber + 1} (${jTrapKATEditor.currentKit.kitName})") => {
            val kitV4Clip = getContentKitV4()
            jTrapKATEditor.doV3V4(
                if (okayToConvert(L.G("Kit"), L.G("V4"), L.G("V3")))
                    jTrapKATEditor.setKit(kitV4Clip.kitNoWas, kitV4Clip.kit.kitName, new model.KitV3(kitV4Clip.kit)),
                jTrapKATEditor.setKit(kitV4Clip.kitNoWas, kitV4Clip.kit.kitName, kitV4Clip.kit)
            )
        }
        case otherwise => {}
    }

    def swapKits(source: Component) = clipboardType match {
        case KitSwap => {
            val kit = getContentSwapKits().kit
            if (kit != jTrapKATEditor.currentKitNumber) {
                jTrapKATEditor.swapKits(source, kit)
                clipboard.setContents(Empty, this)
            }
        }
        case _ => clipboard.setContents(SwapKits(jTrapKATEditor.currentKitNumber), this)
    }

    listenTo(jTrapKATEditor)
    reactions += {
        case e: eventX.CurrentAllMemoryChanged if e.source == jTrapKATEditor && (clipboardType == ClipboardType.PadSwap || clipboardType == ClipboardType.KitSwap) => clipboard.setContents(Empty, this)
    }

    if (clipboardType == ClipboardType.PadSwap || clipboardType == ClipboardType.KitSwap) clipboard.setContents(Empty, this)
}
