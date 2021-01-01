package gdx.scala.labyrinth

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Screen
import com.badlogic.gdx.graphics.{GL20, Texture}
import com.badlogic.gdx.scenes.scene2d.ui.TextField
import com.badlogic.gdx.scenes.scene2d.ui.Label
import com.badlogic.gdx.graphics.g2d.{SpriteBatch, TextureRegion}
import com.badlogic.gdx.scenes.scene2d.ui.Slider
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.badlogic.gdx.scenes.scene2d.{Actor, InputEvent, Stage}
import com.badlogic.gdx.scenes.scene2d.utils.{ChangeListener, ClickListener}
import com.badlogic.gdx.scenes.scene2d.ui.ButtonGroup
import com.badlogic.gdx.scenes.scene2d.ui.TextButton
import com.badlogic.gdx.scenes.scene2d.ui.CheckBox
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType
import com.badlogic.gdx.utils.viewport.ExtendViewport
import com.badlogic.gdx.utils.Array

import com.badlogic.gdx.scenes.scene2d.ui.ImageButton
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton.ImageButtonStyle
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener.ChangeEvent


class SettingsScreen(val start: Start) extends Screen {

  def toggleStatus: Unit = {
    this.statusLabel.setVisible(!this.statusLabel.isVisible)
  }

  class okListener(private val start: Start, private val xyField: Slider, private val zField: Slider, private val viewMode: ButtonGroup[CheckBox]) extends ClickListener {
    override def clicked(event: InputEvent, x: Float, y: Float): Unit = {
      this.start.game.initGame(nameField.getText, xyField.getValue.toInt, zField.getValue.toInt, viewMode.getChecked.getText.toString)
    }
  }

  class sliderListener(private val label: Label, private val slider: Slider) extends ChangeListener {
    override def changed(event: ChangeEvent, actor: Actor): Unit = {
      label.setText(slider.getValue.toInt.toString)
    }
  }

  class viewChangeListener(private val start: Start, private val viewMode: ButtonGroup[CheckBox], private val slider: Slider) extends ClickListener {
    override def clicked(event: InputEvent, x: Float, y: Float): Unit = {
      val newViewName: String = viewMode.getChecked.getText.toString
      this.start.toggleGameView(newViewName)
      newViewName match {
        case "focused" => slider.setRange(4, Manager.MAX_WIDTH_FOCUSED)
        case "complete" => slider.setRange(4, Manager.MAX_WIDTH_COMPLETE)
      }
    }
  }

  class resetListener(private val start: Start) extends ClickListener {
    override def clicked(event: InputEvent, x: Float, y: Float): Unit = {
      this.start.game.setScores(scala.Array[Score]())
      this.start.game.writeScores
    }
  }

  class saveListener(private val start: Start) extends ClickListener {
    override def clicked(event: InputEvent, x: Float, y: Float): Unit = this.start.game.writeLabyrinth
  }

  class loadListener(private val start: Start) extends ClickListener {
    override def clicked(event: InputEvent, x: Float, y: Float): Unit = this.start.game.readLabyrinth
  }

  private val batch = this.start.batch

  private val statusLabel = new Label("READY", Manager.biologicalSkin)

  private val nameLabel = new Label("name: ", Manager.biologicalSkin)
  private val nameField = new TextField("Player", Manager.biologicalSkin)

  private val xyLabel = new Label("width/height: ", Manager.biologicalSkin)
  private val xyValue = new Label("", Manager.biologicalSkin)
  private val xySlider = new Slider(4, Manager.MAX_WIDTH_FOCUSED, Manager.WIDTH_STEP_SIZE, false, Manager.neutralizerSkin)
  private val xyListener: sliderListener = new sliderListener(xyValue, xySlider)
  xySlider.addListener(xyListener)

  private val viewmodeLabel = new Label("viewmode: ", Manager.biologicalSkin)

  private val zLabel = new Label("# of floors: ", Manager.biologicalSkin)
  private val zValue = new Label("", Manager.biologicalSkin)
  private val zSlider = new Slider(1, Manager.MAX_FLOORS, 1, false, Manager.neutralizerSkin)

  private val zListener: sliderListener = new sliderListener(zValue, zSlider)
  zSlider.addListener(zListener)

  private val okButton = new TextButton("Generate labyrinth", Manager.neutralizerSkin)
  private val completeViewButton: CheckBox = new CheckBox("complete", Manager.defaultSkin)
  private val focusedViewButton: CheckBox = new CheckBox("focused", Manager.defaultSkin)
  private val resetButton = new TextButton("RESET SCORES", Manager.neutralizerSkin)
  private val saveButton = new TextButton("SAVE LABYRINTH", Manager.neutralizerSkin)
  private val loadButton = new TextButton("LOAD LABYRINTH", Manager.neutralizerSkin)
  private val viewButtons: ButtonGroup[CheckBox] = new ButtonGroup[CheckBox](completeViewButton, focusedViewButton)
  private val animationBatch: SpriteBatch = new SpriteBatch
  private var animationTexture: Option[Texture] = None
  private var animationRegion: Option[TextureRegion] = _

  private val scoreboardLabel: Label = new Label("T: Scoreboard", Manager.defaultSkin)
  private val settingsLabel: Label = new Label("M: Settings", Manager.defaultSkin)
  private val startGameLabel: Label = new Label("ESC: Start Game", Manager.defaultSkin)

