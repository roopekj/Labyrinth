package gdx.scala.labyrinth

class Score(val name: String, var score: Long, val xy: Int, val z: Int, val view: String) {
  /**
   * Name - player's name
   * Score - score achieved
   * xy - played labyrinth's width/height
   * z - played labyrinths number of floors
   * view - name of view used
   */

  def printScore: String = s"$name\t$score\t$xy\t$z\t$view"
}
