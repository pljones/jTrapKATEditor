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
    def getHome: java.io.File = {
        if (!com.sun.jna.Platform.isWindows()) {
            new java.io.File(System.getProperty("user.home"))
        }
        else {
            val pszPath: Array[Char] = new Array[Char](WinDef.MAX_PATH)
            new java.io.File(Shell32.INSTANCE.SHGetSpecialFolderPath(null, pszPath, ShlObj.CSIDL_MYDOCUMENTS, false) match {
                case true => new String(pszPath.takeWhile(c => c != '\0'))
                case _    => System.getProperty("user.home")
            })
        }
    }
}
