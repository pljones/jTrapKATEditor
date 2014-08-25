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

import java.io._
import collection.mutable

abstract class Global[TPad <: Pad] protected (p: TPad) extends DataItem {
    // 21 bytes
    private[this] var _beeperStatus: Byte = 1
    private[this] var _bcFunction: Byte = 0
    private[this] var _chokeFunction: Byte = 0
    private[this] var _fcClosedRegion: Byte = 0
    private[this] var _fcPolarity: Byte = 0
    private[this] var _bcPolarity: Byte = 0
    private[this] var _bcLowLevel: Byte = 0
    private[this] var _bcHighLevel: Byte = 0
    private[this] var _fcLowLevel: Byte = 0
    private[this] var _fcHighLevel: Byte = 0
    private[this] var _fcVelocityLevel: Byte = 0
    private[this] var _fcWaitModeLevel: Byte = 0
    private[this] var _instrumentID: Byte = 0
    private[this] var _kitNumber: Byte = 1
    private[this] var _kitNumberUser: Byte = 1
    private[this] var _kitNumberDemo: Byte = 1
    private[this] var _motifNumber: Byte = 0
    private[this] var _motifNumberPerc: Byte = 0
    private[this] var _motifNumberMel: Byte = 0
    private[this] var _midiMergeStatus: Byte = 0
    private[this] var _fcOpenRegion: Byte = 0

    protected[Global] val _padLevels: Array[Byte] = new Array[Byte](50)

    // 10 bytes
    private[this] var _trigGain: Byte = 0 // (0 - 3)
    private[this] var _prgChgRcvChn: Byte = 17 // Program change receive channel (1-16; 17=off)
    private[this] var _displayAngle: Byte = 0
    private[this] var _playMode: Byte = 0 // (0 -> demo, 1 -> user, 2 -> KAT)
    private[this] var _grooveVol: Byte = 0
    private[this] var _grooveStatus: Byte = 0 // (1 -> grooves enables)
    private[this] var _fcSplashEase: Byte = 2 // Something like 1 = Off, 2->11 = 1->10?
    private[this] var _noteNamesStatus: Byte = 0

    private[this] val _ttPadData: TPad = p

    // 5 bytes
    private[this] var _hatNoteGate: Byte = 0 // HAT NOTE gate time index
    private[this] var _grooveAutoOff: Byte = 0 // enabled if > 0
    private[this] var _kitNumberKAT: Byte = 1 // 1 to 6
    private[this] var _ttMeter: Byte = 0 // Tap tempo meter (quarter, half, eighth, etc.)
    private[this] var _hearSoundStatus: Byte = 0 // (1 -> on)

    protected val _unused1: Array[Byte]

    protected[Global] val _thresholdManual: Array[Byte] = new Array[Byte](25)
    protected[Global] val _unused2: Array[Byte] = new Array[Byte](231)
    protected[Global] val _internalMargin: Array[Byte] = new Array[Byte](25)
    protected[Global] val _unused3: Array[Byte] = new Array[Byte](231)
    protected[Global] val _userMargin: Array[Byte] = new Array[Byte](25)
    protected[Global] val _unused4: Array[Byte] = new Array[Byte](231)
    protected[Global] val _thresholdActual: Array[Byte] = new Array[Byte](25)

    private[this] val _padDynamics: PadDynamicsContainer = new PadDynamicsContainer(_padLevels, _userMargin, _internalMargin, _thresholdManual, _thresholdActual)

