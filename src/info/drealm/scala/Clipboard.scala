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

    private[this] def okayToConvert(thing: String, from: String, to: String): Boolean = Dialog.showConfirmation(frmTrapkatSysexEditor.contents(0),
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

    // TODO: Need to check against current kit to see if it is in "kit mode" for Curve, etc and, if so, whether pasting this
    //       pad needs to switch to various or use the kit setting (prompt)
    private[this] def pastePadV3(source: Component) {
        val padV3Clip = getContentPadV3()
        val padV3 = padV3Clip.pad
        val hhPad = padV3Clip.hhPad
        padV3.flags = ((if (hhPad) 0x80 else 0) | padV3.flags).toByte
        jTrapKATEditor.doV3V4({
            // v3 -> v3
            jTrapKATEditor.setPadV3(source, padV3)
        }, {
            // v3 -> v4
            if (okayToConvert(L.G("Pad"), L.G("V3"), L.G("V4")))
                jTrapKATEditor.setPadV4(source, new model.PadV4(padV3, jTrapKATEditor.currentPadNumber.toByte))
        })
    }
    // TODO: Need to check against current kit to see if it is in "kit mode" for Curve, etc and, if so, whether pasting this
    //       pad needs to switch to various or use the kit setting (prompt)
    private[this] def pastePadV4(source: Component) {
        val padV4Clip = getContentPadV4()
        val padV4 = padV4Clip.pad
        val hhPad = padV4Clip.hhPad
        val padNoWas = (padV4Clip.padNoWas + 1).toByte
        padV4.flags = ((if (hhPad) 0x80 else 0) | padV4.flags).toByte
        jTrapKATEditor.doV3V4({
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
                if (Dialog.showOptions(frmTrapkatSysexEditor.contents(0), message = L.G("PastePadV4Link", padName(padNoIs, linkTo), padName(padNoWas, padV4.linkTo)),
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

    // TODO: Need to check against current kit to see if it is in "kit mode" for Curve, etc and, if so, whether pasting this
    //       pad needs to switch to various or use the kit setting (prompt)
    def swapPads(source: Component) = clipboardType match {
        case PadSwap => {
            val content = getContentSwapPads()
            if (content.kit != jTrapKATEditor.currentKitNumber || content.pad != jTrapKATEditor.currentPadNumber) {
                jTrapKATEditor.swapPads(source, content.kit, content.pad)
                clipboard.setContents(Empty, this)
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
