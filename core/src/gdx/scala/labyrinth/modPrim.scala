package gdx.scala.labyrinth

import scala.util.Random

class modPrim(private val game: LabyrinthGame) extends Generator(game) {
  private val r: Random = new Random()

  def generate(floors: Array[Floor], player: Player): Unit = {
    super.setAdjacentCells(this.game.floors)

    this.game.floors.indices.foreach(f => {
      /**
       * To limit the amount of stairs created on any given floor, we set the generator to create
       * a set of stairs downwards every n:th corner it creates, where n is the width (and height) of the labyrinth
       */
      var cornersCreated = 0

      val floor: Floor = floors(f)
      val start: Cell = floor.getCells(0)(0)

      var possibleCells: Array[Cell] = Array(start.adjacentEast, start.adjacentSouth).filter(_.isDefined).map(_.get)
      var visitedCells: Array[Cell] = Array(start)

      while (!possibleCells.isEmpty) {
        val currentIndex = r.nextInt(possibleCells.length)
        val current: Cell = possibleCells(currentIndex)
        val adjacent: Array[(Option[Cell], Direction.Direction)] = Array(
          (current.adjacentNorth, Direction.NORTH),
          (current.adjacentEast, Direction.EAST),
          (current.adjacentSouth, Direction.SOUTH),
          (current.adjacentWest, Direction.WEST)
        ).filter(_._1.isDefined)

        val connectFrom = adjacent.filter(a => visitedCells.contains(a._1.get) && (a._1.get.contents.isEmpty || a._1.get.howMany2DConnections == 0))

        val toConnect: (Option[Cell], Direction.Direction) = connectFrom(r.nextInt(connectFrom.length))
        val connected: Cell = toConnect._1.get
        this.connect(current, connected, toConnect._2)

        if (current.howMany2DConnections == 1 && current.contents.isEmpty) {
          cornersCreated = (cornersCreated + 1) % (floor.getCells.length * 2)
          if (cornersCreated == 0) {
            this.game.findAdjacent(current, Direction.DOWN) match {
              case Some(cell) => this.connect(current, cell, Direction.DOWN)
              case _ =>
            }
          }
        }

        visitedCells :+= current
        possibleCells = possibleCells.filter(_ != current)

        if (current.contents.isEmpty) {
          adjacent.foreach(a => {
            a._1 match {
              case Some(cell) =>
                if (!visitedCells.contains(cell) && !possibleCells.contains(cell)) {
                  possibleCells :+= cell
                }
              case _ =>
            }
          })
        }
      }
    })

    val cells: Array[Array[Cell]] = floors.map(_.getCells).reduce(_ ++ _)

    val possibleEnds = cells(r.nextInt(cells.length)).filter(_.contents.isEmpty)
    val end: Cell = possibleEnds(r.nextInt(possibleEnds.length))

    val floorIndex = r.nextInt(floors.length)
    val floor = floors(floorIndex)

    end.isEnd = true

    val possibleStarts = floor.getCells.flatten.filter(c => c.contents.isEmpty && !c.isEnd)
    val start = possibleStarts(r.nextInt(possibleStarts.length))

    start.isBeginning = true
    player.setCurrentCell(start)
  }
}