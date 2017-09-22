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

import javax.swing.UIManager
import swing._
import swing.event._
import info.drealm.scala.eventX._
import info.drealm.scala.{ Localization => L }
import info.drealm.scala.prefs.{ Preferences => P }

object jTrapKATEditor extends SimpleSwingApplication with Publisher {
    val ui = UIManager.getSystemLookAndFeelClassName()
    UIManager.put("swing.boldMetal", false)
    UIManager.setLookAndFeel(ui)

    // Use custom preferences manager
    System.setProperty("java.util.prefs.PreferencesFactory", "info.drealm.scala.prefs.FilePreferencesFactory")

    // Now we know how to get the preferences, check to see if there's an update
    updateTool.Checker.dailyCheck()

    def top = frmTrapkatSysexEditor

    override def shutdown(): Unit = {
        prefs.Preferences.userPreferences.flush()
    }

    private[this] var _currentType: model.DumpType.DumpType = model.DumpType.NotSet
    def currentType = _currentType

    private[this] var _currentFile: java.io.File = new java.io.File(P.currentWorkingDirectory.getPath() + "/.")
    def currentFile = _currentFile

    private[this] var _currentAllMemory: model.AllMemory = new model.AllMemoryV4
    def currentAllMemory = _currentAllMemory
    def doV3V4V5[T](fV3: => T, fV4: => T, fV5: => T): T = _currentAllMemory match {
        case am: model.AllMemoryV3 => fV3
        case am: model.AllMemoryV4 => fV4
        case am: model.AllMemoryV5 => fV5
    }
    def allMemoryChangedBy(source: Component) = publish(new CurrentAllMemoryChanged(source))

    def currentGlobal = _currentAllMemory.global
    def globalMemoryChangedBy(source: Component) = {
        publish(new CurrentGlobalChanged(source))
        allMemoryChangedBy(source)
    }
    def pd = currentAllMemory.global.padDynamics(_currentPadNumber)

    private[this] var _currentKitNumber: Int = 0
    def currentKitNumber: Int = _currentKitNumber
    // Urrrrrghhh... well, okay, this "debugging code" appears to fix the problem being debugged...
    val lastSet = scala.collection.mutable.Set.empty[PartialFunction[Event, Unit]]
    def currentKitNumber_=(value: Int): Unit = {
        for (l <- lastSet) if (!listeners.contains(l)) Console.println(s"${l.toString()} is no longer a listener")
        lastSet.clear()
        for (l <- listeners) lastSet.add(l)
        _currentKitNumber = value
        publish(new SelectedKitChanged)
    }
    def currentKit: model.Kit[_ <: model.Pad] = currentAllMemory(_currentKitNumber)
    def kitChangedBy(source: Component) = {
        publish(new CurrentKitChanged(source))
        allMemoryChangedBy(source)
    }
    def swapKits(source: Component, leftKitNo: Int, rightKitNo: Int) {
        val leftKit: model.Kit[_ <: model.Pad] = doV3V4V5(
            _currentAllMemory(leftKitNo).asInstanceOf[model.KitV3],
            _currentAllMemory(leftKitNo).asInstanceOf[model.KitV4],
            _currentAllMemory(leftKitNo).asInstanceOf[model.KitV5])
        val leftKitName = leftKit.kitName
        _currentAllMemory(leftKitNo) = doV3V4V5(
            _currentAllMemory(rightKitNo).asInstanceOf[model.KitV3],
            _currentAllMemory(rightKitNo).asInstanceOf[model.KitV4],
            _currentAllMemory(rightKitNo).asInstanceOf[model.KitV5])
        _currentAllMemory(leftKitNo).kitName = _currentAllMemory(rightKitNo).kitName
        _currentAllMemory(rightKitNo) = leftKit
        _currentAllMemory(rightKitNo).kitName = leftKitName

        publish(new SelectedKitChanged)
        kitChangedBy(source)
    }