    protected def from(global: Global[_]): Unit = {
        _beeperStatus = global.beeperStatus
        _bcFunction = global.bcFunction
        _chokeFunction = global.chokeFunction
        _fcClosedRegion = global.fcClosedRegion
        _fcPolarity = global.fcPolarity
        _bcPolarity = global.bcPolarity
        _bcLowLevel = global.bcLowLevel
        _bcHighLevel = global.bcHighLevel
        _fcLowLevel = global.fcLowLevel
        _fcHighLevel = global.fcHighLevel
        _fcVelocityLevel = global.fcVelocityLevel
        _fcWaitModeLevel = global.fcWaitModeLevel
        _instrumentID = global.instrumentID
        _kitNumber = global.kitNumber
        _kitNumberUser = global.kitNumberUser
        _kitNumberDemo = global.kitNumberDemo
        _motifNumber = global.motifNumber
        _motifNumberPerc = global.motifNumberPerc
        _motifNumberMel = global.motifNumberMel
        _midiMergeStatus = global.midiMergeStatus
        _fcOpenRegion = global.fcOpenRegion
        global._padLevels.copyToArray(_padLevels)
        _trigGain = global.trigGain
        _prgChgRcvChn = global.prgChgRcvChn
        _displayAngle = global.displayAngle
        _playMode = global.playMode
        _grooveVol = global.grooveVol
        _grooveStatus = global.grooveStatus
        _fcSplashEase = global.fcSplashEase
        _noteNamesStatus = global.noteNamesStatus
        //_ttPadData is left to the concrete class
        _hatNoteGate = global.hatNoteGate
        _grooveAutoOff = global.grooveAutoOff
        _kitNumberKAT = global.kitNumberKAT
        _ttMeter = global.ttMeter
        _hearSoundStatus = global.hearSoundStatus
        //_unused1 is left to the concrete class
        global._thresholdManual.copyToArray(_thresholdManual)
        global._unused2.copyToArray(_unused2)
        global._internalMargin.copyToArray(_internalMargin)
        global._unused3.copyToArray(_unused3)
        global._userMargin.copyToArray(_userMargin)
        global._unused4.copyToArray(_unused4)
        global._thresholdActual.copyToArray(_thresholdActual)
    }

    def deserialize(in: FileInputStream): Unit = {
        _beeperStatus = in.read().toByte
        _bcFunction = in.read().toByte
        _chokeFunction = in.read().toByte
        _fcClosedRegion = in.read().toByte
        _fcPolarity = in.read().toByte
        _bcPolarity = in.read().toByte
        _bcLowLevel = in.read().toByte
        _bcHighLevel = in.read().toByte
        _fcLowLevel = in.read().toByte
        _fcHighLevel = in.read().toByte
        _fcVelocityLevel = in.read().toByte
        _fcWaitModeLevel = in.read().toByte
        _instrumentID = in.read().toByte
        _kitNumber = in.read().toByte
        _kitNumberUser = in.read().toByte
        _kitNumberDemo = in.read().toByte
        _motifNumber = in.read().toByte
        _motifNumberPerc = in.read().toByte
        _motifNumberMel = in.read().toByte
        _midiMergeStatus = in.read().toByte
        _fcOpenRegion = in.read().toByte

        in.read(_padLevels)

        _trigGain = in.read().toByte
        _prgChgRcvChn = in.read().toByte
        _displayAngle = in.read().toByte
        _playMode = in.read().toByte
        _grooveVol = in.read().toByte
        _grooveStatus = in.read().toByte
        _fcSplashEase = in.read().toByte
        _noteNamesStatus = in.read().toByte

        _ttPadData.deserialize(in)

        _hatNoteGate = in.read().toByte
        _grooveAutoOff = in.read().toByte
        _kitNumberKAT = in.read().toByte
        _ttMeter = in.read().toByte
        _hearSoundStatus = in.read().toByte

        in.read(_unused1)
        in.read(_thresholdManual)
        in.read(_unused2)
        in.read(_internalMargin)
        in.read(_unused3)
        in.read(_userMargin)
        in.read(_unused4)
        in.read(_thresholdActual)
    }
    def serialize(out: FileOutputStream, saving: Boolean): Unit = {
        out.write(_beeperStatus)
        out.write(_bcFunction)
        out.write(_chokeFunction)
        out.write(_fcClosedRegion)
        out.write(_fcPolarity)
        out.write(_bcPolarity)
        out.write(_bcLowLevel)
        out.write(_bcHighLevel)
        out.write(_fcLowLevel)
        out.write(_fcHighLevel)
        out.write(_fcVelocityLevel)
        out.write(_fcWaitModeLevel)
        out.write(_instrumentID)
        out.write(_kitNumber)
        out.write(_kitNumberUser)
        out.write(_kitNumberDemo)
        out.write(_motifNumber)
        out.write(_motifNumberPerc)
        out.write(_motifNumberMel)
        out.write(_midiMergeStatus)
        out.write(_fcOpenRegion)
        
        out.write(_padLevels)

        out.write(_trigGain)
        out.write(_prgChgRcvChn)
        out.write(_displayAngle)
        out.write(_playMode)
        out.write(_grooveVol)
        out.write(_grooveStatus)
        out.write(_fcSplashEase)
        out.write(_noteNamesStatus)

        if (saving) _ttPadData.save(out) else _ttPadData.serialize(out, saving)

        out.write(_hatNoteGate)
        out.write(_grooveAutoOff)
        out.write(_kitNumberKAT)
        out.write(_ttMeter)
        out.write(_hearSoundStatus)

        out.write(_unused1)
        out.write(_thresholdManual)
        out.write(_unused2)
        out.write(_internalMargin)
        out.write(_unused3)
        out.write(_userMargin)
        out.write(_unused4)
        out.write(_thresholdActual)
    }

