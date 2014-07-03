package info.drealm.scala

import info.drealm.scala.{ Resource => R }

object version {
    private[this] lazy val _version = R.S("info/drealm/scala/version.txt") match {
        case null   => "Unknown"
        case stream => try { io.Source.fromInputStream(stream).getLines.toSeq.head.trim } finally { stream.close() }
    }
    def currentVersion: String = _version
}
