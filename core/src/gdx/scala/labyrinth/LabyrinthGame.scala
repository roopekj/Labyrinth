package gdx.scala.labyrinth

import java.time.Instant
import java.time.Duration
import java.io.{FileOutputStream, OutputStream, OutputStreamWriter, PrintWriter}
import java.nio.file.{FileAlreadyExistsException, Files, Path, Paths}
import scala.concurrent._
import ExecutionContext.Implicits.global

import scala.io.Codec;

class LabyrinthGame(private var width: Int, private var height: Int, private var depth: Int, private val start: Start) {
  private var movesMade = 1 // The last move made towards the end is not counted so we start counting from one
  private var solution: Array[Cell] = Array[Cell]() // The current solution to the labyrinth as seen from the player's current location
  private val pathItem: Item = new markedRoute // The item placed in a cell which belongs on the solution
  private var playerName: String = _

  private var scores: Array[Score] = Array[Score]() // The list of high scores

  private var startTime: Option[Instant] = None
  private var endTime: Option[Instant] = None

  var hasConceded: Boolean = false
  var hasWon: Boolean = false

  val stairsDown: Stairs = new Stairs(Direction.DOWN)
  val stairsUp: Stairs = new Stairs(Direction.UP)

  val player: Player = new Player(new Cell(), 0)
  var floors: Array[Floor] = Array()

  private var currentViewmode: String = ""

  private val generator: Generator = new modPrim(this) // The generator used when generating labyrinths for this game
  private val solver: Solver = new breadthFirst() // The solver used when solving the labyrinths in this game

  private var generationFuture: Future[Unit] = Future {}
  private var solvingFuture: Future[Unit] = Future {}

  /**
   * Places the player in the current labyrinth's designated start-position
   */
  def placePlayer: Unit = {
    val startCells = floors.flatMap(_.getCells.flatten.filter(c => c.isBeginning))
    require(startCells.length == 1)

    this.player.setCurrentCell(startCells(0))

    this.floors.find(_.hasStart) match {
      case Some(startFloor) =>
        this.player.setCurrentFloor(this.floors.indexOf(startFloor))
      case _ =>
    }
  }

  /**
   * Initializes the game with the given parameters
   *
   * @param name
   * @param xy
   * @param z
   * @param viewmode
   */
  def initGame(name: String, xy: Int, z: Int, viewmode: String): Unit = {
    this.setConfiguration(name, xy, z, viewmode)

    this.floors = new Array[Floor](this.depth)
    generationFuture.andThen { case _ => this.generateLabyrinth }
  }

  def setConfiguration(name: String, xy: Int, z: Int, viewmode: String): Unit = {
    this.playerName = name
    this.height = xy
    this.width = xy
    this.depth = z
    this.currentViewmode = viewmode
  }

  /** Resets the player's position and variables used to track their progress throughout the run **/
  def resetGame: Unit = {
    this.placePlayer
    this.hasWon = false
    this.hasConceded = false
    this.startTime = None
    this.endTime = None
  }

  /** Sets the starting time of the current run **/
  def initStartTime: Unit = this.startTime = Some(Instant.now)

  /** Generates a new labyrinth using the current generator **/
  def generateLabyrinth: Unit = {
    if (this.generationFuture.isCompleted) {
      this.generationFuture = Future {
        (0 until this.depth).foreach(n => {
          this.floors(n) = new Floor(this.width, this.height)
        })
        this.start.getSettingsScreen.toggleStatus
        this.generator.generate(floors, this.player)
      }

      this.generationFuture.onSuccess {
        case _ =>
          this.resetGame
          this.start.getSettingsScreen.toggleStatus
      }
    }
  }

  /** Solves the current labyrinth starting from the player's current location **/
  def solveLabyrinth: Unit = {
    if (this.solvingFuture.isCompleted) {
      this.solvingFuture = Future {
        this.solution = this.solver.solve(this.floors, this.player.getCurrentCell)
      }

      this.solvingFuture.onSuccess {
        case _ => this.solution.foreach(_.setPath(Some(pathItem)))
      }
    }
  }

  def createFileIfNotExists(path: Path): Unit = {
    try {
      Files.createFile(path)
    } catch {
      case _: FileAlreadyExistsException =>
      case _: Any => System.exit(0)
    }
  }

  /** Read the saved labyrinth and reset the game with it's specifications */
  def readLabyrinth: Unit = {
    if (!Files.exists(Paths.get(Manager.savedLabyrinthPath)) && !this.isReady) return

    val bufferedSource = io.Source.fromFile(Manager.savedLabyrinthPath)(Codec("UTF-8"))
    val newFloors = bufferedSource.getLines.mkString("\n").split("\n\n").reverse

    val structure = Manager.fingerprints.map(_.swap)

    newFloors.indices.foreach(floorIndex => {
      val rows = newFloors(floorIndex).split("\n")
      val newFloor = new Floor(rows.length, rows.length)
      this.floors(newFloors.length - 1 - floorIndex) = newFloor
      newFloor.getCells.indices.foreach(rowIndex => {
        val newRow = newFloor.getCells(rowIndex)
        newRow.indices.foreach(cellIndex => {
          val cell = newRow(cellIndex)
          val cellStructure = structure(rows(rowIndex)(cellIndex))
          if (!cellStructure(0)) this.generator.connect(cell, this.findAdjacent(cell, Direction.EAST).get, Direction.EAST)
          if (!cellStructure(1)) this.generator.connect(cell, this.findAdjacent(cell, Direction.SOUTH).get, Direction.SOUTH)
          if (!cellStructure(2)) this.generator.connect(cell, this.findAdjacent(cell, Direction.DOWN).get, Direction.DOWN)
          if (cellStructure(3)) cell.isBeginning = true
          if (cellStructure(4)) cell.isEnd = true
        })
      })
    })

    bufferedSource.close

    val rowLength = newFloors(0).split("\n").length

    this.setConfiguration(this.playerName, rowLength, newFloors.length, this.currentViewmode)
    this.resetGame
    this.generator.setAdjacentCells(floors)
  }