    private[this] var _currentSoundControl = 0
    def currentSoundControlNumber = _currentSoundControl
    def currentSoundControlNumber_=(value: Int) = {
        def v3 = {}
        def v4v5 = { if (_currentSoundControl != value) { _currentSoundControl = value; publish(new SelectedSoundControlChanged) } }
        doV3V4V5(v3, v4v5, v4v5)
    }
    def sc = currentKit.soundControls(_currentSoundControl)
    def soundControlChangedBy(source: Component) = {
        publish(new CurrentSoundControlChanged(source))
        kitChangedBy(source)
    }

    def scBank: Byte = currentAllMemory(_currentKitNumber).asInstanceOf[model.KitV3].bank
    def scBank_=(value: Byte) = currentAllMemory(_currentKitNumber).asInstanceOf[model.KitV3].bank = value

    private[this] var _currentPadNumber: Int = 0
    def currentPadNumber: Int = _currentPadNumber
    def currentPadNumber_=(value: Int): Unit = { _currentPadNumber = value; publish(new SelectedPadChanged) }
    def currentPad: model.Pad = currentKit(_currentPadNumber)
    def padChangedBy(source: Component) = {
        publish(new CurrentPadChanged(source))
        kitChangedBy(source)
    }

    def swapPads(source: Component, leftKitNo: Int, leftPadNo: Int, rightKitNo: Int, rightPadNo: Int) = {
        val leftKit = _currentAllMemory(leftKitNo)
        val rightKit = _currentAllMemory(rightKitNo)

        // Handy names for linkTo (V4) and hhPad numbers
        val thisPadNo = (rightPadNo + 1).toByte
        val thatPadNo = (leftPadNo + 1).toByte

        import scala.language.existentials
        def v3 = {
            val _thatKit = leftKit.asInstanceOf[model.KitV3]
            val _thatPad = _thatKit(leftPadNo)
            val _thisKit = rightKit.asInstanceOf[model.KitV3]
            val _thisPad = _thisKit(rightPadNo)
            _thisKit(rightPadNo) = _thatPad
            _thatKit(leftPadNo) = _thisPad
            Tuple4(_thisKit, _thisPad, _thatKit, _thatPad)
        }
        def handleLinkTo(rightKitNo: Int, leftKitNo: Int, _thisPad: model.PadV4, _thatPad: model.PadV4) = {
            if (rightKitNo != leftKitNo) {
                // If changing kits, lose it.
                _thisPad.linkTo = thatPadNo
                _thatPad.linkTo = thisPadNo
            } else {
                // Same kit - retain linked-ness
                // Not linked -> not linked
                // Linked to other -> swap it!
                // Linked to something else -> no change

                if (_thisPad.linkTo == thisPadNo)
                    _thisPad.linkTo = thatPadNo
                else if (_thisPad.linkTo == thatPadNo)
                    _thisPad.linkTo = thisPadNo

                if (_thatPad.linkTo == thatPadNo)
                    _thatPad.linkTo = thisPadNo
                else if (_thatPad.linkTo == thisPadNo)
                    _thatPad.linkTo = thatPadNo
            }
        }
        def v4 = {
            val _thatKit = leftKit.asInstanceOf[model.KitV4]
            val _thatPad = _thatKit(leftPadNo)
            val _thisKit = rightKit.asInstanceOf[model.KitV4]
            val _thisPad = _thisKit(rightPadNo)

            handleLinkTo(rightKitNo, leftKitNo, _thisPad, _thatPad)

            _thisKit(rightPadNo) = _thatPad
            _thatKit(leftPadNo) = _thisPad
            Tuple4(_thisKit, _thisPad, _thatKit, _thatPad)
        }
        def v5 = {
            val _thatKit = leftKit.asInstanceOf[model.KitV5]
            val _thatPad = _thatKit(leftPadNo)
            val _thisKit = rightKit.asInstanceOf[model.KitV5]
            val _thisPad = _thisKit(rightPadNo)

            handleLinkTo(rightKitNo, leftKitNo, _thisPad, _thatPad)

            _thisKit(rightPadNo) = _thatPad
            _thatKit(leftPadNo) = _thisPad
            Tuple4(_thisKit, _thisPad, _thatKit, _thatPad)
        }
        val (thisKit, thisPad, thatKit, thatPad) = doV3V4V5(v3, v4, v5)

        // Remember whether a pad is a hhPad
        val thisPadIsHH = ((thisPad.flags & 0x80) != 0) || !thisKit.hhPadNos(thisPadNo).isEmpty
        val thatPadIsHH = ((thatPad.flags & 0x80) != 0) || !thatKit.hhPadNos(thatPadNo).isEmpty

        // Free up any used hhPad
        thisKit.hhPadNos(thisPadNo).foreach(thisKit.hhPads(_, 0))
        thatKit.hhPadNos(thatPadNo).foreach(thatKit.hhPads(_, 0))

        if (thisPadIsHH) {
            // Make thisPad a hhPad in thatKit
            thatKit.hhPadNos(0).headOption.foreach(thatKit.hhPads(_, thatPadNo))
            thisPad.flags = (0x80 | thisPad.flags).toByte
        }

        if (thatPadIsHH) {
            // Make thatPad a hhPad in thisKit
            thisKit.hhPadNos(0).headOption.foreach(thisKit.hhPads(_, thisPadNo))
            thatPad.flags = (0x80 | thatPad.flags).toByte
        }

        padChangedBy(source)
    }

