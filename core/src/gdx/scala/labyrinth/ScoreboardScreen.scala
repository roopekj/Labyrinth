package gdx.scala.labyrinth

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Screen
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.scenes.scene2d.ui.Label
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.utils.viewport.ExtendViewport

class ScoreboardScreen(val start: Start) extends Screen {
  private val batch = this.start.batch

  private var labels: Array[Label] = _
  private var table: Table = _
  private var stage: Stage = new Stage(new ExtendViewport(1000, 1000))

  def initScores: Unit = {
    stage = new Stage(new ExtendViewport(1000, 1000))
    stage.setViewport(Manager.viewport)
    table = new Table()
    labels = Array()
    table.defaults().pad(10)
    table.setFillParent(true)

    val hScoreLabel: Label = new Label("HIGH SCORES:", Manager.biologicalSkin)
    val xyLabel: Label = new Label(s"width/height: ${this.start.game.getHeight}", Manager.defaultSkin)
    val zLabel: Label = new Label(s"Floors: ${this.start.game.getDepth}", Manager.defaultSkin)
    val viewmodeLabel: Label = new Label(s"viewmode: ${this.start.game.getViewmode}", Manager.defaultSkin)

    table.add(hScoreLabel)
    table.row()

    val relevantScores: Array[(String, Long)] = start.game.getScores.filter(a => a.xy == start.game.getHeight && a.z == start.game.getDepth && a.view == start.game.getViewmode).map(score => (score.name, score.score))

    for (score <- relevantScores.sortBy(_._2).reverse.take(10)) {
      val newLabel = new Label(s"${score._1}: ${score._2}", Manager.biologicalSkin)

      table.add(newLabel).center()
      table.row()
      labels :+= newLabel
    }

    table.row()
    table.add(xyLabel).center.padTop(200.0.toFloat)
    table.row()
    table.add(zLabel).center
    table.row()
    table.add(viewmodeLabel).center

    stage.addActor(table)
  }

  def pause(): Unit = {}

  def resume(): Unit = {}

  def dispose(): Unit = {}

  def hide(): Unit = {}

  def show(): Unit = initScores

  def resize(w: Int, h: Int): Unit = {
    Manager.viewport.update(Gdx.graphics.getWidth, Gdx.graphics.getHeight);
    Manager.camera.update();
  }

  def render(d: Float): Unit = {
    batch.setProjectionMatrix(Manager.camera.combined);
    Gdx.gl.glClearColor(255, 255, 255, 1)
    Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)

    batch.begin()
    stage.draw()
    stage.act()
    batch.end()
  }
}