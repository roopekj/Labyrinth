package gdx.scala.labyrinth

class Player(cell: Cell, floor: Int) {
  private var currentCell: Cell = cell
  private var currentFloor: Int = floor

  def getCurrentCell: Cell = this.currentCell

  def setCurrentCell(c: Cell): Unit = this.currentCell = c

  def getCurrentFloor: Int = this.currentFloor

  def setCurrentFloor(f: Int): Unit = this.currentFloor = f
}