    def beeperStatus: Byte = _beeperStatus
    def beeperStatus_=(value: Byte): Unit = if (_beeperStatus != value) update(_beeperStatus = value) else {}
    def bcFunction: Byte = _bcFunction
    def bcFunction_=(value: Byte): Unit = if (_bcFunction != value) update(_bcFunction = value) else {}
    def chokeFunction: Byte = _chokeFunction
    def chokeFunction_=(value: Byte): Unit = if (_chokeFunction != value) update(_chokeFunction = value) else {}
    def fcClosedRegion: Byte = _fcClosedRegion
    def fcClosedRegion_=(value: Byte): Unit = if (_fcClosedRegion != value) update(_fcClosedRegion = value) else {}
    def fcPolarity: Byte = _fcPolarity
    def fcPolarity_=(value: Byte): Unit = if (_fcPolarity != value) update(_fcPolarity = value) else {}
    def bcPolarity: Byte = _bcPolarity
    def bcPolarity_=(value: Byte): Unit = if (_bcPolarity != value) update(_bcPolarity = value) else {}
    def bcLowLevel: Byte = _bcLowLevel
    def bcLowLevel_=(value: Byte): Unit = if (_bcLowLevel != value) update(_bcLowLevel = value) else {}
    def bcHighLevel: Byte = _bcHighLevel
    def bcHighLevel_=(value: Byte): Unit = if (_bcHighLevel != value) update(_bcHighLevel = value) else {}
    def fcLowLevel: Byte = _fcLowLevel
    def fcLowLevel_=(value: Byte): Unit = if (_fcLowLevel != value) update(_fcLowLevel = value) else {}
    def fcHighLevel: Byte = _fcHighLevel
    def fcHighLevel_=(value: Byte): Unit = if (_fcHighLevel != value) update(_fcHighLevel = value) else {}
    def fcVelocityLevel: Byte = _fcVelocityLevel
    def fcVelocityLevel_=(value: Byte): Unit = if (_fcVelocityLevel != value) update(_fcVelocityLevel = value) else {}
    def fcWaitModeLevel: Byte = _fcWaitModeLevel
    def fcWaitModeLevel_=(value: Byte): Unit = if (_fcWaitModeLevel != value) update(_fcWaitModeLevel = value) else {}
    def instrumentID: Byte = _instrumentID
    def instrumentID_=(value: Byte): Unit = if (_instrumentID != value) update(_instrumentID = value) else {}
    def kitNumber: Byte = _kitNumber
    def kitNumber_=(value: Byte): Unit = if (_kitNumber != value) update(_kitNumber = value) else {}
    def kitNumberUser: Byte = _kitNumberUser
    def kitNumberUser_=(value: Byte): Unit = if (_kitNumberUser != value) update(_kitNumberUser = value) else {}
    def kitNumberDemo: Byte = _kitNumberDemo
    def kitNumberDemo_=(value: Byte): Unit = if (_kitNumberDemo != value) update(_kitNumberDemo = value) else {}
    def motifNumber: Byte = _motifNumber
    def motifNumber_=(value: Byte): Unit = if (_motifNumber != value) update(_motifNumber = value) else {}
    def motifNumberPerc: Byte = _motifNumberPerc
    def motifNumberPerc_=(value: Byte): Unit = if (_motifNumberPerc != value) update(_motifNumberPerc = value) else {}
    def motifNumberMel: Byte = _motifNumberMel
    def motifNumberMel_=(value: Byte): Unit = if (_motifNumberMel != value) update(_motifNumberMel = value) else {}
    def midiMergeStatus: Byte = _midiMergeStatus
    def midiMergeStatus_=(value: Byte): Unit = if (_midiMergeStatus != value) update(_midiMergeStatus = value) else {}
    def fcOpenRegion: Byte = _fcOpenRegion
    def fcOpenRegion_=(value: Byte): Unit = if (_fcOpenRegion != value) update(_fcOpenRegion = value) else {}
    def trigGain: Byte = _trigGain
    def trigGain_=(value: Byte): Unit = if (_trigGain != value) update(_trigGain = value) else {}
    def prgChgRcvChn: Byte = _prgChgRcvChn
    def prgChgRcvChn_=(value: Byte): Unit = if (_prgChgRcvChn != value) update(_prgChgRcvChn = value) else {}
    def displayAngle: Byte = _displayAngle
    def displayAngle_=(value: Byte): Unit = if (_displayAngle != value) update(_displayAngle = value) else {}
    def playMode: Byte = _playMode
    def playMode_=(value: Byte): Unit = if (_playMode != value) update(_playMode = value) else {}
    def grooveVol: Byte = _grooveVol
    def grooveVol_=(value: Byte): Unit = if (_grooveVol != value) update(_grooveVol = value) else {}
    def grooveStatus: Byte = _grooveStatus
    def grooveStatus_=(value: Byte): Unit = if (_grooveStatus != value) update(_grooveStatus = value) else {}
    def fcSplashEase: Byte = _fcSplashEase
    def fcSplashEase_=(value: Byte): Unit = if (_fcSplashEase != value) update(_fcSplashEase = value) else {}
    def noteNamesStatus: Byte = _noteNamesStatus
    def noteNamesStatus_=(value: Byte): Unit = if (_noteNamesStatus != value) update(_noteNamesStatus = value) else {}
    def hatNoteGate: Byte = _hatNoteGate
    def hatNoteGate_=(value: Byte): Unit = if (_hatNoteGate != value) update(_hatNoteGate = value) else {}
    def grooveAutoOff: Byte = _grooveAutoOff
    def grooveAutoOff_=(value: Byte): Unit = if (_grooveAutoOff != value) update(_grooveAutoOff = value) else {}
    def kitNumberKAT: Byte = _kitNumberKAT
    def kitNumberKAT_=(value: Byte): Unit = if (_kitNumberKAT != value) update(_kitNumberKAT = value) else {}
    def ttMeter: Byte = _ttMeter
    def ttMeter_=(value: Byte): Unit = if (_ttMeter != value) update(_ttMeter = value) else {}
    def hearSoundStatus: Byte = _hearSoundStatus
    def hearSoundStatus_=(value: Byte): Unit = if (_hearSoundStatus != value) update(_hearSoundStatus = value) else {}

