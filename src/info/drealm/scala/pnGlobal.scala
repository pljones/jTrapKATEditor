package info.drealm.scala

import swing._
import info.drealm.scala.migPanel.MigPanel
import info.drealm.scala.{ Localization => L }

object pnGlobal extends MigPanel("insets 5", "[]", "[]") {
    name = "pnGlobal"
    contents += (new Label(L.G("lbGlobal")), "cell 0 0")
}