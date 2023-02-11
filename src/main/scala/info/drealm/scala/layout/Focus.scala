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

package info.drealm.scala.layout

import scala.swing._

object Focus {
    def findInContainer(container: Container, value: String): Option[Component] = findInContainer(container, (cp => cp.name == value))
    def findInContainer(container: Container, found: Component => Boolean): Option[Component] = {
        val cps = container.contents map { p => findInComponent(p, found) } filter { p => p.isDefined }
        if (cps.length > 0) cps.head else None
    }

    def findAllInContainer(container: Container, found: Component => Boolean): Seq[Component] = {
        Seq(Seq(), container.contents).flatten flatMap {
            p => findAllInComponent(p, found)
        }
    }

    def findInTabbedPane(tabbedPane: TabbedPane, value: String): Option[Component] = findInTabbedPane(tabbedPane, (cp => cp.name == value))
    def findInTabbedPane(tabbedPane: TabbedPane, found: Component => Boolean): Option[Component] = {
        val cps = tabbedPane.pages map { pg => findInComponent(pg.content, found) } filter { p => p.isDefined }
        if (cps.length > 0) cps.head else None
    }

    def findAllInTabbedPane(tabbedPane: TabbedPane, found: Component => Boolean): Seq[Component] = {
        Seq(Seq(), tabbedPane.pages).flatten flatMap {
            pg => findAllInComponent(pg.content, found)
        }
    }

    def findInComponent(component: Component, value: String): Option[Component] = findInComponent(component, (cp => cp.name == value))
    def findInComponent(component: Component, found: Component => Boolean): Option[Component] = {
        if (found(component)) Some(component) else
            component match {
                case cn: Container  => findInContainer(cn, found)
                case tp: TabbedPane => findInTabbedPane(tp, found)
                case _              => None
            }
    }

    def findAllInComponent(component: Component, found: Component => Boolean): Seq[Component] = {
        Seq(if (found(component)) Seq(component) else Seq(), (component match {
            case cn: Container   => findAllInContainer(cn, found)
            case tp: TabbedPane  => findAllInTabbedPane(tp, found)
            case cp if found(cp) => Seq(cp)
            case _               => Seq()
        })).flatten
    }

    def set(component: Component, target: String): Unit = {
        val cp = findInComponent(component, target)
        if (cp.isDefined) cp.get.requestFocusInWindow()
    }
}

class NameSeqOrderTraversalPolicy(container: Container, order: Seq[String], first: String, last: String, default: String, initial: String) extends javax.swing.LayoutFocusTraversalPolicy {
    def this(container: Container, order: Seq[String], first: String, last: String, default: String) = this(container, order, first, last, default, default)
    def this(container: Container, order: Seq[String], first: String, last: String) = this(container, order, first, last, first, first)
    def this(container: Container, order: Seq[String], first: String) = this(container, order, first, order.last, first, first)
    def this(container: Container, order: Seq[String]) = this(container, order, order.head, order.last)

    private[this] lazy val firstCp = getPeer(first)
    private[this] lazy val lastCp = getPeer(last)
    private[this] lazy val defaultCp = getPeer(default)
    private[this] lazy val initialCp = getPeer(initial)

    private[this] def getParents(pn: java.awt.Component, a: java.awt.Component): Seq[java.awt.Component] = a match {
        case cp if cp == pn        => Seq() //{ Console.print("!");  }
        case l: javax.swing.JLabel => getParents(pn, a.getParent()) ++ (if (l.getLabelFor() == null) Seq(l.getLabelFor()) else Seq()) //{ Console.print("L "); }
        case otherwise => a.getName() match {
            case skip if skip == null || skip.length() == 0 || skip == "Spinner.formattedTextField" => getParents(pn, a.getParent()) //{ Console.print("_ "); }
            case otherwise => getParents(pn, a.getParent()) ++ Seq(a) //{ Console.print("A "); }
        }
    }

    protected def getPeer(item: String): java.awt.Component = {
        //Console.println(s"getPeer item ${item} in ${container match { case cp: Component => cp.name; case c => c.getClass().getName() }}")
        Focus.findInContainer(container, item) match {
            case None => null
            case Some(cp) => cp match {
                case spn: info.drealm.scala.spinner.Spinner => spn.peer.getEditor().getComponent(0)
                case cbx: info.drealm.scala.RichComboBox[_] if cbx.editable => cbx.peer.getEditor().getEditorComponent()
                case _ => cp.peer
            }
        }
    }

    private[this] def getJSwingCp(cp: java.awt.Component) = getParents(container.peer, cp) match {
        case x if x.length == 0 => None
        case cps                => Some(cps.last)
    }

    private[this] def getSSwingCp(oCp: Option[java.awt.Component]) = oCp match {
        case None     => None
        case Some(cp) => Focus.findInContainer(container, cp.getName())
    }

    private[this] def getCpPos(cp: Option[Component]) = cp match {
        case None => None
        case Some(_cp) => order.indexOf(_cp.name) match {
            case -1    => None
            case found => Some((_cp, found))
        }
    }

    private[this] def isFocusable(cp: Component) = cp.enabled && cp.visible //{ Console.println(s"isFocusable: cp ${cp.name} enabled ${cp.enabled} visible ${cp.visible}"); }