  /** Writes the current labyrinth to Manager.savedLabyrinthPath */
  def writeLabyrinth: Unit = {
    if (!this.isReady) return
    this.createFileIfNotExists(Paths.get(Manager.savedLabyrinthPath))

    val output = floors.map(floor => floor.formatToFile).mkString("\n")
    val stream: OutputStream = new FileOutputStream(Manager.savedLabyrinthPath)
    new PrintWriter(new OutputStreamWriter(stream, "UTF-8")) {
      write(output); close()
    }
  }

  /** Move the player in the specified direction if possible */
  def movePlayer(direction: Direction.Direction): Unit = {
    val neighbor = this.player.getCurrentCell.neighborInDirection(direction)
    neighbor match {
      case Some(adjacentCell) if adjacentCell.isEnd => {
        hasWon = true
        this.finishGame
      }
      case Some(adjacentCell) => {
        player.setCurrentCell(adjacentCell)
        adjacentCell.contents match {
          case Some(a) if a.isInstanceOf[Stairs] => {
            val direction = a.asInstanceOf[Stairs].direction
            val targetCell = findAdjacent(adjacentCell, direction)
            if (targetCell.isDefined) {
              player.setCurrentCell(targetCell.get)
              direction match {
                case Direction.DOWN => player.setCurrentFloor(player.getCurrentFloor + 1)
                case Direction.UP => player.setCurrentFloor(player.getCurrentFloor - 1)
                case _ =>
              }
            }
          }
          case _ =>
        }
      }
      case _ =>
    }
    this.movesMade += 1
  }

  /** Concede the game and render a path to the end from the player's location **/
  def concede(): Unit = {
    this.hasConceded = true
    this.solveLabyrinth
  }

  /** Writes the current scores-map to Manager.scoresPath */
  def writeScores: Unit = {
    new PrintWriter(Manager.scoresPath) {
      write(scores.map(a => a.printScore).mkString("\n")); close()
    }
  }

  /** Reads scores from Manager.scoresPath and updates scores-map */
  def readScores: Unit = {
    this.createFileIfNotExists(Paths.get(Manager.scoresPath))

    var output: Array[Score] = Array[Score]()
    val bufferedSource = io.Source.fromFile(Manager.scoresPath)

    bufferedSource.getLines.filter(_ != "").map(line => line.split("\t").map(_.trim)).foreach(line => {
      output :+= new Score(line(0), line(1).toLong, line(2).toInt, line(3).toInt, line(4))
    })

    bufferedSource.close
    this.scores = output
  }

  /** Checks whether or not the score s was a new high score for player n, and updates the Manager.scoresPath and scores-map if necessary */
  def checkForHighscore(n: String, s: Long): Unit = {
    if (this.hasConceded) return
    val previous: Option[Score] = this.scores.find(a => a.name == n && a.xy == this.height && a.z == this.depth && a.view == this.currentViewmode)

    previous match {
      case Some(previousScore) =>
        if (previousScore.score < s) {
          previousScore.score = s
          writeScores
        }
      case _ =>
        this.scores :+= new Score(n, s, this.height, this.depth, this.currentViewmode)
        writeScores
    }

  }

  /** Calculate the player's score and end the run. The player can no longer continue their run after this is called */
  def finishGame: Unit = {
    this.endTime = Some(Instant.now)
    val timeTakenMultiplier = (this.startTime, this.endTime) match {
      case (Some(a), Some(b)) =>
        val difference: Long = Duration.between(a, b).toMillis
        val divisor: Long = 1000000.toLong
        if (difference != 0) divisor / difference else 0
      case _ => 0
    }
    this.checkForHighscore(this.playerName, (1000 / this.movesMade) * timeTakenMultiplier)
    this.startTime = None
  }

  /**
   * Find a cell's neighbor in a direction
   *
   * @param c
   * @param d
   * @return Cell in direction d, if any
   */
  def findAdjacent(c: Cell, d: Direction.Direction): Option[Cell] = {
    floors.indices.foreach(x => {
      val f: Floor = floors(x)
      f.getCells.indices.foreach(m => {
        val row = f.getCells(m)
        if (row.contains(c)) {
          val n = row.indexOf(c)
          d match {
            case Direction.SOUTH => if (m < f.getCells.length - 1) return Option(f.getCells(m + 1)(n))
            case Direction.EAST => if (n < row.length - 1) return Option(row(n + 1))
            case Direction.NORTH => if (m > 0) return Option(f.getCells(m - 1)(n))
            case Direction.WEST => if (n > 0) return Option(row(n - 1))
            case Direction.DOWN => if (x < floors.length - 1) return Option(floors(x + 1).getCells(m)(n))
            case Direction.UP => if (x > 0) return Option(floors(x - 1).getCells(m)(n))
          }
        }
      })
    })

    None
  }

  def isReady: Boolean = this.generationFuture.isCompleted && this.solvingFuture.isCompleted // Returns whether or not the labyrinth is ready
  def isNewGame: Boolean = this.startTime.isEmpty

  def getWidth: Int = this.width

  def getHeight: Int = this.height

  def getDepth: Int = this.depth

  def getScores: Array[Score] = this.scores

  def getViewmode: String = this.currentViewmode

  def setScores(s: Array[Score]): Unit = this.scores = s

  // The initial template for the game
  this.initGame("Player", width, depth, "focused")
}