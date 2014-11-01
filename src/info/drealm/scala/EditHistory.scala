package info.drealm.scala

trait HistoryAction { val actionName: String; def undoAction(): Unit; def redoAction(): Unit }
object EditHistory {
    private[this] var _editHistory = List[HistoryAction]()
    private[this] var _depth = 0

    def canUndo: Boolean = _depth < _editHistory.length
    def undoAction(): Unit = if (canUndo) _editHistory.drop(_depth).headOption map (x => { _depth = _depth + 1; x.undoAction() })
    def undoActionName: Option[String] = if (canUndo) _editHistory.drop(_depth).headOption.map(x => x.actionName) else None

    def canRedo: Boolean = _depth > 0
    def redoAction(): Unit = if (canRedo) _editHistory.drop(_depth - 1).headOption map (x => { _depth = _depth - 1; x.redoAction() })
    def redoActionName: Option[String] = if (canRedo) _editHistory.drop(_depth - 1).headOption.map(x => x.actionName) else None

    def add(action: HistoryAction): Unit = {
        _editHistory = action :: _editHistory.drop(_depth);
        _depth = 0
    }
    
    def clear(): Unit = {
        _editHistory = List()
        _depth = 0
    }
}
