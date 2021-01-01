package gdx.scala.labyrinth

import com.badlogic.gdx.graphics.Texture

abstract class Item {
  val filename: String
  private var texture: Option[Texture] = None

  def getTexture: Texture = {
    this.texture match {
      case Some(texture) => texture
      case _ =>
        this.texture = Some(new Texture(this.filename))
        this.texture.get
    }
  }
}