    private[this] def nextBy(i: Int, d: (Int) => Int, ith: Boolean = false): Option[(Component, Int)] = {
        //Console.println(s"nextBy from ${i} (${order(i)}) by ${d(0)}, ith ${ith}")
        Focus.findInContainer(container, order(i)) match {
            case Some(_cp) if ith && isFocusable(_cp) => Some((_cp, i))
            case _ => d(i) match {
                case _i if _i >= 0 && _i < order.length => nextBy(_i, d, true)
                case _                                  => None
            }
        }
    }

    protected def stepBy(cp: java.awt.Component, nthFrom: (Int) => Int, ith: Boolean = false): java.awt.Component = {
        if (cp == null) null
        else {
            //Console.println(s"stepBy cp ${cp.getName()} by ${nthFrom(0)}, ith ${ith}")
            getCpPos(getSSwingCp(getJSwingCp(cp))) match {
                case None => null
                case Some((cp, pos)) => nextBy(pos, nthFrom, ith) match {
                    case Some((cp, pos)) => getPeer(cp.name)
                    case _               => null
                }
            }
        }
    }
    
    protected def containerValid(pn: java.awt.Container): Boolean =
        pn == container.peer && pn.isShowing() && pn.isFocusTraversalPolicyProvider() && pn.isFocusTraversalPolicySet()

    //private[this] def fn(pn: java.awt.Container) = "Container " +
    //    (if (pn.isFocusCycleRoot()) "isRoot " else "") +
    //    (if (pn.isFocusTraversalPolicyProvider() && pn.isFocusTraversalPolicySet()) "isPolicy " else "") +
    //    (if (pn == container.peer) "isSelf " else "") +
    //    (if (pn.isShowing()) "isShowing " else "") +
    //    s"${pn.getName()} (${pn.getClass().getName()})"

    override def getComponentAfter(pn: java.awt.Container, cp: java.awt.Component): java.awt.Component = {
        //Console.println(s"getComponentAfter: ${fn(pn)} cp ${if (cp == null) "null" else s"${cp.getName()} (${cp.getClass().getName()})"}")
        val peer = if (containerValid(pn)) { if (cp != null) stepBy(cp, _ + 1) else getFirstComponent(pn) } else null
        //Console.println(s"getComponentAfter => ${if (peer == null) "is null!" else s"${peer.getName()} (${peer.getClass().getName()})"}")
        peer
    }

    override def getComponentBefore(pn: java.awt.Container, cp: java.awt.Component): java.awt.Component = {
        //Console.println(s"getComponentBefore: ${fn(pn)} cp ${if (cp == null) "null" else s"${cp.getName()} (${cp.getClass().getName()})"}")
        val peer = if (containerValid(pn)) { if (cp != null) stepBy(cp, _ - 1) else getLastComponent(pn) } else null
        //Console.println(s"getComponentBefore => ${if (peer == null) "is null!" else s"${peer.getName()} (${peer.getClass().getName()})"}")
        peer
    }

    override def getFirstComponent(pn: java.awt.Container): java.awt.Component = {
        //Console.println(s"getFirstComponent: ${fn(pn)} -> first (${first}) ${if (firstCp == null) "null" else s"${firstCp.getName()} (${firstCp.getClass().getName()})"}")
        val peer = if (containerValid(pn)) stepBy(firstCp, _ + 1, true) else null
        //Console.println(s"getFirstComponent => ${if (peer == null) "is null!" else s"${peer.getName()} (${peer.getClass().getName()})"}")
        peer
    }

    override def getLastComponent(pn: java.awt.Container): java.awt.Component = {
        //Console.println(s"getLastComponent: ${fn(pn)} -> last (${last}) ${if (lastCp == null) "null" else s"${lastCp.getName()} (${lastCp.getClass().getName()})"}")
        val peer = if (containerValid(pn)) stepBy(lastCp, _ - 1, true) else null
        //Console.println(s"getLastComponent => ${if (peer == null) "is null!" else s"${peer.getName()} (${peer.getClass().getName()})"}")
        peer
    }

    override def getDefaultComponent(pn: java.awt.Container): java.awt.Component = {
        val peer = if (containerValid(pn)) stepBy(defaultCp, _ + 1, true) else null
        //Console.println(s"getDefaultComponent: ${fn(pn)} -> default (${default}) ${if (defaultCp == null) "null" else s"${defaultCp.getName()} (${defaultCp.getClass().getName()})"} => ${if (peer == null) "is null!" else s"${peer.getName()} (${peer.getClass().getName()})"}")
        peer
    }

    override def getInitialComponent(w: java.awt.Window): java.awt.Component = {
        val peer = stepBy(initialCp, _ + 1, true)
        //Console.println(s"getInitialComponent: ${w} -> initial (${initial}) ${if (initialCp == null) "null" else s"${initialCp.getName()} (${initialCp.getClass().getName()})"} => ${if (peer == null) "is null!" else s"${peer.getName()} (${peer.getClass().getName()})"}")
        peer
    }

    //Console.println(s"NameSeqOrderTraversalPolicy ${container match { case cp: Component => cp.name; case c => c.getClass().getName() }}; default ${default} ; order ${order}")
}
