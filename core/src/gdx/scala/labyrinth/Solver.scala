package gdx.scala.labyrinth

abstract class Solver {
  def solve(floors: Array[Floor], start: Cell): Array[Cell]

  /** Clear all marked routes in the labyrinth */
  def clearPath(floors: Array[Floor]): Unit = floors.map(_.getCells).reduce(_++_).flatten.foreach(_.setPath(None))
}