    def ttPadData: TPad = _ttPadData

    def padDynamics: Seq[PadDynamics] = _padDynamics

    def changed = _changed || _ttPadData.changed || _padDynamics.changed
}

class GlobalV3 private(p: PadV3) extends Global[PadV3](p) {
    def this() = this(new PadV3)
    def this(in: FileInputStream) = {
        this()
        deserialize(in)
    }
    def this(globalV4: GlobalV4) = {
        this(new PadV3(globalV4.ttPadData))
        from(globalV4)
        //_unused1, _currentDefaults and _userDefaults left as default
    }

    override def deserialize(in: FileInputStream): Unit = {
        in.read(_currentDefaults)
        in.read(_userDefaults)
        super.deserialize(in)
    }
    override def serialize(out: FileOutputStream, saving: Boolean): Unit = {
        out.write(_currentDefaults)
        out.write(_userDefaults)
        super.serialize(out, saving)
    }

    private[this] val _currentDefaults: Array[Byte] = new Array[Byte](128)
    private[this] val _userDefaults: Array[Byte] = new Array[Byte](128)
    protected val _unused1: Array[Byte] = new Array[Byte](160)
}

class GlobalV4 private(p: PadV4) extends Global[PadV4](p) {
    def this() = this(new PadV4(0.toByte))
    def this(in: FileInputStream) = {
        this()
        deserialize(in)
    }
    def this(globalV3: GlobalV3) = {
        this(new PadV4(globalV3.ttPadData, 0))
        from(globalV3)
        //_unused1 left as default
    }

    protected val _unused1: Array[Byte] = new Array[Byte](149)
}
