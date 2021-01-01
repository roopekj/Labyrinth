package gdx.scala.labyrinth

/**
 * Labyrinth-generator for testing purposes, connects every cell to all four of its adjacent cells
 */
class TestGenerator(private val game: LabyrinthGame) extends Generator(game) {
  def generate(floors: Array[Floor], player: Player): Unit = {
    floors.foreach(f => {
      f.getCells.indices.foreach(m => {
        val cells: Array[Cell] = f.getCells(m)
        cells.indices.foreach(n => {
          val cell: Cell = cells(n)
          val adjacent = (game.findAdjacent(cell, Direction.NORTH) -> Direction.NORTH,
            game.findAdjacent(cell, Direction.EAST) -> Direction.EAST,
            game.findAdjacent(cell, Direction.SOUTH) -> Direction.SOUTH,
            game.findAdjacent(cell, Direction.WEST) -> Direction.WEST)
          if (adjacent._1._1.isDefined) connect(cell, adjacent._1._1.get, adjacent._1._2)
          if (adjacent._2._1.isDefined) connect(cell, adjacent._2._1.get, adjacent._2._2)
          if (adjacent._3._1.isDefined) connect(cell, adjacent._3._1.get, adjacent._3._2)
          if (adjacent._4._1.isDefined) connect(cell, adjacent._4._1.get, adjacent._4._2)
        })
      })
    })
    val start = floors(0).getCells(0)(0)
    val end = floors(0).getCells(0)(1)

    player.setCurrentCell(start)
  }
}
