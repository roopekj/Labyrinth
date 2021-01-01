package gdx.scala.labyrinth

class Cell() {
  var north: Option[Cell] = None
  var west: Option[Cell] = None
  var south: Option[Cell] = None
  var east: Option[Cell] = None
  var up: Option[Cell] = None
  var down: Option[Cell] = None

  var adjacentNorth: Option[Cell] = None
  var adjacentEast: Option[Cell] = None
  var adjacentSouth: Option[Cell] = None
  var adjacentWest: Option[Cell] = None

  var solvedFrom: Option[Cell] = None
  var contents: Option[Item] = None
  var path: Option[Item] = None
  var isBeginning: Boolean = false
  var isEnd: Boolean = false

  var xCoord: Float = 0
  var yCoord: Float = 0

  def getSolveConnection: Option[Cell] = this.solvedFrom

  def setSolveConnection(cell: Cell): Unit = this.solvedFrom = Some(cell)

  def adjacent2D: Array[Option[Cell]] = Array(north, east, south, west)

  def adjacent3D: Array[Option[Cell]] = Array(north, east, south, west, up, down)

  def howMany2DConnections: Int = adjacent2D.count(_.isDefined)

  def howMany3DConnections: Int = adjacent3D.count(_.isDefined)

  def neighborInDirection(d: Direction.Direction): Option[Cell] = {
    d match {
      case Direction.NORTH => this.north
      case Direction.EAST => this.east
      case Direction.SOUTH => this.south
      case Direction.WEST => this.west
      case Direction.UP => this.up
      case Direction.DOWN => this.down
      case _ => None
    }
  }

  def neighborInDirection(d: Int): Option[Cell] = {
    this.neighborInDirection(Manager.movementActions(d))
  }

  def getContent: Option[Item] = this.contents

  def setContent(item: Item): Unit = this.contents = Some(item)

  def getPath: Option[Item] = this.path

  def setPath(item: Option[Item]): Unit = this.path = item

  def formatForFile: Char = {
    val fingerprint = Array[Boolean](
      east.isEmpty,
      south.isEmpty,
      down.isEmpty,
      isBeginning,
      isEnd
    )
    val (keys, values) = Manager.fingerprints.filterKeys(key => key sameElements fingerprint).toSeq.unzip
    values.length match {
      case l if l > 0 => values.head
      case _ => throw new UnknownError("No fingerprint found for cell")
    }
  }
}
