package app

import scala.collection.mutable
import scala.io.Source

case class Node(name: String)
case class Edge(parent: Node, child: Node)

class ParseTreeTable {
  val nodes = new mutable.ListBuffer[Node]
  val edges = new mutable.ListBuffer[Edge]

  def execute(filePathName: String): (List[Node], List[Edge]) = {
    val table = Source.fromFile(filePathName).getLines()

    table.foreach(line => {
      val cs = line.trim.split("\\s+")
      if(cs.size > 1) {
        val parent = findNodeByName(cs(0))
        val child = findNodeByName(cs(1))
        edges += Edge(parent, child)
      }
    })

    (nodes.toList, edges.toList)
  }

  private def findNodeByName(name: String) = {
    nodes.find(_.name == name) match {
      case Some(n) => n
      case None =>
        val n = Node(name)
        nodes += n
        n
    }
  }
}
