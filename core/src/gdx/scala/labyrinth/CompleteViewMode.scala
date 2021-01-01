package gdx.scala.labyrinth

class CompleteViewMode(private val start: Start) extends ViewMode(start) {

  def renderFloor(floor: Floor): Unit = {
    val cells: Array[Array[Cell]] = floor.getCells.reverse
    cells.foreach(m => {
      m.foreach(n => {
        this.renderCell(n)
      })
    })

    batch.draw(WALL, 0, 0, wallW, gameHeight)
    batch.draw(WALL, 0, gameHeight - wallH, gameWidth, wallH)
  }

  def renderCell(cell: Cell): Unit = {
    val x = cell.xCoord
    val y = cell.yCoord

    cell.path match {
      case Some(a) => batch.draw(a.getTexture, x, y, cellW, cellH)
      case _ =>
    }

    cell.contents match {
      case Some(a) => batch.draw(a.getTexture, x, y, cellW, cellH)
      case _ =>
    }

    if (this.game.player.getCurrentCell == cell) batch.draw(PLAYER, x, y, cellW, cellH)
    if (cell.isEnd) batch.draw(END, x, y, cellW, cellH)

    val currentWalls: Array[(Option[Cell], (Float, Float) => Unit)] = Array(
      (cell.east, this.drawEast),
      (cell.south, this.drawSouth)
    )

    val absY = y - wallH
    currentWalls.foreach(a => {
      if (a._1.isEmpty) {
        a._2(x, absY)
      }
    })
  }

  def handleMovementKeypress(key: Int): Unit = {
    frameIndex = (frameIndex + 1) % 15
    if (this.keysDown(key)) {
      if (frameIndex == 0) {
        this.game.movePlayer(Manager.movementActions(key))
      }
    } else {
      this.keysDown(key) = true
      this.game.movePlayer(Manager.movementActions(key))
      this.frameIndex = 0
    }
  }
}
