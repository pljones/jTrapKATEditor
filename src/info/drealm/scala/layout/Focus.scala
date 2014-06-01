/****************************************************************************
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
 ****************************************************************************/

package info.drealm.scala.layout

import scala.swing._

object Focus {
    def findInContainer(container: Container, value: String): Option[Component] = findInContainer(container, (cp => cp.name == value))
    def findInContainer(container: Container, found: Component => Boolean): Option[Component] = {
        val cps = container.contents map { p => findInComponent(p, found) } filter { p => p.isDefined }
        if (cps.length > 0) cps.head else None
    }
    def findAllInContainer(container: Container, found: Component => Boolean): Seq[Component] = {
        container.contents flatMap { p => findAllInComponent(p, found) }
    }
    def findInTabbedPane(tabbedPane: TabbedPane, value: String): Option[Component] = findInTabbedPane(tabbedPane, (cp => cp.name == value))
    def findInTabbedPane(tabbedPane: TabbedPane, found: Component => Boolean): Option[Component] = {
        val cps = tabbedPane.pages map { pg => findInComponent(pg.content, found) } filter { p => p.isDefined }
        if (cps.length > 0) cps.head else None
    }
    def findAllInTabbedPane(tabbedPane: TabbedPane, found: Component => Boolean): Seq[Component] = {
        tabbedPane.pages flatMap { pg => findAllInComponent(pg.content, found) }
    }
    def findInComponent(component: Component, value: String): Option[Component] = findInComponent(component, (cp => cp.name == value))
    def findInComponent(component: Component, found: Component => Boolean): Option[Component] = {
        component match {
            case cn: Container   => findInContainer(cn, found)
            case tp: TabbedPane  => findInTabbedPane(tp, found)
            case cp if found(cp) => Some(cp)
            case _               => None
        }
    }
    def findAllInComponent(component: Component, found: Component => Boolean): Seq[Component] = {
        component match {
            case cn: Container   => findAllInContainer(cn, found)
            case tp: TabbedPane  => findAllInTabbedPane(tp, found)
            case cp if found(cp) => Seq(cp)
            case _               => Seq()
        }
    }

    def set(component: Component, target: String): Unit = {
        val cp = findInComponent(component, target)
        if (cp.isDefined) cp.get.requestFocus()
    }
}

class NameSeqOrderTraversalPolicy(container: Container, order: Seq[String], first: String, last: String, default: String, initial: String) extends javax.swing.LayoutFocusTraversalPolicy {
    def this(container: Container, order: Seq[String], first: String, last: String, default: String) = this(container, order, first, last, default, default)
    def this(container: Container, order: Seq[String], first: String, last: String) = this(container, order, first, last, first, first)
    def this(container: Container, order: Seq[String], first: String) = this(container, order, first, order.last, first, first)
    def this(container: Container, order: Seq[String]) = this(container, order, order.head, order.last)

    private[this] lazy val firstCp = peer(first)
    private[this] lazy val lastCp = peer(last)
    private[this] lazy val defaultCp = peer(default)
    private[this] lazy val initialCp = peer(initial)

    private[this] def getParents(pn: java.awt.Component, a: java.awt.Component): Seq[java.awt.Component] =
        if (a == pn) Seq()
        else if (a.isInstanceOf[javax.swing.JLabel]) {
            if (a.asInstanceOf[javax.swing.JLabel].getLabelFor() != null)
                getParents(pn, a.getParent()) ++ Seq(a.asInstanceOf[javax.swing.JLabel].getLabelFor())
            else
                getParents(pn, a.getParent())
        }
        else if (a.getName() == null || a.getName().length() == 0 || a.getName() == "Spinner.formattedTextField") getParents(pn, a.getParent())
        else getParents(pn, a.getParent()) ++ Seq(a)

    private[this] def getPos(a: java.awt.Component) = {
        val hier = getParents(container.peer, a)
        if (hier.isEmpty) -1 else order.indexOf(hier.last.getName())
    }

    private[this] def peer(item: String): java.awt.Component = {
        //Console.println("peer: " + item)
        Focus.findInContainer(container, item) match {
            case None => null
            case Some(cp) => cp match {
                case spn: info.drealm.scala.spinner.Spinner => spn.peer.getEditor().getComponent(0)
                case cbx: info.drealm.scala.RichComboBox[_] if cbx.editable => cbx.peer.getEditor().getEditorComponent()
                case _ => cp.peer
            }
        }
    }

    //def fn(pn: java.awt.Container) = "" +
    //    (if (pn.isFocusCycleRoot()) "isRoot " else "") +
    //    (if (pn.isFocusTraversalPolicyProvider() && pn.isFocusTraversalPolicySet()) "isPolicy " else "") +
    //    (if (pn == container.peer) "isSelf " else "") +
    //    pn

    override def getComponentAfter(pn: java.awt.Container, cp: java.awt.Component): java.awt.Component = {
        //Console.println("getComponentAfter: " + fn(pn))
        val pos = getPos(cp)
        if (pos < order.length - 1) peer(order(pos + 1)) else null
    }

    override def getComponentBefore(pn: java.awt.Container, cp: java.awt.Component): java.awt.Component = {
        //Console.println("getComponentBefore: " + fn(pn))
        val pos = getPos(cp)
        if (pos > 0) peer(order(pos - 1)) else null
    }

    override def getFirstComponent(pn: java.awt.Container): java.awt.Component = {
        //Console.println("getFirstComponent: %s -> %s".format(fn(pn), first))
        if (pn == container.peer) firstCp else null
    }

    override def getLastComponent(pn: java.awt.Container): java.awt.Component = {
        //Console.println("getLastComponent: %s -> %s".format(fn(pn), last))
        if (pn == container.peer) lastCp else null
    }

    override def getDefaultComponent(pn: java.awt.Container): java.awt.Component = {
        //Console.println("getDefaultComponent: %s -> %s".format(fn(pn), default))
        if (pn == container.peer) defaultCp else null
    }

    override def getInitialComponent(w: java.awt.Window): java.awt.Component = {
        //Console.println("getInitialComponent: %s -> %s".format(w, initial))
        initialCp
    }
}
