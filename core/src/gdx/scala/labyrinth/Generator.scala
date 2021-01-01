package gdx.scala.labyrinth

abstract class Generator(private val game: LabyrinthGame) {
  /**
   * Initialize labyrinth, place player at the beginning of the labyrinth
   * @param floors
   * @param player
   */
  def generate(floors: Array[Floor], player: Player): Unit

  def setAdjacentCells(floors: Array[Floor]): Unit = {
    floors.indices.foreach(x => {
      val f: Floor = floors(x)
      f.getCells.indices.foreach(m => {
        val row = f.getCells(m)

        var cellIndex: Int = 0
        while (cellIndex < row.length) {
          val cell: Cell = row(cellIndex)
          cell.adjacentNorth = if (m > 0) Option(f.getCells(m - 1)(cellIndex)) else None
          cell.adjacentEast = if (cellIndex < row.length - 1) Option(row(cellIndex + 1)) else None
          cell.adjacentSouth = if (m < f.getCells.length - 1) Option(f.getCells(m + 1)(cellIndex)) else None
          cell.adjacentWest = if (cellIndex > 0) Option(row(cellIndex - 1)) else None
          cellIndex += 1
        }
      })
    })
  }

  /**
   * Break the connection between two cells, effectively building a wall between them
   * @param a, first cell
   * @param b, second cell
   * @param d, direction from a -> b
   */
  def isolate(a: Cell, b: Cell, d: Direction.Direction): Unit = {
    d match {
      case Direction.NORTH =>
        a.north = None
        b.south = None
      case Direction.EAST =>
        a.east = None
        b.west = None
      case Direction.SOUTH =>
        a.south = None
        b.north = None
      case Direction.WEST =>
        a.west = None
        b.east = None
    }
  }

  /**
   * Establish a connection between two cells, effectively breaking the (possibly) existing wall between them
   * @param a, first cell
   * @param b, second cell
   * @param d, direction from a -> b
   */
  def connect(a: Cell, b: Cell, d: Direction.Direction): Unit = {
    d match {
      case Direction.NORTH =>
        a.north = Option(b)
        b.south = Option(a)
      case Direction.EAST =>
        a.east = Option(b)
        b.west = Option(a)
      case Direction.SOUTH =>
        a.south = Option(b)
        b.north = Option(a)
      case Direction.WEST =>
        a.west = Option(b)
        b.east = Option(a)
      case Direction.UP =>
        a.up = Option(b)
        a.setContent(this.game.stairsUp)
        b.down = Option(a)
        b.setContent(this.game.stairsDown)
      case Direction.DOWN =>
        a.down = Option(b)
        a.setContent(this.game.stairsDown)
        b.up = Option(a)
        b.setContent(this.game.stairsUp)
    }
  }
}
