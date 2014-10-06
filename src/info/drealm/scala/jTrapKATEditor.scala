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

    override def publish(thing: Event) = {
        val source = thing match { case e: SomethingChanged => e.source match { case cp: Component => cp.name; case e if e == jTrapKATEditor => "jTrapKATEditor"; case _ => "{?}" }; case _ => "{?}" }
        Console.println(s"${thing.getClass().getName()} emited from ${source} to ${listeners.count(p => p.isDefinedAt(thing))} listeners.")
        super.publish(thing)
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
        publish(new GlobalChanged(source))
        allMemoryChangedBy(source)
    }
    def pd = currentAllMemory.global.padDynamics(_currentPadNumber)

    private[this] var _currentKitNumber: Int = 0
    def currentKitNumber: Int = _currentKitNumber
    def currentKitNumber_=(value: Int): Unit = {
        _currentKitNumber = value
        publish(new CurrentKitChanged(this))
    }
    def currentKit: model.Kit[_ <: model.Pad] = currentAllMemory(_currentKitNumber)
    def currentKitV3: model.KitV3 = currentAllMemory(_currentKitNumber).asInstanceOf[model.KitV3]
    def currentKitV4: model.KitV4 = currentAllMemory(_currentKitNumber).asInstanceOf[model.KitV4]
    def kitChangedBy(source: Component) = {
        publish(new CurrentKitChanged(source))
        allMemoryChangedBy(source)
    }
    def setKit(kitNo: Int, kitName: String, getKit: => model.Kit[_ <: model.Pad]): Unit = {
        if ((kitNo == _currentKitNumber || frmTrapkatSysexEditor.okayToRenumber(_currentKitNumber + 1, currentKit.kitName, kitNo + 1, kitName))) {
            _currentAllMemory(_currentKitNumber) = getKit
            publish(new CurrentKitChanged(this))
        }
    }
    def swapKits(source: Component, kitOther: Int) {
        doV3V4({
            val thisKit = currentKitV3
            _currentAllMemory(_currentKitNumber) = _currentAllMemory(kitOther).asInstanceOf[model.KitV3]
            _currentAllMemory(kitOther) = thisKit
        }, {
            val thisKit = currentKitV4
            _currentAllMemory(_currentKitNumber) = _currentAllMemory(kitOther).asInstanceOf[model.KitV4]
            _currentAllMemory(kitOther) = thisKit
        })

        // hackery and fakery
        val _wasKit = _currentKitNumber
        try {
            _currentKitNumber = kitOther
            publish(new CurrentKitChanged(this))
        }
        catch { case e: Exception => {} }
        finally { _currentKitNumber = _wasKit }

        publish(new CurrentKitChanged(this))
    }

    def isKitCurve: Boolean = currentKit.forall(p => p.curve == currentKit.curve)
    def toKitCurve(): Unit = currentKit foreach (p => p.curve = currentKit.curve)
    def isKitGate: Boolean = currentKit.forall(p => p.gate == currentKit.gate)
    def toKitGate(): Unit = currentKit foreach (p => p.gate = currentKit.gate)
    def isKitChannel: Boolean = currentKit.forall(p => p.channel == currentKit.channel)
    def toKitChannel(): Unit = currentKit foreach (p => p.channel = currentKit.channel)
    def isKitMinVel: Boolean = currentKit.forall(p => p.minVelocity == currentKit.minVelocity)
    def toKitMinVel(): Unit = currentKit foreach (p => p.minVelocity = currentKit.minVelocity)
    def isKitMaxVel: Boolean = currentKit.forall(p => p.maxVelocity == currentKit.maxVelocity)
    def toKitMaxVel(): Unit = currentKit foreach (p => p.maxVelocity = currentKit.maxVelocity)

    private[this] var _currentSoundControl = 0
    def currentSoundControlNumber = _currentSoundControl
    def currentSoundControlNumber_=(value: Int) = doV3V4({}, if (_currentSoundControl != value) {
        _currentSoundControl = value
        publish(new CurrentSoundControlChanged(this))
    })
    def sc = currentKit.soundControls(_currentSoundControl)

    def scBank: Byte = currentKitV3.bank
    def scBank_=(value: Byte) = currentKitV3.bank = value

    private[this] var _currentPadNumber: Int = 0
    def currentPadNumber: Int = _currentPadNumber
    def currentPadNumber_=(value: Int): Unit = {
        _currentPadNumber = value
        publish(new CurrentPadChanged(this))
    }
    def currentPad: model.Pad = currentKit(_currentPadNumber)
    def currentPadV3: model.PadV3 = currentKitV3(_currentPadNumber)
    def currentPadV4: model.PadV4 = currentKitV4(_currentPadNumber)
    def padChangedBy(source: Component) = {
        publish(new CurrentPadChanged(source))
        kitChangedBy(source)
    }
    def setPadV3(source: Component, pad: model.PadV3) = {
        currentKitV3.update(_currentPadNumber, pad)
        publish(new CurrentPadChanged(this))
        kitChangedBy(source)
    }
    def setPadV4(source: Component, pad: model.PadV4) = {
        currentKitV4.update(_currentPadNumber, pad)
        publish(new CurrentPadChanged(this))
        kitChangedBy(source)
    }

    def swapPads(source: Component, kitOther: Int, padOther: Int) = {

        import scala.language.existentials
        val (thisKit, thisPad, thatKit, thatPad) = doV3V4({
            val _thisPad = currentPadV3
            val _thatKit = _currentAllMemory(kitOther).asInstanceOf[model.KitV3]
            val _thatPad = _thatKit(padOther)
            currentKitV3(_currentPadNumber) = _thatPad
            _thatKit(padOther) = _thisPad
            Tuple4(currentKitV3, _thisPad, _thatKit, _thatPad)
        }, {
            val _thisPad = currentPadV4
            val _thatKit = _currentAllMemory(kitOther).asInstanceOf[model.KitV4]
            val _thatPad = _thatKit(padOther)

            // Handle LinkTo.
            if (_currentKitNumber != kitOther) {
                // If changing kits, lose it.
                _thisPad.linkTo = (padOther + 1).toByte
                _thatPad.linkTo = (_currentPadNumber + 1).toByte
            }
            else {
                // Same kit - retain linked-ness
                // Not linked -> not linked
                // Linked to other -> swap it!
                // Linked to something else -> no change

                if (_thisPad.linkTo == _currentPadNumber + 1)
                    _thisPad.linkTo = (padOther + 1).toByte
                else if (_thisPad.linkTo == padOther + 1)
                    _thisPad.linkTo = (_currentPadNumber + 1).toByte

                if (_thatPad.linkTo == padOther + 1)
                    _thatPad.linkTo = (_currentPadNumber + 1).toByte
                else if (_thatPad.linkTo == _currentPadNumber + 1)
                    _thatPad.linkTo = (padOther + 1).toByte
            }

            currentKitV4(_currentPadNumber) = _thatPad
            _thatKit(padOther) = _thisPad
            Tuple4(currentKitV4, _thisPad, _thatKit, _thatPad)
        })

        def hhPad(kit: model.Kit[_], padNo: Int): Option[Int] = (0 to 3).find(idx => kit.hhPads(idx) == padNo + 1)
        if (((thisPad.flags & 0x80) != (thatPad.flags & 0x80)) || hhPad(thisKit, _currentPadNumber).isDefined != hhPad(thatKit, padOther).isDefined) {
            // One or other of the pads is a hi-hat pads but not both - update hhPads, if needed
            if ((thisPad.flags & 0x80) != 0 || hhPad(thisKit, _currentPadNumber).isDefined) {
                (0 to 3).foreach(_hh => {
                    if (thisKit.hhPads(_hh) == _currentPadNumber + 1)
                        thisKit.hhPads(_hh, (if (thisKit == thatKit) (padOther + 1) else 0).toByte)
                })
                if (_currentKitNumber != kitOther && !hhPad(thatKit, padOther).isDefined)
                    hhPad(thatKit, -1) match {
                        case Some(idx) => thatKit.hhPads(idx, (padOther + 1).toByte)
                        case _         => {}
                    }
            }
            if ((thatPad.flags & 0x80) != 0 || hhPad(thatKit, padOther).isDefined) {
                (0 to 3).foreach(_hh => {
                    if (thatKit.hhPads(_hh) == padOther + 1)
                        thatKit.hhPads(_hh, (if (thisKit == thatKit) (_currentPadNumber + 1) else 0).toByte)
                })
                if (_currentKitNumber != kitOther && !hhPad(thisKit, _currentPadNumber).isDefined)
                    hhPad(thisKit, -1) match {
                        case Some(idx) => thisKit.hhPads(idx, (_currentPadNumber + 1).toByte)
                        case _         => {}
                    }
            }
        }

        // hackery and fakery
        val _wasKit = _currentKitNumber
        val _wasPad = _currentPadNumber
        try {
            _currentKitNumber = kitOther
            _currentPadNumber = padOther
            publish(new CurrentPadChanged(this))
        }
        catch { case e: Exception => {} }
        finally {
            _currentKitNumber = _wasKit
            _currentPadNumber = _wasPad
        }

        publish(new CurrentPadChanged(this))
        kitChangedBy(source)

    }

    def reinitV3(): Unit = if (frmTrapkatSysexEditor.okayToSplat(_currentAllMemory, L.G("AllMemory"))) {
        _currentFile = if (_currentFile.isFile()) _currentFile.getParentFile() else _currentFile
        _currentType = model.DumpType.NotSet
        _currentAllMemory = new model.AllMemoryV3
        _currentSoundControl = 0
        publish(new CurrentAllMemoryChanged(this))
    }

    def reinitV4(): Unit = if (frmTrapkatSysexEditor.okayToSplat(_currentAllMemory, L.G("AllMemory"))) {
        _currentFile = if (_currentFile.isFile()) _currentFile.getParentFile() else _currentFile
        _currentType = model.DumpType.NotSet
        _currentAllMemory = new model.AllMemoryV4
        publish(new CurrentAllMemoryChanged(this))
    }

    def convertToV3(): Unit = if (frmTrapkatSysexEditor.okayToSplat(_currentAllMemory, L.G("AllMemory"))) {
        _currentFile = if (_currentFile.isFile()) _currentFile.getParentFile() else _currentFile
        _currentType = model.DumpType.AllMemory
        _currentAllMemory = new model.AllMemoryV3(_currentAllMemory.asInstanceOf[model.AllMemoryV4])
        publish(new CurrentAllMemoryChanged(this))
    }

    def convertToV4(): Unit = if (frmTrapkatSysexEditor.okayToSplat(_currentAllMemory, L.G("AllMemory"))) {
        _currentFile = if (_currentFile.isFile()) _currentFile.getParentFile() else _currentFile
        _currentType = model.DumpType.AllMemory
        _currentAllMemory = new model.AllMemoryV4(_currentAllMemory.asInstanceOf[model.AllMemoryV3])
        publish(new CurrentAllMemoryChanged(this))
    }

    def openFile(file: java.io.File): Unit = {
        val dump = model.TrapKATSysexDump.fromFile(file)

        if (dump.isInstanceOf[model.AllMemoryV3Dump] || dump.isInstanceOf[model.AllMemoryV4Dump] || _currentType != model.DumpType.AllMemory) {
            _currentFile = file
        }

        dump match {
            case allMemoryV3Dump: model.AllMemoryV3Dump if frmTrapkatSysexEditor.okayToSplat(_currentAllMemory, L.G("AllMemory")) => {
                _currentType = model.DumpType.AllMemory
                _currentAllMemory = allMemoryV3Dump.self
                publish(new CurrentAllMemoryChanged(this))
            }
            case allMemoryV4Dump: model.AllMemoryV4Dump if frmTrapkatSysexEditor.okayToSplat(_currentAllMemory, L.G("AllMemory")) => {
                _currentType = model.DumpType.AllMemory
                _currentAllMemory = allMemoryV4Dump.self
                publish(new CurrentAllMemoryChanged(this))
            }
            case globalV3Dump: model.GlobalV3Dump if frmTrapkatSysexEditor.okayToSplat(_currentAllMemory.global, L.G("Global")) => {
                doV3V4({
                    if (_currentType != model.DumpType.AllMemory) _currentType = model.DumpType.Global
                    _currentAllMemory.global = globalV3Dump.self
                    publish(new GlobalChanged(this))
                }, if (frmTrapkatSysexEditor.okayToConvert(L.G("Global"), L.G("V3"), L.G("V4"))) {
                    if (_currentType != model.DumpType.AllMemory) _currentType = model.DumpType.Global
                    _currentAllMemory.global = new model.GlobalV4(globalV3Dump.self)
                    publish(new GlobalChanged(this))
                })
            }
            case globalV4Dump: model.GlobalV4Dump if frmTrapkatSysexEditor.okayToSplat(_currentAllMemory.global, L.G("Global")) => {
                doV3V4(if (frmTrapkatSysexEditor.okayToConvert(L.G("Global"), L.G("V4"), L.G("V3"))) {
                    if (_currentType != model.DumpType.AllMemory) _currentType = model.DumpType.Global
                    _currentAllMemory.global = new model.GlobalV3(globalV4Dump.self)
                    publish(new GlobalChanged(this))
                }, {
                    if (_currentType != model.DumpType.AllMemory) _currentType = model.DumpType.Global
                    _currentAllMemory.global = globalV4Dump.self
                    publish(new GlobalChanged(this))
                })
            }
            case kitV3Dump: model.KitV3Dump if _currentKitNumber >= 0 && _currentKitNumber < _currentAllMemory.length &&
                frmTrapkatSysexEditor.okayToSplat(_currentAllMemory(_currentKitNumber), s"Kit ${_currentKitNumber + 1} (${currentKit.kitName})") => {

                def doKit(kitNo: Int, kitName: String, getKit: => model.Kit[_ <: model.Pad]): Unit = {
                    if ((kitNo == _currentKitNumber || frmTrapkatSysexEditor.okayToRenumber(_currentKitNumber + 1, currentKit.kitName, kitNo + 1, kitName))) {
                        if (_currentType != model.DumpType.AllMemory) _currentType = model.DumpType.Kit
                        _currentAllMemory(_currentKitNumber) = getKit
                        publish(new CurrentKitChanged(this))
                    }
                }
                doV3V4(
                    doKit(kitV3Dump.auxType, kitV3Dump.self.kitName, kitV3Dump.self),
                    if (frmTrapkatSysexEditor.okayToConvert(L.G("Kit"), L.G("V3"), L.G("V4"))) doKit(kitV3Dump.auxType, kitV3Dump.self.kitName, new model.KitV4(kitV3Dump.self))
                )
            }
            case kitV4Dump: model.KitV4Dump if _currentKitNumber >= 0 && _currentKitNumber < _currentAllMemory.length &&
                frmTrapkatSysexEditor.okayToSplat(_currentAllMemory(_currentKitNumber), f"Kit ${_currentKitNumber} (${_currentAllMemory(_currentKitNumber).kitName})") => {

                def doKit(kitNo: Int, kitName: String, getKit: => model.Kit[_ <: model.Pad]): Unit = {
                    if ((kitNo == _currentKitNumber || frmTrapkatSysexEditor.okayToRenumber(_currentKitNumber + 1, currentKit.kitName, kitNo + 1, kitName))) {
                        if (_currentType != model.DumpType.AllMemory) _currentType = model.DumpType.Kit
                        _currentAllMemory(_currentKitNumber) = getKit
                        publish(new CurrentKitChanged(this))
                    }
                }
                doV3V4(
                    if (frmTrapkatSysexEditor.okayToConvert(L.G("Kit"), L.G("V4"), L.G("V3"))) doKit(kitV4Dump.auxType, kitV4Dump.self.kitName, new model.KitV3(kitV4Dump.self)),
                    doKit(kitV4Dump.auxType, kitV4Dump.self.kitName, kitV4Dump.self)
                )

            }
            case otherwise => {
                // Do nothing - should never happen if the form manages things correctly
            }
        }
    }

    def saveFileAs(thing: model.DumpType.DumpType, file: java.io.File) = {
        thing match {
            case model.DumpType.AllMemory => _save(false, file, _currentAllMemory, thing, new CurrentAllMemoryChanged(this))
            case model.DumpType.Global    => _save(_currentType != thing && _currentAllMemory.global.changed, file, _currentAllMemory.global, thing, new GlobalChanged(this))
            case model.DumpType.Kit       => _save(_currentType != thing && _currentAllMemory(_currentKitNumber).changed, file, _currentAllMemory(_currentKitNumber), thing, new CurrentKitChanged(this))
            case unknown =>
                throw new IllegalArgumentException(s"Do not ask to save ${unknown} as it is unknown.")
        }
    }

    def exitClose() = if (_currentType match {
        case model.DumpType.Global => frmTrapkatSysexEditor.okayToSplat(_currentAllMemory.global, L.G("Global"))
        case model.DumpType.Kit    => frmTrapkatSysexEditor.okayToSplat(currentKit, L.G("Kit"))
        case _                     => frmTrapkatSysexEditor.okayToSplat(_currentAllMemory, L.G("AllMemory"))
    }) quit

    private[this] def _save(notSaving: => Boolean, file: java.io.File, thing: model.DataItem, thingType: model.DumpType.DumpType, thingChanged: => Event) = {
        if (thingType == model.DumpType.Kit)
            model.TrapKATSysexDump.toFile(file, thing, _currentKitNumber, !notSaving)
        else
            model.TrapKATSysexDump.toFile(file, thing, !notSaving)
        if (_currentType == model.DumpType.NotSet || thingType == model.DumpType.AllMemory) {
            _currentFile = file
            _currentType = thingType
        }
        publish(thingChanged)
    }

}
