package gdx.scala.labyrinth

class breadthFirst extends Solver {
  def solve(floors: Array[Floor], start: Cell): Array[Cell] = {
    var currentWave: Array[Cell] = start.adjacent3D.filter(_.isDefined).map(_.get)
    currentWave.foreach(_.setSolveConnection(start))
    var visitedCells: Array[Cell] = Array()
    while (!currentWave.isEmpty) {
      var nextWave: Array[Cell] = Array()
      currentWave.indices.foreach(n => {
        val current: Cell = currentWave(n)

        if (current.isEnd) {
          super.clearPath(floors)
          return findSolution(current, start)
        }

        visitedCells :+= current
        val toAdd = current.adjacent3D.filter(_.isDefined).map(_.get).filter(cell => !visitedCells.contains(cell) && !nextWave.contains(cell))
        toAdd.foreach(_.setSolveConnection(current))
        nextWave ++= toAdd
      })
      currentWave = nextWave
    }

    Array()
  }

  def findSolution(end: Cell, start: Cell): Array[Cell] = {
    var solution: Array[Cell] = Array()
    var current: Cell = end
    while (current != start) {
      solution :+= current
      current = current.getSolveConnection.get
    }
    solution :+ current
  }
}