    def reinitV3(): Unit = if (frmTrapkatSysexEditor.okayToSplat(_currentAllMemory, L.G("AllMemory"))) {
        EditHistory.clear()
        _currentFile = if (_currentFile.isFile()) _currentFile.getParentFile() else _currentFile
        _currentType = model.DumpType.NotSet
        _currentSoundControl = 0
        _currentAllMemory = new model.AllMemoryV3
        publish(new SelectedAllMemoryChanged)
    }

    def reinitV4(): Unit = if (frmTrapkatSysexEditor.okayToSplat(_currentAllMemory, L.G("AllMemory"))) {
        EditHistory.clear()
        _currentFile = if (_currentFile.isFile()) _currentFile.getParentFile() else _currentFile
        _currentType = model.DumpType.NotSet
        _currentAllMemory = new model.AllMemoryV4
        publish(new SelectedAllMemoryChanged)
    }

    def reinitV5(): Unit = if (frmTrapkatSysexEditor.okayToSplat(_currentAllMemory, L.G("AllMemory"))) {
        EditHistory.clear()
        _currentFile = if (_currentFile.isFile()) _currentFile.getParentFile() else _currentFile
        _currentType = model.DumpType.NotSet
        _currentAllMemory = new model.AllMemoryV5
        publish(new SelectedAllMemoryChanged)
    }

    def convertToV3(): Unit = if (frmTrapkatSysexEditor.okayToSplat(_currentAllMemory, L.G("AllMemory"))) {
        EditHistory.clear()
        _currentFile = if (_currentFile.isFile()) _currentFile.getParentFile() else _currentFile
        _currentType = model.DumpType.AllMemory
        _currentAllMemory = doV3V4V5(_currentAllMemory.asInstanceOf[model.AllMemoryV3], new model.AllMemoryV3(_currentAllMemory.asInstanceOf[model.AllMemoryV4]), new model.AllMemoryV3(_currentAllMemory.asInstanceOf[model.AllMemoryV5]))
        publish(new SelectedAllMemoryChanged)
    }

    def convertToV4(): Unit = if (frmTrapkatSysexEditor.okayToSplat(_currentAllMemory, L.G("AllMemory"))) {
        EditHistory.clear()
        _currentFile = if (_currentFile.isFile()) _currentFile.getParentFile() else _currentFile
        _currentType = model.DumpType.AllMemory
        _currentAllMemory = doV3V4V5(new model.AllMemoryV4(_currentAllMemory.asInstanceOf[model.AllMemoryV3]), _currentAllMemory.asInstanceOf[model.AllMemoryV4], new model.AllMemoryV4(_currentAllMemory.asInstanceOf[model.AllMemoryV5]))
        publish(new SelectedAllMemoryChanged)
    }

