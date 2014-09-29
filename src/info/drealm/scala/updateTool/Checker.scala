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

package info.drealm.scala.updateTool

import java.util.Date
import swing._
import swing.event._
import info.drealm.scala.{ Localization => L, Resource => R, util, eventX }
import info.drealm.scala.prefs.{ Preferences => P }

object Checker extends Publisher {

    lazy val currentVersion = R.S("info/drealm/scala/version.txt") match {
        case null   => "Unknown"
        case stream => try { io.Source.fromInputStream(stream).getLines.toSeq.head.trim } finally { stream.close() }
    }

    private[this] object updateInfo {
        /*
         * Lines may be requested in any order, hence munching into a Seq and lazily determining values
         * Line 1: boolean [Ff]alse/[Tt]rue) - reset (optional!)
         * Line 2: version (string in format of version.currentVersion)
         * Line 3: update URL (string)
         * Live 4+: message (string, multi-line) 
         * */
        private[this] lazy val _update_txt: Seq[String] = {
            // No benefit having this in an ini file
            val url = new java.net.URL("http://www.drealm.info/kat/TrapKATEditor/jTrapKATEditorUpdate.txt")
            try {
                val stream = url.openStream()
                try { io.Source.fromInputStream(stream).getLines.toSeq } finally { stream.close() }
            }
            catch {
                case e: Throwable => {
                    Dialog.showMessage(null,
                        L.G("UIWebException", e.toString()),
                        L.G("UCErrorCaption", L.G("ApplicationProductName")), Dialog.Message.Error)
                    Nil
                }
            }
        }

        lazy val reset: Option[Boolean] = if (_update_txt.length > 0) _update_txt.head.trim match {
            case "true" | "True"   => Some(true)
            case "false" | "False" => Some(false)
            case _                 => None
        }
        else None
        lazy val availableVersion: Option[String] = _update_txt.drop(if (reset.isDefined) 1 else 0) match {
            case x if x.length > 0 => Some(x.head.trim)
            case _                 => None
        }
        lazy val updateURL: Option[String] = _update_txt.drop(if (reset.isDefined) 1 else 0) match {
            case x if x.length > 1 => Some(x.drop(1).head.trim)
            case _                 => None
        }
        lazy val updateMessage: Option[String] = _update_txt.drop(if (reset.isDefined) 1 else 0) match {
            case x if x.length > 2 => Some(x.drop(2).mkString("\n"))
            case _                 => None
        }
    }

    object AutoUpdateMode extends Enumeration {
        type AutoUpdateMode = Value
        val Off, Automatically = Value
        val NotSet = Value(-1)
    }
    import AutoUpdateMode._

    private[this] var _autoUpdateMode: AutoUpdateMode = NotSet
    def autoUpdateMode: AutoUpdateMode = _autoUpdateMode
    def autoUpdateMode_=(value: AutoUpdateMode): Unit = {
        val oldMode = _autoUpdateMode
        _autoUpdateMode = value
        P.updateAutomatically = _autoUpdateMode
        publish(new eventX.AutoUpdateModeChanged(oldMode, _autoUpdateMode))
    }

    def dailyCheck(): Unit = {
        if (autoUpdateMode == Automatically && P.lastUpdateTS != util.dateToDay(new Date())) {
            if (getUpdate(true).isDefined)
                P.lastUpdateTS = new Date()
        }
    }

    def getUpdate(auto: Boolean = false): Option[Boolean] = {
        updateApplicable(auto) match {
            case Some(true) => {
                Dialog.showOptions(null,
                    L.G("UCAvailableUpdate", updateInfo.updateMessage.getOrElse(""), currentVersion, updateInfo.availableVersion.get, updateInfo.updateURL.getOrElse("updateURL missing!")),
                    L.G("UCAvailableCaption", L.G("ApplicationProductName")), Dialog.Options.YesNoCancel, Dialog.Message.Question, null, L.G("UCAvailableOptions").split("\n"), 0) match {
                        case Dialog.Result.Yes => /*Visit*/ util.browse(updateInfo.updateURL.getOrElse("updateURL missing!"))
                        case Dialog.Result.No  => /*Later*/ {}
                        case _                 => /*Ignore*/ P.lastIgnoredVersion = updateInfo.availableVersion.get
                    }
                Some(true)
            }
            case x => x
        }
    }

    def updateApplicable(auto: Boolean): Option[Boolean] = {
        currentVersion match {
            case "Unknown" => None
            case cv => updateInfo.availableVersion match {
                case None => None
                case Some(av) => Some(if (av <= cv) false else {
                    if (updateInfo.reset.getOrElse(false) && av != P.lastIgnoredVersion) P.lastIgnoredVersion = cv
                    !(auto && av <= P.lastIgnoredVersion)
                })
            }
        }
    }

    if (P.updateAutomatically == NotSet) {
        Dialog.showConfirmation(null,
            L.G("UCNotSet", L.G("ApplicationProductName")), L.G("UCNotSetCaption", L.G("ApplicationProductName"))) match {
                case Dialog.Result.Yes => P.updateAutomatically = Automatically
                case _ => {
                    Dialog.showMessage(null, L.G("UCSaidNo"), L.G("UCNotSetCaption", L.G("ApplicationProductName")))
                    P.updateAutomatically = Off
                }
            }
    }
    autoUpdateMode = P.updateAutomatically
}
