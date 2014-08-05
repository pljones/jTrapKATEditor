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
import info.drealm.scala.{ jTrapKATEditorPreferences => prefs, Localization => L }

object jTrapKATEditor extends SimpleSwingApplication with Publisher {
    val ui = UIManager.getSystemLookAndFeelClassName()
    UIManager.put("swing.boldMetal", false)
    UIManager.setLookAndFeel(ui)

    private[this] var _currentType: model.DumpType.DumpType = model.DumpType.NotSet
    def currentType = _currentType

    private[this] var _currentFile: java.io.File = new java.io.File(prefs.currentWorkingDirectory.getPath() + "/.")
    def currentFile = _currentFile

    private[this] var _currentAllMemory: model.AllMemory = new model.AllMemoryV4
    def currentAllMemory = _currentAllMemory
    def doV3V4[T](fV3: => T, fV4: => T): T = _currentAllMemory match {
        case am: model.AllMemoryV3 => fV3
        case am: model.AllMemoryV4 => fV4
    }

    private[this] var _currentKitNumber: Int = 0
    def currentKitNumber: Int = _currentKitNumber
    def currentKitNumber_=(value: Int): Unit = {
        _currentKitNumber = value
        publish(new CurrentKitChanged(this))
    }
    def currentKit: model.Kit[_ <: model.Pad] = currentAllMemory(_currentKitNumber)
    def currentKitV3: model.KitV3 = currentAllMemory(_currentKitNumber).asInstanceOf[model.KitV3]
    def currentKitV4: model.KitV4 = currentAllMemory(_currentKitNumber).asInstanceOf[model.KitV4]

    def isKitCurve: Boolean = currentKit.forall(p => p.asInstanceOf[model.Pad].curve == currentKit.curve)
    def isKitGate: Boolean = currentKit.forall(p => p.asInstanceOf[model.Pad].gate == currentKit.gate)
    def fcChanAsChick: Boolean = currentKit.fcChannel >= 16
    def isNoVolume(sc: Int): Boolean = currentKit.soundControls(sc).volume >= 128
    def isNoPrgChg(sc: Int): Boolean = currentKit.soundControls(sc).prgChg == 0
    def isNoBankMSB(sc: Int): Boolean = currentKit.soundControls(sc).bankMSB >= 128
    def isNoBankLSB(sc: Int): Boolean = currentKit.soundControls(sc).bankLSB >= 128
    def isNoBank: Boolean = currentKitV3.bank >= 128

    private[this] var _currentPadNumber: Int = 0
    def currentPadNumber: Int = _currentPadNumber
    def currentPadNumber_=(value: Int): Unit = {
        _currentPadNumber = value
        publish(new CurrentPadChanged(this))
    }
    def currentPad: model.Pad = currentKit(_currentPadNumber)
    def currentPadV3: model.PadV3 = currentKitV3(_currentPadNumber)
    def currentPadV4: model.PadV4 = currentKitV4(_currentPadNumber)

    def reinitV3(): Unit = {
        _currentFile = if (_currentFile.isFile()) _currentFile.getParentFile() else _currentFile
        _currentType = model.DumpType.NotSet
        _currentAllMemory = new model.AllMemoryV3
        Console.println("reinitV3: AllMemoryChanged")
        publish(new CurrentAllMemoryChanged(this))
    }

    def reinitV4(): Unit = {
        _currentFile = if (_currentFile.isFile()) _currentFile.getParentFile() else _currentFile
        _currentType = model.DumpType.NotSet
        _currentAllMemory = new model.AllMemoryV4
        Console.println("reinitV4: AllMemoryChanged")
        publish(new CurrentAllMemoryChanged(this))
    }

    def convertToV3(): Unit = {
        _currentFile = if (_currentFile.isFile()) _currentFile.getParentFile() else _currentFile
        _currentType = model.DumpType.NotSet
        _currentAllMemory = new model.AllMemoryV3(_currentAllMemory.asInstanceOf[model.AllMemoryV4])
        _currentAllMemory.makeChanged()
        Console.println("convertToV3: AllMemoryChanged")
        publish(new CurrentAllMemoryChanged(this))
    }

    def convertToV4(): Unit = {
        _currentFile = if (_currentFile.isFile()) _currentFile.getParentFile() else _currentFile
        _currentType = model.DumpType.NotSet
        _currentAllMemory = new model.AllMemoryV4(_currentAllMemory.asInstanceOf[model.AllMemoryV3])
        _currentAllMemory.makeChanged()
        Console.println("convertToV4: AllMemoryChanged")
        publish(new CurrentAllMemoryChanged(this))
    }

