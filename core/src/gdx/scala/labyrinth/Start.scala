package gdx.scala.labyrinth

import com.badlogic.gdx.Game
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input.Keys
import com.badlogic.gdx.Screen
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.physics.bullet.Bullet
import com.badlogic.gdx.audio.Music

import scala.collection.mutable.Map

class Start extends Game {
  private[labyrinth] var batch: SpriteBatch = _
  private[labyrinth] val game: LabyrinthGame = new LabyrinthGame(8, 8, 3, this)
  private var focusedview: Option[ViewMode] = _
  private var completeview: Option[ViewMode] = _
  private var gamescreen: Option[ViewMode] = _
  private var settingsscreen: Option[SettingsScreen] = _
  private var scoreboardscreen: Option[ScoreboardScreen] = _
  private var currentMusic: Option[Music] = _

  // Filename -> Music-object
  private lazy val musicFiles: Map[String, Music] = Map()

  // name -> Screen-object
  private lazy val views: Map[String, Option[ViewMode]] = Map(
    "focused" -> focusedview,
    "complete" -> completeview
  )

  override def create() {
    Bullet.init()
    game.readScores
    batch = new SpriteBatch
    this.focusedview = Some(new FocusedViewMode(this))
    this.completeview = Some(new CompleteViewMode(this))
    this.gamescreen = this.focusedview
    this.settingsscreen = Some(new SettingsScreen(this))
    this.scoreboardscreen = Some(new ScoreboardScreen(this))
    this.setScreen(this.getSettingsScreen)
    this.setMusicTo(Manager.menuMusicPath)
    this.game.generateLabyrinth
  }

  /** Handles switching between screens before the current screen is called to render its contents **/
  override def render(): Unit = {
    if (this.screen != this.getSettingsScreen || !isWriting) {
      if (switchingToGameScreen) {
        this.toggleScreen(this.getGameScreen)
        if (this.game.isNewGame) this.game.initStartTime
        this.setMusicTo(Manager.gameMusicPath)
      }

      if (switchingToSettingsScreen) {
        this.toggleScreen(this.getSettingsScreen)
        this.setMusicTo(Manager.menuMusicPath)
      }

      if (switchingToScoresScreen) {
        this.toggleScreen(this.getScoreboardScreen)
        this.setMusicTo(Manager.menuMusicPath)
      }
    } else {
      /** If the player is writing or in the settings screen, pressing ESC should remove focus as a secondary way
       * of exiting the text field */
      if (Gdx.input.isKeyJustPressed(Keys.ESCAPE)) this.getSettingsScreen.getStage.setKeyboardFocus(null)
    }

    super.render()
  }

  def toggleScreen(screen: Screen): Unit = {
    this.getScreen.dispose()
    this.setScreen(screen)
  }

  /**
   * @return Whether or not it is appropriate to switch to the respective screen at this time
   */
  private def switchingToGameScreen: Boolean =
    Gdx.input.isKeyJustPressed(Keys.ESCAPE) &&
    this.screen != this.getGameScreen &&
    this.game.isReady &&
    !this.game.hasWon &&
    this.getSettingsScreen.currentlySelectedViewmode == this.game.getViewmode

  private def switchingToSettingsScreen: Boolean = this.getScreen != this.getSettingsScreen && Gdx.input.isKeyJustPressed(Keys.M)

  private def switchingToScoresScreen: Boolean =
    (this.game.hasWon && this.getScreen == this.getGameScreen) ||
      (this.getScreen != this.getScoreboardScreen && Gdx.input.isKeyJustPressed(Keys.T))

  /** Change the current view mode by name **/
  def toggleGameView(s: String): Unit = {
    val newScreen: Option[ViewMode] = this.views(s)
    this.gamescreen = newScreen
  }

  /** Play the currently selected track **/
  def playMusic: Unit = {
    this.currentMusic match {
      case Some(music) => music.play()
      case _ =>
    }
  }

  /** Stop the currently selected track **/
  def stopMusic: Unit = {
    this.currentMusic match {
      case Some(music) => music.stop()
      case _ =>
    }
  }

  /** Sets the current music to the song found at the specified filepath. Creates music-object if needed. */
  def setMusicTo(filepath: String): Unit = {

    if (!musicFiles.keys.toSeq.contains(filepath)) {
      // The game has not yet created a music-object for this file
      val newMusic = Gdx.audio.newMusic(Gdx.files.internal(filepath))
      newMusic.setLooping(true)
      musicFiles(filepath) = newMusic
    }

    val selectedMusic = Some(musicFiles(filepath))
    if (currentMusic != selectedMusic) {
      stopMusic
      this.currentMusic = Some(musicFiles(filepath))
      playMusic
    }
  }

  /** @return Whether or not the player is focused on a text-field **/
  def isWriting: Boolean = this.getSettingsScreen.getStage.getKeyboardFocus != null

  def getSettingsScreen: SettingsScreen = this.settingsscreen.get

  def getScoreboardScreen: ScoreboardScreen = this.scoreboardscreen.get

  def getGameScreen: Screen = this.gamescreen.get
}