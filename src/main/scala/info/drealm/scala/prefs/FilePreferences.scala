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

/*
 * This Scala implementation was inspired by the public domain java code provided by
 * David C. A. Croft
 * http://www.davidc.net/programming/java/java-preferences-using-file-backing-store
 * None of the other Java Preferences examples I found was as helpful.  Thanks!
 */

package info.drealm.scala.prefs

import java.util.prefs._

abstract class FilePreferences(parent: FilePreferences, name: String) extends AbstractPreferences(parent, name) {
    protected var _isRemoved = false

    def nodeName: String = s"${if (parent != null) parent.nodeName else ""}${if (parent != null && parent.nodeName != "") "." else ""}${name}"

    protected[prefs] val root = new scala.collection.mutable.HashMap[String, String]()
    protected val children = new scala.collection.mutable.HashMap[String, FilePreferences]()
    protected def getChild(key: String, f: String => FilePreferences): FilePreferences = children.getOrElse(key, null) match {
        case null                          => { children(key) = f(key); children(key) }
        case removed if removed._isRemoved => { children(key) = f(key); children(key) }
        case found                         => found
    }

    def getSpi(key: String): String = root.getOrElse(key, null)

    def removeNodeSpi(): Unit = _isRemoved = true
    def keysSpi(): Array[String] = root.keys.toArray
    def childrenNamesSpi(): Array[String] = children.keys.toArray

    sync()
}

class SystemFilePreferences(parent: SystemFilePreferences, name: String) extends FilePreferences(parent, name) {
    def putSpi(key: String, value: String): Unit = if (root.getOrElse(key, null) != value) throw new SecurityException("System preferences store is not user-modifiable")
    def removeSpi(key: String): Unit = if (root.contains(key)) throw new SecurityException("System preferences store is not user-modifiable")
    def childSpi(key: String): FilePreferences = getChild(key, new SystemFilePreferences(this, _))
    def syncSpi(): Unit = if (!_isRemoved) FilePreferencesFactory.syncSystemFilePreferences(this)
    def flushSpi(): Unit = FilePreferencesFactory.flushSystemFilePreferences(this)
}

class UserFilePreferences(parent: UserFilePreferences, name: String) extends FilePreferences(parent, name) {
    def putSpi(key: String, value: String): Unit = root(key) = value
    def removeSpi(key: String): Unit = root -= key
    def childSpi(key: String): FilePreferences = getChild(key, new UserFilePreferences(this, _))
    def syncSpi(): Unit = if (!_isRemoved) FilePreferencesFactory.syncUserFilePreferences(this)
    def flushSpi(): Unit = FilePreferencesFactory.flushUserFilePreferences(this)
}