  private lazy val parallaxTextures: Array[Texture] = new com.badlogic.gdx.utils.Array[Texture]()
  private lazy val backgroundAnimation: ParallaxBackground = new ParallaxBackground(parallaxTextures)
  backgroundAnimation.setSize(Manager.worldWidth, Manager.worldHeight)
  backgroundAnimation.x = (Manager.worldWidth*(1.0/5)).toInt
  backgroundAnimation.y = (Manager.worldWidth*(-1.0/5)).toInt
  backgroundAnimation.setSpeed(1)
  backgroundAnimation.rotation = 45

  private val textBackground: ShapeRenderer = new ShapeRenderer()

  def animateInBackground(filepath: String): Unit = {
    val newTexture = new Texture(Gdx.files.internal(filepath))
    parallaxTextures.add(newTexture)
    this.animationTexture = Some(newTexture)
    newTexture.setWrap(Texture.TextureWrap.Repeat, Texture.TextureWrap.Repeat)
    this.animationRegion = Some(new TextureRegion(animationTexture.get))
  }

  def currentlySelectedViewmode: String = this.viewButtons.getChecked.getText.toString

  resetButton.getLabel.setFontScale(0.6.toFloat, 0.6.toFloat)
  saveButton.getLabel.setFontScale(0.5.toFloat, 0.6.toFloat)
  loadButton.getLabel.setFontScale(0.5.toFloat, 0.6.toFloat)
  okButton.addListener(new okListener(this.start, xySlider, zSlider, viewButtons))

  resetButton.addListener(new resetListener(this.start))
  saveButton.addListener(new saveListener(this.start))
  loadButton.addListener(new loadListener(this.start))

  val viewListener: viewChangeListener = new viewChangeListener(this.start, viewButtons, xySlider)

  completeViewButton.addListener(viewListener)
  focusedViewButton.addListener(viewListener)

  viewButtons.setMinCheckCount(1)
  viewButtons.setMaxCheckCount(1)
  viewButtons.setChecked("focused")

  private val table: Table = new Table()
  private val stage: Stage = new Stage(new ExtendViewport(Manager.worldWidth, Manager.worldWidth))
  stage.setViewport(Manager.viewport)
  val style = new ImageButtonStyle();
  style.up = null;
  style.down = null;
  val background = new ImageButton(style);
  background.addListener(new ClickListener {
    override def clicked(event: InputEvent, x: Float, y: Float) {
      stage.unfocusAll();
    }
  })

  background.setSize(1000, 1000)
  animateInBackground(Manager.animationPath)

  stage.addActor(background)
  table.defaults().pad(5)

  table.add(nameLabel).center()
  table.add(nameField).center()
  table.row()
  table.add(xyLabel).center()
  table.add(xySlider).center()
  table.add(xyValue).center()
  table.row()
  table.add(zLabel).center()
  table.add(zSlider).center().padRight(0)
  table.add(zValue).center().padLeft(0)
  table.row()
  table.add(okButton).pad(20).center()
  table.add(statusLabel).pad(20).right()
  table.row()
  table.row()
  table.row()
  table.add(viewmodeLabel).center()
  table.add(focusedViewButton).center().padRight(0)
  table.add(completeViewButton).center()
  table.row()
  table.add(resetButton).size(75, 20).center()
  table.add(saveButton).size(75, 20).center().padRight(0)
  table.add(loadButton).size(75, 20).center()
  table.row()
  table.add(scoreboardLabel).size(130, 20).center()
  table.add(settingsLabel).size(130, 20).center()
  table.add(startGameLabel).size(130, 20).center()
  stage.addActor(table)
  table.defaults().pad(10)
  table.setFillParent(true)

  Gdx.input.setInputProcessor(stage);

  def getStage: Stage = this.stage

  def pause(): Unit = {}

  def resume(): Unit = {}

  def dispose(): Unit = {}

  def hide(): Unit = stage.setKeyboardFocus(null)

  def show(): Unit = {
    xyValue.setText(this.start.game.getWidth.toString)
    xySlider.setValue(this.start.game.getWidth.toFloat)
    zValue.setText(this.start.game.getDepth.toString)
    zSlider.setValue(this.start.game.getDepth.toFloat)
    Gdx.input.setInputProcessor(this.stage)
  }

  def resize(w: Int, h: Int): Unit = {
    Manager.viewport.update(Gdx.graphics.getWidth, Gdx.graphics.getHeight);
    Manager.camera.update();
  }

  def render(d: Float): Unit = {
    batch.setProjectionMatrix(Manager.camera.combined)
    animationBatch.setProjectionMatrix(Manager.animationCamera.combined)

    Gdx.gl.glClearColor(255, 255, 255, 1)
    Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)

    // If an animation has been defined, render it
    animationTexture match {
      case Some(_) =>
        animationBatch.begin()
        backgroundAnimation.draw(animationBatch, 1)
        animationBatch.end()
      case _ =>
    }

    // Draw rectangle behind settings so that the player can read them more easily
    textBackground.begin(ShapeType.Filled)
    textBackground.setColor(255, 255, 255, 1)
    textBackground.rect(250, 350, 500, 300)
    textBackground.end()

    // Draw the actual stage which contains the settings
    batch.begin()
    stage.draw()
    stage.act()
    batch.end()
  }
}