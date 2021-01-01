package gdx.scala.labyrinth

import com.badlogic.gdx.Input.Keys
import com.badlogic.gdx.graphics.g2d._
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.badlogic.gdx.utils.viewport.{ScalingViewport}
import com.badlogic.gdx.{Gdx, InputMultiplexer}
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.utils.Scaling

import scala.collection.mutable.Map


object Manager {
  /**
   * Identifiers for different types of cells
   * neighbor east, neighbor south, neighbor down, isStart, isEnd
   * Used when saving labyrinths to file
   */

  val MAX_FLOORS = 10
  val MAX_WIDTH_COMPLETE = 100
  val MAX_WIDTH_FOCUSED = 200
  val WIDTH_STEP_SIZE = 2

  val fingerprints: Map[Array[Boolean], Char] = Map(
    Array(true, false, true, false, false) -> '╶',
    Array(false, true, true, false, false) -> '╷',
    Array(true, true, true, false, false) -> '┌',
    Array(false, false, true, false, false) -> '╳',

    Array(true, false, false, false, false) -> '╺',
    Array(false, true, false, false, false) -> '╻',
    Array(true, true, false, false, false) -> '┏',

    Array(true, false, true, true, false) -> '└',
    Array(false, true, true, true, false) -> '│',
    Array(true, true, true, true, false) -> '├',
    Array(false, false, true, true, false) -> '╵',

    Array(true, false, true, false, true) -> '─',
    Array(false, true, true, false, true) -> '┐',
    Array(true, true, true, false, true) -> '┬',
    Array(false, false, true, false, true) -> '╴'
  )

  val movementActions: Map[Int, Direction.Direction] = Map(
    Keys.W -> Direction.NORTH,
    Keys.D -> Direction.EAST,
    Keys.S -> Direction.SOUTH,
    Keys.A -> Direction.WEST
  )

  val worldWidth: Float = 1000.toFloat
  val worldHeight: Float = 1000.toFloat

  val camera = new OrthographicCamera()
  val animationCamera = new OrthographicCamera()

  val viewport = new ScalingViewport(Scaling.none, worldWidth, worldHeight, camera)
  val animationViewport = new ScalingViewport(Scaling.none, worldWidth, worldHeight, animationCamera)

  animationCamera.zoom = 0.4.toFloat

  camera.setToOrtho(false, worldWidth, worldHeight)
  viewport.setScreenPosition(0, 0)

  animationCamera.setToOrtho(false, worldWidth, worldHeight)
  animationViewport.setScreenPosition(0, 0)

  viewport.setScreenBounds(0, 0, worldWidth.toInt, worldHeight.toInt)
  viewport.setWorldSize(worldWidth, worldHeight)

  val aspectRatios = new Sprite();
  aspectRatios.setPosition(0, 0)
  aspectRatios.setSize(100, 100)

  val multiplexer = new InputMultiplexer()
  Gdx.input.setInputProcessor(multiplexer)

  val scoresPath: String = "scores.tsv"
  val savedLabyrinthPath: String = "savedLabyrinth"

  val gameMusicPath: String = "gameMusic.ogg"
  val menuMusicPath: String = "menuMusic.ogg"

  val animationPath: String = "labyrinth.png"

  val defaultSkin = new Skin(Gdx.files.internal("default/skin/uiskin.json"))
  val biologicalSkin = new Skin(Gdx.files.internal("biological-attack/skin/biological-attack-ui.json"))
  val neutralizerSkin = new Skin(Gdx.files.internal("neutralizer/skin/neutralizer-ui.json"))
}