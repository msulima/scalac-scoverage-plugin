package scoverage.report

import scala.io.Source

import _root_.scoverage.{MeasuredFile, Statement}

/** @author Stephen Samuel */
class CodeGrid(mfile: MeasuredFile) {

  case class Cell(char: Char, var status: StatementStatus)

  private val lineBreak = System.getProperty("line.separator").toCharArray

  // note: we must reinclude the line sep to keep source positions correct.
  private val lines = source(mfile).split(lineBreak).map(line => (line.toCharArray ++ lineBreak).map(Cell(_, NoData)))

  // useful to have a single array to write into the cells
  private val cells = lines.flatten

  // for all statements in the source file we build highlighted data
  mfile.statements.foreach(highlight)

  val highlighted: String = {
    var lineNumber = 1
    val code = lines map (line => {
      var style = cellStyle(NoData)
      val sb = new StringBuilder
      sb append lineNumber + " "
      lineNumber = lineNumber + 1
      sb append spanStart(NoData)
      line.map(cell => {
        val style2 = cellStyle(cell.status)
        if (style != style2) {
          sb append "</span>"
          sb append spanStart(cell.status)
          style = style2
        }
        sb.append(cell.char)
      })
      sb append "</span>"
      sb.toString
    }) mkString ""
    s"<pre style='font-size: 12pt; font-family: courier;'>$code</pre>"
  }

  private def source(mfile: MeasuredFile): String = Source.fromFile(mfile.source).mkString

  private def highlight(stmt: Statement) {

    for ( k <- stmt.start until stmt.end ) {
      if (k < cells.size) {
        if (stmt.isInvoked) {
          cells(k).status = Invoked
        } else if (cells(k).status == NoData) {
          cells(k).status = NotInvoked
        }
      }
    }
  }

  private def spanStart(status: StatementStatus): String = s"<span style='${cellStyle(status)}'>"

  private def cellStyle(status: StatementStatus): String = {
    val GREEN = "#AEF1AE"
    val RED = "#F0ADAD"
    status match {
      case Invoked => s"background: $GREEN"
      case NotInvoked => s"background: $RED"
      case NoData => ""
    }
  }
}