    def convertToV5(): Unit = if (frmTrapkatSysexEditor.okayToSplat(_currentAllMemory, L.G("AllMemory"))) {
        EditHistory.clear()
        _currentFile = if (_currentFile.isFile()) _currentFile.getParentFile() else _currentFile
        _currentType = model.DumpType.AllMemory
        _currentAllMemory = doV3V4V5(new model.AllMemoryV5(_currentAllMemory.asInstanceOf[model.AllMemoryV3]), new model.AllMemoryV5(_currentAllMemory.asInstanceOf[model.AllMemoryV4]), _currentAllMemory.asInstanceOf[model.AllMemoryV5])
        publish(new SelectedAllMemoryChanged)
    }

    def openFile(file: java.io.File): Unit = {
        val dump = model.TrapKATSysexDump.fromFile(file)

        if (dump.isInstanceOf[model.AllMemoryV3Dump] || dump.isInstanceOf[model.AllMemoryV4Dump] || dump.isInstanceOf[model.AllMemoryV5Dump] || _currentType != model.DumpType.AllMemory) {
            _currentFile = file
        }

        def openAllMemoryDump(_getAllMemeory: => model.AllMemory) = if (frmTrapkatSysexEditor.okayToSplat(_currentAllMemory, L.G("AllMemory"))) {
            EditHistory.clear()
            _currentType = model.DumpType.AllMemory
            _currentAllMemory = _getAllMemeory
            publish(new SelectedAllMemoryChanged)
        }
        def openGlobalDump(_okCnv: => Boolean, _getGlobal: => model.Global[_ <: model.Pad]) = if (frmTrapkatSysexEditor.okayToSplat(_currentAllMemory.global, L.G("Global"))) {
            if (_okCnv) {
                EditHistory.clear()
                if (_currentType != model.DumpType.AllMemory) _currentType = model.DumpType.Global
                _currentAllMemory.global = _getGlobal
                publish(new SelectedGlobalChanged)
            }
        }
        def openKitDump(_okCnv: => Boolean, _kitNo: Int, _kitName: String, _getKit: => model.Kit[_ <: model.Pad]) = if (_currentKitNumber >= 0 && _currentKitNumber < _currentAllMemory.length &&
            frmTrapkatSysexEditor.okayToSplat(_currentAllMemory(_currentKitNumber), s"Kit ${_currentKitNumber + 1} (${currentKit.kitName})")) {
            if (_okCnv) {
                if ((_kitNo == _currentKitNumber || frmTrapkatSysexEditor.okayToRenumber(_currentKitNumber + 1, currentKit.kitName, _kitNo + 1, _kitName))) {
                    EditHistory.clear()
                    if (_currentType != model.DumpType.AllMemory) _currentType = model.DumpType.Kit
                    _currentAllMemory(_currentKitNumber) = _getKit
                    _currentAllMemory(_currentKitNumber).kitName = _kitName
                    publish(new SelectedKitChanged)
                }
            }
        }
        dump match {
            case allMemoryV3Dump: model.AllMemoryV3Dump => openAllMemoryDump(allMemoryV3Dump.self)
            case allMemoryV4Dump: model.AllMemoryV4Dump => openAllMemoryDump(allMemoryV4Dump.self)
            case allMemoryV5Dump: model.AllMemoryV5Dump => openAllMemoryDump(allMemoryV5Dump.self)
            case globalV3Dump: model.GlobalV3Dump => openGlobalDump(
                doV3V4V5(
                    true,
                    frmTrapkatSysexEditor.okayToConvert(L.G("Global"), L.G("V3"), L.G("V4")),
                    frmTrapkatSysexEditor.okayToConvert(L.G("Global"), L.G("V3"), L.G("V5"))),
                doV3V4V5(globalV3Dump.self, new model.GlobalV4(globalV3Dump.self), new model.GlobalV5(globalV3Dump.self)))
            case globalV4Dump: model.GlobalV4Dump => openGlobalDump(
                doV3V4V5(
                    frmTrapkatSysexEditor.okayToConvert(L.G("Global"), L.G("V4"), L.G("V3")),
                    true,
                    frmTrapkatSysexEditor.okayToConvert(L.G("Global"), L.G("V4"), L.G("V5"))),
                doV3V4V5(new model.GlobalV3(globalV4Dump.self), globalV4Dump.self, new model.GlobalV5(globalV4Dump.self)))
            case globalV5Dump: model.GlobalV5Dump => openGlobalDump(
                doV3V4V5(
                    frmTrapkatSysexEditor.okayToConvert(L.G("Global"), L.G("V5"), L.G("V3")),
                    frmTrapkatSysexEditor.okayToConvert(L.G("Global"), L.G("V5"), L.G("V4")),
                    true),
                doV3V4V5(new model.GlobalV3(globalV5Dump.self), new model.GlobalV4(globalV5Dump.self), globalV5Dump.self))
            case kitV3Dump: model.KitV3Dump => openKitDump(
                doV3V4V5(
                    true,
                    frmTrapkatSysexEditor.okayToConvert(L.G("Kit"), L.G("V3"), L.G("V4")),
                    frmTrapkatSysexEditor.okayToConvert(L.G("Kit"), L.G("V3"), L.G("V5"))), kitV3Dump.auxType, kitV3Dump.self.kitName,
                doV3V4V5(kitV3Dump.self, new model.KitV4(kitV3Dump.self), new model.KitV5(kitV3Dump.self)))
            case kitV4Dump: model.KitV4Dump => openKitDump(
                doV3V4V5(
                    frmTrapkatSysexEditor.okayToConvert(L.G("Kit"), L.G("V4"), L.G("V3")),
                    true,
                    frmTrapkatSysexEditor.okayToConvert(L.G("Kit"), L.G("V4"), L.G("V5"))), kitV4Dump.auxType, kitV4Dump.self.kitName,
                doV3V4V5(new model.KitV3(kitV4Dump.self), kitV4Dump.self, new model.KitV5(kitV4Dump.self)))
            case kitV5Dump: model.KitV5Dump => openKitDump(
                doV3V4V5(
                    frmTrapkatSysexEditor.okayToConvert(L.G("Kit"), L.G("V5"), L.G("V3")),
                    frmTrapkatSysexEditor.okayToConvert(L.G("Kit"), L.G("V5"), L.G("V4")),
                    true), kitV5Dump.auxType, kitV5Dump.self.kitName,
                doV3V4V5(new model.KitV3(kitV5Dump.self), new model.KitV4(kitV5Dump.self), kitV5Dump.self))
            case otherwise => {
                // Do nothing - should never happen if the form manages things correctly
            }
        }
    }

