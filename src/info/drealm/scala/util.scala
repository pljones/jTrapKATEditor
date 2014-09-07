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

import scala.compat.{ Platform => _ }
import com.sun.jna.platform.win32._

/**
 * Provides hacks around standard mechanisms to fix Windows-isms
 */
object util {

    // Document storage location
    def getHome: java.io.File = {
        if (!com.sun.jna.Platform.isWindows()) {
            new java.io.File(System.getProperty("user.home"))
        }
        else {
            val pszPath: Array[Char] = new Array[Char](WinDef.MAX_PATH)
            new java.io.File(Shell32.INSTANCE.SHGetSpecialFolderPath(null, pszPath, ShlObj.CSIDL_MYDOCUMENTS, false) match {
                case true => new String(pszPath.takeWhile(c => c != '\u0000'))
                case _    => System.getProperty("user.home")
            })
        }
    }

    private[this] val _vendor = "peter_l_jones"
    private[this] val _vendowWindows = "Peter L Jones"

    // System-wide settings storage
    def getSystemStore: java.io.File = {
        val prefix = s"${
            if (!com.sun.jna.Platform.isWindows()) {
                new java.io.File(System.getProperty("system.config", "/etc"), _vendor)
            }
            else {
                val pszPath: Array[Char] = new Array[Char](WinDef.MAX_PATH)
                new java.io.File(Shell32.INSTANCE.SHGetSpecialFolderPath(null, pszPath, ShlObj.CSIDL_COMMON_APPDATA, false) match {
                    case true => new String(pszPath.takeWhile(c => c != '\u0000'))
                    case _    => System.getProperty("system.config", "/")
                }, _vendowWindows)
            }.getCanonicalPath()
        }"
        new java.io.File(prefix)
    }

    // Per-user settings storage
    def getUserStore: java.io.File = {
        val prefix = s"${
            if (!com.sun.jna.Platform.isWindows()) {
                new java.io.File(System.getProperty("user.home", ""), s".${_vendor}")
            }
            else {
                val pszPath: Array[Char] = new Array[Char](WinDef.MAX_PATH)
                new java.io.File(Shell32.INSTANCE.SHGetSpecialFolderPath(null, pszPath, ShlObj.CSIDL_APPDATA, false) match {
                    case true => new String(pszPath.takeWhile(c => c != '\u0000'))
                    case _    => System.getProperty("user.home", "/")
                }, _vendowWindows)
            }.getCanonicalPath()
        }"
        new java.io.File(prefix)
    }

    def createUserStore() = {
        getUserStore match {
            case exists if exists.isDirectory() => {}
            case create                         => {create.mkdirs()}
        }
    }

    def browse(target: String): Unit = {
        import java.awt.Desktop
        import info.drealm.scala.{ Localization => L }
        if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
            Desktop.getDesktop().browse(new java.net.URL(target).toURI())
        }
        else {
            import swing._
            Dialog.showMessage(null, new info.drealm.scala.migPanel.MigPanel("insets 5", "[]", "[]") {
                name = "pnNoDesktopBrowse"

                contents += (new Label(L.G("NoDesktopBrowse")), "cell 0 0")

                contents += (new swing.TextField {
                    editable = false
                    focusable = true
                    foreground = java.awt.Color.BLUE
                    text = target
                    preferredSize = new Dimension((preferredSize.getWidth() * 1.05).toInt, preferredSize.getHeight().toInt)
                }, "cell 0 1")
            }.peer, L.G("ApplicationProductName"), swing.Dialog.Message.Info)
        }
    }

    @inline def dateToDay(value: java.util.Date): java.util.Date = new java.text.SimpleDateFormat("YYYY-MM-DD").parse(new java.text.SimpleDateFormat("YYYY-MM-DD").format(value))
}
