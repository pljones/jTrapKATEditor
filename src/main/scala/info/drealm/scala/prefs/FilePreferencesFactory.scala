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

import scala.xml._
import java.util.prefs._
import info.drealm.scala.util

object FilePreferencesFactory {
    private[this] val SYSTEM_PROPERTY_FILE = "info.drealm.scala.prefs.FilePreferencesFactory.SystemPropertyFile"
    private[this] val USER_PROPERTY_FILE = "info.drealm.scala.prefs.FilePreferencesFactory.UserPropertyFile"
    private[this] val PROPERTY_STORE = "jTrapKATEditor.xml"

    private[this] lazy val _systemPropertyFile = new java.io.File(System.getProperty(SYSTEM_PROPERTY_FILE, new java.io.File(util.getSystemStore, PROPERTY_STORE).getCanonicalPath()))
    private[this] lazy val _userPropertyFile = new java.io.File(System.getProperty(USER_PROPERTY_FILE, new java.io.File(util.getUserStore, PROPERTY_STORE).getCanonicalPath()))
    private lazy val _systemRoot: FilePreferences = new SystemFilePreferences(null, "")
    private lazy val _userRoot: FilePreferences = new UserFilePreferences(null, "")

    private[this] def pp(n: Node) = new scala.xml.PrettyPrinter(80, 2).format(n)

    private[this] def attrGet(node: Node, attrName: String): Option[String] = node.attribute(attrName) match {
        case Some(Seq(n)) => Some(n.text)
        case _            => None
    }
    private[this] def attrMatch(node: Node, attrName: String, attrVal: String): Boolean = attrVal != null && attrGet(node, attrName).getOrElse(null) == attrVal

    private[this] def readNode(propertyFile: Elem, nodeName: String): Node = propertyFile.child.find(node =>
        node.label == "node" && attrMatch(node, "name", nodeName)).getOrElse(Elem(null, "node", Null.append(Attribute("name", Text(nodeName), Null)), TopScope, true)
    )
    private[this] def readProperty(property: Node) = attrGet(property, "key") match {
        case Some(key) => attrGet(property, "value") match {
            case Some(value)                    => Some(key -> value)
            case None if property.child != Null => Some(key -> (property.child map (n => pp(n))).fold("")(_ ++ _))
            case _                              => None
        }
        case None => None
    }
    private[this] def readPropertyFile(file: java.io.File): Elem = {
        try {
            if (file.isFile()) XML.loadFile(file) else <propertyFile/>
        }
        catch {
            case e: Throwable => <propertyFile/>
        }
    }

    private[this] def syncFilePreferences(propertyFile: java.io.File, node: FilePreferences): Unit = {
        node.root.clear()
        propertyFile.synchronized {
            readNode(readPropertyFile(propertyFile), node.nodeName).child filter (n => n.label == "property") map { property => node.root += readProperty(property).get }
        }
    }
    protected[prefs] def syncSystemFilePreferences(node: FilePreferences): Unit = syncFilePreferences(_systemPropertyFile, node)
    protected[prefs] def syncUserFilePreferences(node: FilePreferences): Unit = syncFilePreferences(_userPropertyFile, node)

    private[this] def flushFilePreferences(propertyFile: java.io.File, node: FilePreferences): Unit = {
        propertyFile.synchronized {
            val oldPropertyFile = readPropertyFile(propertyFile)
            val oldNodes = oldPropertyFile.child filterNot (n => attrMatch(n, "name", node.nodeName))

            val newProperties = node.root.keySet.toSeq map (key =>
                Elem(null, "property", Attribute("key", Text(key), Attribute("value", Text(node.root(key)), Null)), TopScope, true)
            )
            val newNode = Elem(null, "node", Attribute("name", Text(node.nodeName), Null), TopScope, true, newProperties: _*)
            val newPropertyFile = Elem(null, "propertyFile", Null, TopScope, true, (oldNodes ++ newNode): _*)

            if (Utility.trim(oldPropertyFile) != Utility.trim(newPropertyFile)) {
                propertyFile match {
                    case system if propertyFile == _systemPropertyFile => throw new BackingStoreException("Invalid root: will not write to systemRoot.")
                    case user => {
                        util.createUserStore()
                        val out = new java.io.FileWriter(propertyFile)
                        out.write(pp(Utility.trim(newPropertyFile)))
                        out.close()
                    }
                }
            }
        }
    }
    protected[prefs] def flushSystemFilePreferences(node: FilePreferences): Unit = flushFilePreferences(_systemPropertyFile, node)
    protected[prefs] def flushUserFilePreferences(node: FilePreferences): Unit = flushFilePreferences(_userPropertyFile, node)
}

class FilePreferencesFactory extends PreferencesFactory {
    // Required API:
    def systemRoot: Preferences = FilePreferencesFactory._systemRoot
    def userRoot: Preferences = FilePreferencesFactory._userRoot
}
