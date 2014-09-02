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
    private[this] case class CopyPadV3(var pad: model.PadV3) extends Clippable {
        override def dataFlavour = dfPadV3
        private def writeObject(out: java.io.ObjectOutputStream): Unit = pad.serialize(out)
        private def readObject(in: java.io.ObjectInputStream): Unit = pad = new model.PadV3(in)
    }
    private[this] case class CopyPadV4(var pad: model.PadV4) extends Clippable {
        override def dataFlavour = dfPadV4
        private def writeObject(out: java.io.ObjectOutputStream): Unit = pad.serialize(out)
        private def readObject(in: java.io.ObjectInputStream): Unit = pad = new model.PadV4(in)
    }
    private[this] case class SwapPads(kit: Int, pad: Int) extends Clippable { override def dataFlavour = dfSwapPads }
    private[this] case class CopyKitV3(var kit: model.KitV3Dump) extends Clippable {
        override def dataFlavour = dfKitV3
        private def writeObject(out: java.io.ObjectOutputStream): Unit = kit.serialize(out, false)
        private def readObject(in: java.io.ObjectInputStream): Unit = kit = new model.KitV3Dump(in)
    }
    private[this] case class CopyKitV4(var kit: model.KitV4Dump) extends Clippable {
        override def dataFlavour = dfKitV4
        private def writeObject(out: java.io.ObjectOutputStream): Unit = kit.serialize(out, false)
        private def readObject(in: java.io.ObjectInputStream): Unit = kit = new model.KitV4Dump(in)
    }
    private[this] case class SwapKits(kit: Int) extends Clippable { override def dataFlavour = dfSwapKits }

    private[this] val dfPadV3 = new DataFlavor(classOf[CopyPadV3], DataFlavor.javaSerializedObjectMimeType)
    private[this] val dfPadV4 = new DataFlavor(classOf[CopyPadV4], DataFlavor.javaSerializedObjectMimeType)
    private[this] val dfSwapPads = new DataFlavor(classOf[SwapPads], DataFlavor.javaJVMLocalObjectMimeType)
    private[this] val dfKitV3 = new DataFlavor(classOf[CopyKitV3], DataFlavor.javaSerializedObjectMimeType)
    private[this] val dfKitV4 = new DataFlavor(classOf[CopyKitV4], DataFlavor.javaSerializedObjectMimeType)
    private[this] val dfSwapKits = new DataFlavor(classOf[SwapKits], DataFlavor.javaJVMLocalObjectMimeType)

    private[this] def okayToConvert(thing: String, from: String, to: String): Boolean = Dialog.showConfirmation(null,
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
        case Some(null)                                  => { Console.println("Got a null data flavor!"); ClipboardType.NotSet }
        case Some(pad) if pad.equals(dfPadV3)            => ClipboardType.Pad
        case Some(pad) if pad.equals(dfPadV4)            => ClipboardType.Pad
        case Some(padSwap) if padSwap.equals(dfSwapPads) => ClipboardType.PadSwap
        case Some(kit) if kit.equals(dfKitV3)            => ClipboardType.Kit
        case Some(kit) if kit.equals(dfKitV4)            => ClipboardType.Kit
        case Some(kitSwap) if kitSwap.equals(dfSwapKits) => ClipboardType.KitSwap
        case otherwise                                   => ClipboardType.NotSet
    }

    private[this] def getContentPadV3() = clipboard.getContents(this).getTransferData(dfPadV3).asInstanceOf[CopyPadV3].pad
    private[this] def getContentPadV4() = clipboard.getContents(this).getTransferData(dfPadV4).asInstanceOf[CopyPadV4].pad
    private[this] def getContentSwapPads() = clipboard.getContents(this).getTransferData(dfSwapPads).asInstanceOf[SwapPads]
    private[this] def getContentKitV3() = clipboard.getContents(this).getTransferData(dfKitV3).asInstanceOf[CopyKitV3].kit
    private[this] def getContentKitV4() = clipboard.getContents(this).getTransferData(dfKitV4).asInstanceOf[CopyKitV4].kit
    private[this] def getContentSwapKits() = clipboard.getContents(this).getTransferData(dfSwapKits).asInstanceOf[SwapKits]

    def copyPad(source: Component) = clipboard.setContents(jTrapKATEditor.doV3V4(CopyPadV3(jTrapKATEditor.currentPadV3), CopyPadV4(jTrapKATEditor.currentPadV4)), this)

    // TODO: Need to check against current kit to see if it is in "kit mode" for Curve, etc and, if so, whether pasting this
    //       pad needs to switch to various or use the kit setting (prompt)
    def pastePad(source: Component) = clipboard.getAvailableDataFlavors().head match {
        case v3 if v3 == dfPadV3 && frmTrapkatSysexEditor.okayToSplat(jTrapKATEditor.currentPad, s"Pad ${jTrapKATEditor.currentPadNumber + 1}") => jTrapKATEditor.doV3V4(
            jTrapKATEditor.setPadV3(source, getContentPadV3()),
            if (okayToConvert(L.G("Pad"), L.G("V3"), L.G("V4")))
                jTrapKATEditor.setPadV4(source, new model.PadV4(getContentPadV3(), jTrapKATEditor.currentPadNumber.toByte))
        )
        case v4 if v4 == dfPadV4 && frmTrapkatSysexEditor.okayToSplat(jTrapKATEditor.currentPad, s"Pad ${jTrapKATEditor.currentPadNumber + 1}") => jTrapKATEditor.doV3V4(
            if (okayToConvert(L.G("Pad"), L.G("V4"), L.G("V3"))) jTrapKATEditor.setPadV3(source, new model.PadV3(getContentPadV4())),
            {
                val pad = getContentPadV4()
                pad.linkTo = (jTrapKATEditor.currentPadNumber + 1).toByte
                jTrapKATEditor.setPadV4(source, pad)
            }
        )
        case otherwise => {}
    }

    def swapPads(source: Component) = clipboardType match {
        case PadSwap => {
            val content = getContentSwapPads()
            if (content.kit != jTrapKATEditor.currentKitNumber || content.pad != jTrapKATEditor.currentPadNumber) jTrapKATEditor.swapPads(source, content.kit, content.pad)
        }
        case _ => clipboard.setContents(SwapPads(jTrapKATEditor.currentKitNumber, jTrapKATEditor.currentPadNumber), this)
    }

    def copyKit(source: Component) = clipboard.setContents(jTrapKATEditor.doV3V4(
        CopyKitV3(new model.KitV3Dump(jTrapKATEditor.currentKitV3, jTrapKATEditor.currentKitNumber.toByte)),
        CopyKitV4(new model.KitV4Dump(jTrapKATEditor.currentKitV4, jTrapKATEditor.currentKitNumber.toByte))
    ), this)

    def pasteKit(source: Component) = clipboard.getAvailableDataFlavors().head match {
        case v3 if v3 == dfKitV3 && frmTrapkatSysexEditor.okayToSplat(jTrapKATEditor.currentPad, s"Kit ${jTrapKATEditor.currentKitNumber + 1} (${jTrapKATEditor.currentKit.kitName})") => jTrapKATEditor.doV3V4(
            jTrapKATEditor.setKitV3(source, getContentKitV3().self),
            if (okayToConvert(L.G("Kit"), L.G("V3"), L.G("V4")))
                jTrapKATEditor.setKitV4(source, new model.KitV4(getContentKitV3().self))
        )
        case v4 if v4 == dfKitV4 && frmTrapkatSysexEditor.okayToSplat(jTrapKATEditor.currentPad, s"Kit ${jTrapKATEditor.currentKitNumber + 1} (${jTrapKATEditor.currentKit.kitName})") => jTrapKATEditor.doV3V4(
            if (okayToConvert(L.G("Kit"), L.G("V4"), L.G("V3")))
                jTrapKATEditor.setKitV3(source, new model.KitV3(getContentKitV4().self)),
            jTrapKATEditor.setKitV4(source, getContentKitV4().self)
        )
        case otherwise => {}
    }

    def swapKits(source: Component) = clipboardType match {
        case KitSwap => {
            val kit = getContentSwapKits().kit
            if (kit != jTrapKATEditor.currentKitNumber) jTrapKATEditor.swapKits(source, kit)
        }
        case _ => clipboard.setContents(SwapKits(jTrapKATEditor.currentKitNumber), this)
    }

    listenTo(jTrapKATEditor)
    reactions += {
        case e: eventX.CurrentAllMemoryChanged if e.source == jTrapKATEditor && (clipboardType == ClipboardType.PadSwap || clipboardType == ClipboardType.KitSwap) => clipboard.setContents(Empty, this)
    }

    if (clipboardType == ClipboardType.PadSwap || clipboardType == ClipboardType.KitSwap) clipboard.setContents(Empty, this)
}
