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
    def doV3V4[T](fV3: => T, fV4: => T): T = _currentAllMemory match {
        case am: model.AllMemoryV3 => fV3
        case am: model.AllMemoryV4 => fV4
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
    var lastSet = scala.collection.mutable.Set.empty[PartialFunction[Event, Unit]]
    def currentKitNumber_=(value: Int): Unit = {
        for (l <- lastSet) if (!listeners.contains(l)) Console.println(s"${l.toString()} is no longer a listener")
        lastSet = for (l <- listeners) yield l
        _currentKitNumber = value
        publish(new SelectedKitChanged)
    }
    def currentKit: model.Kit[_ <: model.Pad] = currentAllMemory(_currentKitNumber)
    def currentKitV3: model.KitV3 = currentAllMemory(_currentKitNumber).asInstanceOf[model.KitV3]
    def currentKitV4: model.KitV4 = currentAllMemory(_currentKitNumber).asInstanceOf[model.KitV4]
    def kitChangedBy(source: Component) = {
        publish(new CurrentKitChanged(source))
        allMemoryChangedBy(source)
    }
    def swapKits(source: Component, leftKitNo: Int, rightKitNo: Int) {
        doV3V4({
            val leftKit = _currentAllMemory(leftKitNo).asInstanceOf[model.KitV3]
            _currentAllMemory(leftKitNo) = _currentAllMemory(rightKitNo).asInstanceOf[model.KitV3]
            _currentAllMemory(rightKitNo) = leftKit
        }, {
            val leftKit = _currentAllMemory(leftKitNo).asInstanceOf[model.KitV4]
            _currentAllMemory(leftKitNo) = _currentAllMemory(rightKitNo).asInstanceOf[model.KitV4]
            _currentAllMemory(rightKitNo) = leftKit
        })

        publish(new SelectedKitChanged)
        kitChangedBy(source)
    }

    private[this] var _currentSoundControl = 0
    def currentSoundControlNumber = _currentSoundControl
    def currentSoundControlNumber_=(value: Int) = doV3V4({}, if (_currentSoundControl != value) { _currentSoundControl = value; publish(new SelectedSoundControlChanged) })
    def sc = currentKit.soundControls(_currentSoundControl)
    def soundControlChangedBy(source: Component) = {
        publish(new CurrentSoundControlChanged(source))
        kitChangedBy(source)
    }

    def scBank: Byte = currentKitV3.bank
    def scBank_=(value: Byte) = currentKitV3.bank = value

    private[this] var _currentPadNumber: Int = 0
    def currentPadNumber: Int = _currentPadNumber
    def currentPadNumber_=(value: Int): Unit = { _currentPadNumber = value; publish(new SelectedPadChanged) }
    def currentPad: model.Pad = currentKit(_currentPadNumber)
    def currentPadV3: model.PadV3 = currentKitV3(_currentPadNumber)
    def currentPadV4: model.PadV4 = currentKitV4(_currentPadNumber)
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
        val (thisKit, thisPad, thatKit, thatPad) = doV3V4({
            val _thatKit = leftKit.asInstanceOf[model.KitV3]
            val _thatPad = _thatKit(leftPadNo)
            val _thisKit = rightKit.asInstanceOf[model.KitV3]
            val _thisPad = _thisKit(rightPadNo)
            _thisKit(rightPadNo) = _thatPad
            _thatKit(leftPadNo) = _thisPad
            Tuple4(_thisKit, _thisPad, _thatKit, _thatPad)
        }, {
            val _thatKit = leftKit.asInstanceOf[model.KitV4]
            val _thatPad = _thatKit(leftPadNo)
            val _thisKit = rightKit.asInstanceOf[model.KitV4]
            val _thisPad = _thisKit(rightPadNo)

            // Handle LinkTo.
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

            _thisKit(rightPadNo) = _thatPad
            _thatKit(leftPadNo) = _thisPad
            Tuple4(_thisKit, _thisPad, _thatKit, _thatPad)
        })

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
        _currentAllMemory = new model.AllMemoryV3
        _currentSoundControl = 0
        publish(new SelectedAllMemoryChanged)
    }

    def reinitV4(): Unit = if (frmTrapkatSysexEditor.okayToSplat(_currentAllMemory, L.G("AllMemory"))) {
        EditHistory.clear()
        _currentFile = if (_currentFile.isFile()) _currentFile.getParentFile() else _currentFile
        _currentType = model.DumpType.NotSet
        _currentAllMemory = new model.AllMemoryV4
        publish(new SelectedAllMemoryChanged)
    }

    def convertToV3(): Unit = if (frmTrapkatSysexEditor.okayToSplat(_currentAllMemory, L.G("AllMemory"))) {
        EditHistory.clear()
        _currentFile = if (_currentFile.isFile()) _currentFile.getParentFile() else _currentFile
        _currentType = model.DumpType.AllMemory
        _currentAllMemory = new model.AllMemoryV3(_currentAllMemory.asInstanceOf[model.AllMemoryV4])
        publish(new SelectedAllMemoryChanged)
    }

    def convertToV4(): Unit = if (frmTrapkatSysexEditor.okayToSplat(_currentAllMemory, L.G("AllMemory"))) {
        EditHistory.clear()
        _currentFile = if (_currentFile.isFile()) _currentFile.getParentFile() else _currentFile
        _currentType = model.DumpType.AllMemory
        _currentAllMemory = new model.AllMemoryV4(_currentAllMemory.asInstanceOf[model.AllMemoryV3])
        publish(new SelectedAllMemoryChanged)
    }

    def openFile(file: java.io.File): Unit = {
        val dump = model.TrapKATSysexDump.fromFile(file)

        if (dump.isInstanceOf[model.AllMemoryV3Dump] || dump.isInstanceOf[model.AllMemoryV4Dump] || _currentType != model.DumpType.AllMemory) {
            _currentFile = file
        }

        dump match {
            case allMemoryV3Dump: model.AllMemoryV3Dump if frmTrapkatSysexEditor.okayToSplat(_currentAllMemory, L.G("AllMemory")) => {
                EditHistory.clear()
                _currentType = model.DumpType.AllMemory
                _currentAllMemory = allMemoryV3Dump.self
                publish(new SelectedAllMemoryChanged)
            }
            case allMemoryV4Dump: model.AllMemoryV4Dump if frmTrapkatSysexEditor.okayToSplat(_currentAllMemory, L.G("AllMemory")) => {
                EditHistory.clear()
                _currentType = model.DumpType.AllMemory
                _currentAllMemory = allMemoryV4Dump.self
                publish(new SelectedAllMemoryChanged)
            }
            case globalV3Dump: model.GlobalV3Dump if frmTrapkatSysexEditor.okayToSplat(_currentAllMemory.global, L.G("Global")) => {
                doV3V4({
                    EditHistory.clear()
                    if (_currentType != model.DumpType.AllMemory) _currentType = model.DumpType.Global
                    _currentAllMemory.global = globalV3Dump.self
                    publish(new SelectedGlobalChanged)
                }, if (frmTrapkatSysexEditor.okayToConvert(L.G("Global"), L.G("V3"), L.G("V4"))) {
                    EditHistory.clear()
                    if (_currentType != model.DumpType.AllMemory) _currentType = model.DumpType.Global
                    _currentAllMemory.global = new model.GlobalV4(globalV3Dump.self)
                    publish(new SelectedGlobalChanged)
                })
            }
            case globalV4Dump: model.GlobalV4Dump if frmTrapkatSysexEditor.okayToSplat(_currentAllMemory.global, L.G("Global")) => {
                doV3V4(if (frmTrapkatSysexEditor.okayToConvert(L.G("Global"), L.G("V4"), L.G("V3"))) {
                    EditHistory.clear()
                    if (_currentType != model.DumpType.AllMemory) _currentType = model.DumpType.Global
                    _currentAllMemory.global = new model.GlobalV3(globalV4Dump.self)
                    publish(new SelectedGlobalChanged)
                }, {
                    EditHistory.clear()
                    if (_currentType != model.DumpType.AllMemory) _currentType = model.DumpType.Global
                    _currentAllMemory.global = globalV4Dump.self
                    publish(new SelectedGlobalChanged)
                })
            }
            case kitV3Dump: model.KitV3Dump if _currentKitNumber >= 0 && _currentKitNumber < _currentAllMemory.length &&
                frmTrapkatSysexEditor.okayToSplat(_currentAllMemory(_currentKitNumber), s"Kit ${_currentKitNumber + 1} (${currentKit.kitName})") => {

                def doKit(kitNo: Int, kitName: String, getKit: => model.Kit[_ <: model.Pad]): Unit = {
                    if ((kitNo == _currentKitNumber || frmTrapkatSysexEditor.okayToRenumber(_currentKitNumber + 1, currentKit.kitName, kitNo + 1, kitName))) {
                        EditHistory.clear()
                        if (_currentType != model.DumpType.AllMemory) _currentType = model.DumpType.Kit
                        _currentAllMemory(_currentKitNumber) = getKit
                        publish(new SelectedKitChanged)
                    }
                }
                doV3V4(
                    doKit(kitV3Dump.auxType, kitV3Dump.self.kitName, kitV3Dump.self),
                    if (frmTrapkatSysexEditor.okayToConvert(L.G("Kit"), L.G("V3"), L.G("V4"))) doKit(kitV3Dump.auxType, kitV3Dump.self.kitName, new model.KitV4(kitV3Dump.self)))
            }
            case kitV4Dump: model.KitV4Dump if _currentKitNumber >= 0 && _currentKitNumber < _currentAllMemory.length &&
                frmTrapkatSysexEditor.okayToSplat(_currentAllMemory(_currentKitNumber), f"Kit ${_currentKitNumber} (${_currentAllMemory(_currentKitNumber).kitName})") => {

                def doKit(kitNo: Int, kitName: String, getKit: => model.Kit[_ <: model.Pad]): Unit = {
                    if ((kitNo == _currentKitNumber || frmTrapkatSysexEditor.okayToRenumber(_currentKitNumber + 1, currentKit.kitName, kitNo + 1, kitName))) {
                        EditHistory.clear()
                        if (_currentType != model.DumpType.AllMemory) _currentType = model.DumpType.Kit
                        _currentAllMemory(_currentKitNumber) = getKit
                        publish(new SelectedKitChanged)
                    }
                }
                doV3V4(
                    if (frmTrapkatSysexEditor.okayToConvert(L.G("Kit"), L.G("V4"), L.G("V3"))) doKit(kitV4Dump.auxType, kitV4Dump.self.kitName, new model.KitV3(kitV4Dump.self)),
                    doKit(kitV4Dump.auxType, kitV4Dump.self.kitName, kitV4Dump.self))

            }
            case otherwise => {
                // Do nothing - should never happen if the form manages things correctly
            }
        }
    }

    def saveFileAs(thing: model.DumpType.DumpType, file: java.io.File) = {
        thing match {
            case model.DumpType.AllMemory => _save(true, file, _currentAllMemory, thing, new SelectedAllMemoryChanged)
            case model.DumpType.Global => _save(_currentType == thing && _currentAllMemory.global.changed, file, _currentAllMemory.global, thing, new SelectedGlobalChanged)
            case model.DumpType.Kit => _save(_currentType == thing && _currentAllMemory(_currentKitNumber).changed, file, _currentAllMemory(_currentKitNumber), thing, new SelectedKitChanged)
            case unknown =>
                throw new IllegalArgumentException(s"Do not ask to save ${unknown} as it is unknown.")
        }
    }

    def exitClose() = if (_currentType match {
        case model.DumpType.Global => frmTrapkatSysexEditor.okayToSplat(_currentAllMemory.global, L.G("Global"))
        case model.DumpType.Kit => frmTrapkatSysexEditor.okayToSplat(currentKit, L.G("Kit"))
        case _ => frmTrapkatSysexEditor.okayToSplat(_currentAllMemory, L.G("AllMemory"))
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
