package gdx.scala.labyrinth

class Stairs(dir: Direction.Direction) extends Item {
  val direction: Direction.Direction = dir
  val filename: String = {
    dir match {
      case Direction.UP => "stairsUp.png"
      case Direction.DOWN => "stairsDown.png"
      case _ => "error.png"
    }
  }
}
