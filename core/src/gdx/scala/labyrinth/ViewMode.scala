package gdx.scala.labyrinth

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input.Keys
import com.badlogic.gdx.Screen
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.{GL20, Texture}

import scala.collection.mutable.Map;

abstract class ViewMode(private val start: Start) extends Screen {
  private[labyrinth] lazy val WALL: Texture = new Texture(Gdx.files.internal("wall.png"))
  private[labyrinth] lazy val END: Texture = new Texture(Gdx.files.internal("end.png"))
  private[labyrinth] lazy val PLAYER: Texture = new Texture(Gdx.files.internal("player.png"))

  protected val batch: SpriteBatch = start.batch
  protected val game: LabyrinthGame = start.game

  // Width and height of the rendered area
  protected val gameWidth: Float = Manager.worldWidth
  protected val gameHeight: Float = Manager.worldHeight

  // width and height of each space (the space contains a cell and four walls)
  protected var spaceW: Float = _
  protected var spaceH: Float = _

  // percentage of space's area that is reserved for walls in each direction
  protected val wallSizeMultiplier: Float = 0.1.toFloat

  // the area that is reserved for the cell in each space
  protected var cellW: Float = _
  protected var cellH: Float = _

  // the area that is reserved for a wall in each space
  protected var wallW: Float = _
  protected var wallH: Float = _

  protected var frameIndex: Int = 0

  protected val keysDown: Map[Int, Boolean] = Map[Int, Boolean](Keys.W -> false, Keys.D -> false, Keys.S -> false, Keys.A -> false)

  val gameplayActions: Map[Int, () => Unit] = Map(
    Keys.Q -> this.game.concede
  )

  def handleMovementKeypress(key: Int): Unit

  def drawEast(x: Float, y: Float): Unit = batch.draw(WALL, x + cellW, y, wallW, cellH + (2 * wallW))

  def drawSouth(x: Float, y: Float): Unit = batch.draw(WALL, x - wallW, y, cellW + (2 * wallW), wallH)

  def renderFloor(f: Floor): Unit

  def renderCell(cell: Cell): Unit

  def handleGameplayKeypress(key: Int): Unit = {
    this.gameplayActions(key)()
  }

  def setCellCoordinates: Unit = {
    for (floor <- this.game.floors) {
      val cells: Array[Array[Cell]] = floor.getCells.reverse
      cells.foreach(m => {
        m.foreach(n => {
          n.xCoord = wallW + m.indexOf(n) * (cellW + wallW)
          n.yCoord = wallH + cells.indexOf(m) * (cellH + wallH)
        })
      })
    }
  }

  /** We give the cells their dimensions beforehand so we do not have to calculate them every render cycle */
  def updateDimensions: Unit = {
    spaceW = gameWidth / this.start.game.getWidth
    spaceW -= (spaceW * wallSizeMultiplier / this.start.game.getWidth)
    spaceH = gameHeight / this.start.game.getHeight
    spaceH -= (spaceH * wallSizeMultiplier / this.start.game.getHeight)

    wallW = spaceW * wallSizeMultiplier
    wallH = spaceH * wallSizeMultiplier

    cellW = spaceW - wallW
    cellH = spaceH - wallH

    setCellCoordinates
  }

  def render(f: Float): Unit = {
    batch.setProjectionMatrix(Manager.camera.combined);
    Gdx.gl.glClearColor(255, 255, 255, 0)
    Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)

    if (keysDown.values.count(_ == true) == 0) this.frameIndex = 0
    for (key <- this.gameplayActions.keys) if (Gdx.input.isKeyPressed(key)) this.handleGameplayKeypress(key)
    for (key <- Manager.movementActions.keys) if (Gdx.input.isKeyPressed(key)) this.handleMovementKeypress(key) else this.keysDown(key) = false

    batch.begin()
    this.renderFloor(this.game.floors(this.game.player.getCurrentFloor))
    batch.end()
  }

  def pause(): Unit = {}

  def resume(): Unit = {}

  def dispose(): Unit = {}

  def hide(): Unit = {}

  def show(): Unit = {
    Gdx.input.setInputProcessor(null)
    this.updateDimensions
  }

  def resize(w: Int, h: Int): Unit = {
    Manager.viewport.update(Gdx.graphics.getWidth, Gdx.graphics.getHeight);
    Manager.camera.update();
  }
}