package gdx.scala.labyrinth

class Floor(private val width: Int, private val height: Int) {
  private val cells: Array[Array[Cell]] = new Array[Array[Cell]](height)

  (0 until width).foreach(m => {
    val row: Array[Cell] = new Array[Cell](width)
    this.cells(m) = row
    (0 until height).foreach(n => {
      row(n) = new Cell()
    })
  })

  def getCells: Array[Array[Cell]] = this.cells

  /**
   * @return This floor formatted as a string
   */
  def formatToFile: String = {
    var output: String = ""
    for (row <- cells) {
      output += row.map(cell => cell.formatForFile).mkString + "\n"
    }
    output
  }

  /**
   * @return Whether or not this floor contains the beginning of the labyrinth
   */
  def hasStart: Boolean = getCells.flatten.count(c => c.isBeginning) == 1
}