    def saveFileAs(thing: model.DumpType.DumpType, file: java.io.File) = {
        thing match {
            case model.DumpType.AllMemory => _save(true, file, _currentAllMemory, thing, new SelectedAllMemoryChanged)
            case model.DumpType.Global    => _save(_currentType == thing && _currentAllMemory.global.changed, file, _currentAllMemory.global, thing, new SelectedGlobalChanged)
            case model.DumpType.Kit       => _save(_currentType == thing && _currentAllMemory(_currentKitNumber).changed, file, _currentAllMemory(_currentKitNumber), thing, new SelectedKitChanged)
            case unknown =>
                throw new IllegalArgumentException(s"Do not ask to save ${unknown} as it is unknown.")
        }
    }

    def exitClose() = if (_currentType match {
        case model.DumpType.Global => frmTrapkatSysexEditor.okayToSplat(_currentAllMemory.global, L.G("Global"))
        case model.DumpType.Kit    => frmTrapkatSysexEditor.okayToSplat(_currentAllMemory(_currentKitNumber), s"Kit ${_currentKitNumber + 1} (${currentKit.kitName})")
        case _                     => frmTrapkatSysexEditor.okayToSplat(_currentAllMemory, L.G("AllMemory"))
    }) quit

    private[this] def _save(saving: => Boolean, file: java.io.File, thing: model.DataItem, thingType: model.DumpType.DumpType, thingChanged: => Event) = {
        if (thingType == model.DumpType.Kit)
            model.TrapKATSysexDump.toFile(file, thing, _currentKitNumber, saving)
        else
            model.TrapKATSysexDump.toFile(file, thing, saving)
        if (_currentType == model.DumpType.NotSet || thingType == model.DumpType.AllMemory) {
            _currentFile = file
            _currentType = thingType
        }
        if (saving)
            publish(thingChanged)
    }

}
