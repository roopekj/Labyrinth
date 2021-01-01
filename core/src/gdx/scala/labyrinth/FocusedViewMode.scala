package gdx.scala.labyrinth

import scala.collection.mutable.Map;

class FocusedViewMode(private val start: Start) extends ViewMode(start) {
  /** The amount of cells to render in any directions */
  private val renderSize: Int = 5

  /** The amount of cells rendered in horizontally and vertically */
  private val renderArea: Int = 1 + (renderSize * 2)

  /** The coordinates of the player, used to position the camera */
  private var playerX: Float = _
  private var playerY: Float = _

  /** Precalculated position in the middle of the cell, used to position camera */
  private var halfCell: Float = _

  /** The amount of rendering cycles it takes for the animation to end */
  val movementAnimationLength = 10

  /** Amount of pixels the camera moves during each rendering cycle */
  var movementAmount: Float = _

  /** The cells close enough to the player that they are visible */
  private var cellsToRender: Array[Cell] = Array()

  /** The amount of times the camera should still be moved before the movement-animation ends */
  private val movementCounters: Map[Direction.Direction, Int] = Map(
    Direction.NORTH -> 0,
    Direction.EAST -> 0,
    Direction.SOUTH -> 0,
    Direction.WEST -> 0
  )

  /** Update all variables specific to the focused view mode that need to change when the size of the labyrinth changes */
  override def updateDimensions: Unit = {
    super.updateDimensions

    playerX = this.wallW
    playerY = this.wallH

    halfCell = this.cellW / 2

    movementAmount = (cellW + wallW) / movementAnimationLength

    this.setPlayerCoordinates
    this.setRenderScale
    this.setCellsToRender
  }

  /** Set the zoom of the camera to a value where the correct amount of cells are shown */
  def setRenderScale: Unit = Manager.camera.zoom = (this.spaceW * this.renderArea + wallW) / this.gameWidth

  /** Finds the cells that should be rendered during this cycle */
  def setCellsToRender: Unit = {
    var newCells: Array[Cell] = Array(this.game.player.getCurrentCell)

    var i = 0
    var cellsToAdd: Array[Cell] = Array()

    while (i <= renderSize) {
      var cellIndex = 0

      while (cellIndex < newCells.length) {

        val newCell: Cell = newCells(cellIndex)
        val possibleCells: Array[Option[Cell]] = Array(newCell.adjacentEast, newCell.adjacentWest)
        possibleCells.foreach({
          case Some(definedCell) => if (!(newCells ++ cellsToAdd).contains(definedCell)) cellsToAdd :+= definedCell
          case _ =>
        })
        cellIndex += 1
      }
      i += 1
      newCells ++= cellsToAdd
    }

    var ii = 0
    cellsToAdd = Array()

    while (ii <= renderSize) {
      var cellIndex = 0
      while (cellIndex < newCells.length) {
        val newCell: Cell = newCells(cellIndex)
        val possibleCells: Array[Option[Cell]] = Array(newCell.adjacentNorth, newCell.adjacentSouth)
        possibleCells.foreach({
          case Some(definedCell) => if (!(newCells ++ cellsToAdd).contains(definedCell)) cellsToAdd :+= definedCell
          case _ =>
        })
        cellIndex += 1
      }
      ii += 1
      newCells ++= cellsToAdd
    }

    this.cellsToRender = newCells.distinct
  }

  /** Set the variables used to track the current location of the player */
  def setPlayerCoordinates: Unit = {
    val playerfloor = this.game.floors(this.game.player.getCurrentFloor)
    val playerrow: Option[Array[Cell]] = playerfloor.getCells.find(_.contains(this.game.player.getCurrentCell))
    playerrow match {
      case Some(row) =>
        val level = playerfloor.getCells.reverse.indexOf(row)
        this.playerY += (level.toFloat * spaceW)
        val playercolumn: Int = playerfloor.getCells.reverse(level).indexOf(this.game.player.getCurrentCell)
        this.playerX += (playercolumn.toFloat * spaceH)
      case _ =>
    }
  }

  /** Render all cells in a floor */
  def renderFloor(floor: Floor): Unit = {
    var cellIndex: Int = 0
    while (cellIndex < this.cellsToRender.length) {
      val cell = this.cellsToRender(cellIndex)
      this.renderCell(cell)
      cellIndex += 1
    }

    batch.draw(PLAYER, this.playerX, this.playerY, cellW, cellH)
    batch.draw(WALL, 0, 0, wallW, gameHeight)
    batch.draw(WALL, 0, gameHeight - wallH, gameWidth, wallH)
  }

  /** Render a cell according to the coordinates saved in its local variables */
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

    if (cell.isEnd) batch.draw(END, x, y, cellW, cellH)

    val currentWalls: Array[(Option[Cell], (Float, Float) => Unit)] = Array(
      (cell.east, this.drawEast),
      (cell.south, this.drawSouth)
    )

    var wallIndex = 0
    val absY = y - wallH
    while (wallIndex < currentWalls.length) {
      val wall = currentWalls(wallIndex)
      if (wall._1.isEmpty) {
        wall._2(x, absY)
      }
      wallIndex += 1
    }
  }

  def handleMovementKeypress(key: Int): Unit = {
    if (this.game.player.getCurrentCell.neighborInDirection(key).isEmpty) return
    frameIndex = (frameIndex + 1) % movementAnimationLength
    if (this.keysDown(key)) {
      if (frameIndex == 0) {
        this.beginMovementAnimation(Manager.movementActions(key))
      }
    } else {
      this.keysDown(key) = true
      this.beginMovementAnimation(Manager.movementActions(key))
      this.frameIndex = 0
    }
  }

  /** Starts the movement animation in the specified direction */
  def beginMovementAnimation(d: Direction.Direction): Unit = {
    if (this.movementCounters.values.count(_ > 0) == 0) {
      this.movementCounters(d) = movementAnimationLength
    }
  }

  override def render(f: Float) {
    super.render(f)

    val movementDirs: Map[Direction.Direction, Int] = this.movementCounters.filter(a => a._2 > 0)
    if (movementDirs.keys.nonEmpty) {
      val d: Direction.Direction = movementDirs.keys.head
      d match {
        case Direction.NORTH => this.playerY += movementAmount
        case Direction.EAST => this.playerX += movementAmount
        case Direction.SOUTH => this.playerY -= movementAmount
        case Direction.WEST => this.playerX -= movementAmount
        case _ =>
      }
      this.movementCounters(d) -= 1
      if (movementCounters(d) == 0) this.game.movePlayer(d)
      this.setCellsToRender
    }

    Manager.camera.position.set(this.playerX + halfCell, this.playerY + halfCell, 0)
    Manager.camera.update()
  }

  override def dispose(): Unit = {
    Manager.camera.position.set(this.gameHeight / 2, this.gameHeight / 2, 0)
    Manager.camera.zoom = 1.toFloat
    Manager.camera.update()
  }
}