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

import java.util.{ Locale, ResourceBundle }

object Localization {
    private[this] val stringBundle = ResourceBundle.getBundle("info.drealm.scala.Localization", Locale.getDefault())
    def G(lookup: String): String = try {
        stringBundle.getString(lookup)
    }
    catch {
        case e: java.util.MissingResourceException => "<<" + lookup + ">>"
    }
    def G(lookup: String, args: String*): String = {
        def from(n: Int): Stream[Int] = n #:: from(n + 1)
        (args zip from(0)).foldLeft(G(lookup))((s, x) => s.replaceAll(s"[{]${x._2}[}]", s"${x._1}"))
    }
}
