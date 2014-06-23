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
    UIManager.put("swing.boldMetal", false);
    UIManager.setLookAndFeel(ui);

    private var _currentType: model.DumpType.DumpType = model.DumpType.NotSet
    def currentType = _currentType

    private var _currentFile: java.io.File = new java.io.File(prefs.currentWorkingDirectory.getPath() + "/.")
    def currentFile = _currentFile

    private var _currentAllMemory: model.AllMemory = new model.AllMemoryV4
    def currentAllMemory = _currentAllMemory
    listenTo(_currentAllMemory)

    private var _currentKit: Int = -1
    def currentKit: model.Kit[_] = if (_currentKit < 0 || _currentKit > _currentAllMemory.length) null else _currentAllMemory(_currentKit)

    class AllMemoryChanged extends Event
    class GlobalChanged extends Event
    class KitChanged extends Event

    def top = frmTrapkatSysexEditor

    def reinitV3: Unit = {
        _currentFile = if (_currentFile.isFile()) _currentFile.getParentFile() else _currentFile
        _currentType = model.DumpType.NotSet
        _currentAllMemory = new model.AllMemoryV3
        publish(new AllMemoryChanged)
    }

    def reinitV4: Unit = {
        _currentFile = if (_currentFile.isFile()) _currentFile.getParentFile() else _currentFile
        _currentType = model.DumpType.NotSet
        _currentAllMemory = new model.AllMemoryV4
        publish(new AllMemoryChanged)
    }

    def openFile(file: java.io.File): Unit = {
        val dump = model.TrapKATSysexDump.fromFile(file)

        if (dump.isInstanceOf[model.AllMemoryV3Dump] || dump.isInstanceOf[model.AllMemoryV4Dump] || _currentType != model.DumpType.AllMemory) {
            _currentFile = file
            _currentType = model.DumpType.AllMemory
        }

        dump match {
            case allMemoryV3Dump: model.AllMemoryV3Dump if frmTrapkatSysexEditor.okayToSplat(_currentAllMemory, L.G("AllMemory")) => {
                _currentAllMemory = allMemoryV3Dump.self
                publish(new AllMemoryChanged)
            }
            case allMemoryV4Dump: model.AllMemoryV4Dump if frmTrapkatSysexEditor.okayToSplat(_currentAllMemory, L.G("AllMemory")) => {
                _currentAllMemory = allMemoryV4Dump.self
                publish(new AllMemoryChanged)
            }
            case globalV3Dump: model.GlobalV3Dump if frmTrapkatSysexEditor.okayToSplat(_currentAllMemory.global, L.G("Global")) => {
                if (_currentAllMemory.isInstanceOf[model.AllMemoryV3]) {
                    _currentAllMemory.global = globalV3Dump.self
                    publish(new GlobalChanged)
                }
                else if (frmTrapkatSysexEditor.okayToConvert(L.G("Global"), L.G("V3"), L.G("V4"))) {
                    _currentAllMemory.global = new model.GlobalV4(globalV3Dump.self)
                    publish(new GlobalChanged)
                }
            }
            case globalV4Dump: model.GlobalV4Dump if frmTrapkatSysexEditor.okayToSplat(_currentAllMemory.global, L.G("Global")) => {
                if (_currentAllMemory.isInstanceOf[model.AllMemoryV4]) {
                    _currentAllMemory.global = globalV4Dump.self
                    publish(new GlobalChanged)
                }
                else if (frmTrapkatSysexEditor.okayToConvert(L.G("Global"), L.G("V4"), L.G("V3"))) {
                    _currentAllMemory.global = new model.GlobalV3(globalV4Dump.self)
                    publish(new GlobalChanged)
                }
            }
            case kitV3Dump: model.KitV3Dump if _currentKit >= 0 && _currentKit < _currentAllMemory.length &&
                frmTrapkatSysexEditor.okayToSplat(_currentAllMemory(_currentKit), f"Kit ${_currentKit} (${_currentAllMemory(_currentKit).kitName})") => {

                if (_currentAllMemory.isInstanceOf[model.AllMemoryV3]) {
                    if (dump.auxType == _currentKit || frmTrapkatSysexEditor.okayToRenumber(_currentKit, _currentAllMemory(_currentKit).kitName, kitV3Dump.auxType, kitV3Dump.self.kitName)) {
                        _currentAllMemory(_currentKit) = kitV3Dump.self
                        publish(new KitChanged)
                    }
                }
                else if (frmTrapkatSysexEditor.okayToConvert(L.G("Kit"), L.G("V3"), L.G("V4"))) {
                    if (dump.auxType == _currentKit || frmTrapkatSysexEditor.okayToRenumber(_currentKit, _currentAllMemory(_currentKit).kitName, kitV3Dump.auxType, kitV3Dump.self.kitName)) {
                        _currentAllMemory(_currentKit) = new model.KitV4(kitV3Dump.self)
                        publish(new KitChanged)
                    }
                }

            }
            case kitV4Dump: model.KitV4Dump if _currentKit >= 0 && _currentKit < _currentAllMemory.length &&
                frmTrapkatSysexEditor.okayToSplat(_currentAllMemory(_currentKit), f"Kit ${_currentKit} (${_currentAllMemory(_currentKit).kitName})") => {

                if (_currentAllMemory.isInstanceOf[model.AllMemoryV4] &&
                    (dump.auxType == _currentKit || frmTrapkatSysexEditor.okayToRenumber(_currentKit, _currentAllMemory(_currentKit).kitName, kitV4Dump.auxType, kitV4Dump.self.kitName))) {
                    _currentAllMemory(_currentKit) = kitV4Dump.self
                    publish(new KitChanged)
                }
                else if (frmTrapkatSysexEditor.okayToConvert(L.G("Kit"), L.G("V4"), L.G("V3")) &&
                    (dump.auxType == _currentKit || frmTrapkatSysexEditor.okayToRenumber(_currentKit, _currentAllMemory(_currentKit).kitName, kitV4Dump.auxType, kitV4Dump.self.kitName))) {
                    _currentAllMemory(_currentKit) = new model.KitV3(kitV4Dump.self)
                    publish(new KitChanged)
                }

            }
            case otherwise => {
                // Do nothing - should never happen if the form manages things correctly
            }
        }
    }

    def saveFileAs(thing: model.DumpType.DumpType, file: java.io.File) = {
        thing match {
            case model.DumpType.AllMemory => _save(false, file, _currentAllMemory, thing, new AllMemoryChanged)
            case model.DumpType.Global    => _save(_currentType != thing && _currentAllMemory.global.changed, file, _currentAllMemory.global, thing, new GlobalChanged)
            case model.DumpType.Kit       => _save(_currentType != thing && _currentAllMemory(_currentKit).changed, file, _currentAllMemory(_currentKit), thing, new KitChanged)
            case unknown =>
                throw new IllegalArgumentException(s"Do not ask to save ${unknown} as it is unknown.")
        }
    }

    def exitClose() = if (frmTrapkatSysexEditor.okayToSplat(_currentAllMemory, L.G("AllMemory"))) quit

    private[this] def _save(makeChanged: Boolean, file: java.io.File, thing: model.DataItem, thingType: model.DumpType.DumpType, thingChanged: Event) = {
        model.TrapKATSysexDump.toFile(file, thing)
        if (makeChanged) thing.makeChanged
        Console.println(f"_currentType ${_currentType} | thingType ${thingType}")
        if (_currentType == model.DumpType.NotSet || thingType == model.DumpType.AllMemory) {
            _currentFile = file
            _currentType = thingType
        }
        publish(thingChanged)
    }
}