    def openFile(file: java.io.File): Unit = {
        val dump = model.TrapKATSysexDump.fromFile(file)

        if (dump.isInstanceOf[model.AllMemoryV3Dump] || dump.isInstanceOf[model.AllMemoryV4Dump] || _currentType != model.DumpType.AllMemory) {
            _currentFile = file
        }

        dump match {
            case allMemoryV3Dump: model.AllMemoryV3Dump if frmTrapkatSysexEditor.okayToSplat(_currentAllMemory, L.G("AllMemory")) => {
                Console.println("allMemoryV3Dump.self")
                _currentAllMemory = allMemoryV3Dump.self
                _currentType = model.DumpType.AllMemory
                Console.println("openFile allMemoryV3Dump: AllMemoryChanged")
                publish(new CurrentAllMemoryChanged(this))
            }
            case allMemoryV4Dump: model.AllMemoryV4Dump if frmTrapkatSysexEditor.okayToSplat(_currentAllMemory, L.G("AllMemory")) => {
                Console.println("allMemoryV4Dump.self")
                _currentAllMemory = allMemoryV4Dump.self
                _currentType = model.DumpType.AllMemory
                Console.println("openFile allMemoryV4Dump: AllMemoryChanged")
                publish(new CurrentAllMemoryChanged(this))
            }
            case globalV3Dump: model.GlobalV3Dump if frmTrapkatSysexEditor.okayToSplat(_currentAllMemory.global, L.G("Global")) => {
                Console.println("globalV3Dump.self")
                _currentType = model.DumpType.Global
                doV3V4({
                    _currentAllMemory.global = globalV3Dump.self
                    publish(new GlobalChanged(this))
                }, if (frmTrapkatSysexEditor.okayToConvert(L.G("Global"), L.G("V3"), L.G("V4"))) {
                    _currentAllMemory.global = new model.GlobalV4(globalV3Dump.self)
                    publish(new GlobalChanged(this))
                })
            }
            case globalV4Dump: model.GlobalV4Dump if frmTrapkatSysexEditor.okayToSplat(_currentAllMemory.global, L.G("Global")) => {
                Console.println("globalV4Dump.self")
                _currentType = model.DumpType.Global
                doV3V4(if (frmTrapkatSysexEditor.okayToConvert(L.G("Global"), L.G("V4"), L.G("V3"))) {
                    _currentAllMemory.global = new model.GlobalV3(globalV4Dump.self)
                    publish(new GlobalChanged(this))
                }, {
                    _currentAllMemory.global = globalV4Dump.self
                    publish(new GlobalChanged(this))
                })
            }
            case kitV3Dump: model.KitV3Dump if _currentKitNumber >= 0 && _currentKitNumber < _currentAllMemory.length &&
                frmTrapkatSysexEditor.okayToSplat(_currentAllMemory(_currentKitNumber), s"Kit ${_currentKitNumber + 1} (${currentKit.kitName})") => {
                Console.println(s"kitV3Dump.self.kitName: ${kitV3Dump.self.kitName}")

                def doKit(getKit: => model.Kit[_ <: model.Pad]): Unit = {
                    if ((kitV3Dump.auxType == _currentKitNumber || frmTrapkatSysexEditor.okayToRenumber(_currentKitNumber + 1, currentKit.kitName, kitV3Dump.auxType + 1, kitV3Dump.self.kitName))) {
                        _currentAllMemory(_currentKitNumber) = getKit
                        Console.println("Send KitChanged V3")
                        _currentType = model.DumpType.Kit
                        publish(new CurrentKitChanged(this))
                    }
                }
                doV3V4(doKit(kitV3Dump.self), if (frmTrapkatSysexEditor.okayToConvert(L.G("Kit"), L.G("V3"), L.G("V4"))) doKit(new model.KitV4(kitV3Dump.self)))
            }
            case kitV4Dump: model.KitV4Dump if _currentKitNumber >= 0 && _currentKitNumber < _currentAllMemory.length &&
                frmTrapkatSysexEditor.okayToSplat(_currentAllMemory(_currentKitNumber), f"Kit ${_currentKitNumber} (${_currentAllMemory(_currentKitNumber).kitName})") => {
                Console.println(s"kitV4Dump.self.kitName: ${kitV4Dump.self.kitName}")

                def doKit(getKit: => model.Kit[_ <: model.Pad]): Unit = {
                    if ((kitV4Dump.auxType == _currentKitNumber || frmTrapkatSysexEditor.okayToRenumber(_currentKitNumber + 1, currentKit.kitName, kitV4Dump.auxType + 1, kitV4Dump.self.kitName))) {
                        _currentAllMemory(_currentKitNumber) = getKit
                        Console.println("Send KitChanged V3")
                        _currentType = model.DumpType.Kit
                        publish(new CurrentKitChanged(this))
                    }
                }
                doV3V4(if (frmTrapkatSysexEditor.okayToConvert(L.G("Kit"), L.G("V4"), L.G("V3"))) doKit(new model.KitV3(kitV4Dump.self)), doKit(kitV4Dump.self))

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

    private[this] def _save(makeChanged: => Boolean, file: java.io.File, thing: model.DataItem, thingType: model.DumpType.DumpType, thingChanged: => Event) = {
        model.TrapKATSysexDump.toFile(file, thing)
        if (makeChanged) thing.makeChanged()
        Console.println(f"_currentType ${_currentType} | thingType ${thingType}")
        if (_currentType == model.DumpType.NotSet || thingType == model.DumpType.AllMemory) {
            _currentFile = file
            _currentType = thingType
        }
        Console.println("_save thingChanged")
        publish(thingChanged)
    }

    def top = frmTrapkatSysexEditor

    // Need to alert contents of pnKitsPads before frmTrapkatSysexEditor gets instantiated 
    // TODO: make it not so
    val sideEffect = pnKitsPads
    Console.println("Start up AllMemoryChanged")
    publish(new CurrentAllMemoryChanged(this))

}
