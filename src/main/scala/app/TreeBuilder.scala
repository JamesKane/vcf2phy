package app

import java.text.SimpleDateFormat
import java.util.Calendar

import upickle.default.{macroRW, ReadWriter => RW}

import scala.collection.mutable.ListBuffer

case class TreeEvent(contig: String, start: Int, anc: String, der: String)

object TreeEvent {
  implicit def rw: RW[TreeEvent] = macroRW
}

case class TreeNode(name: String, events: List[TreeEvent], children: ListBuffer[TreeNode]) {
  lazy val age: Double = events.size * 120 + children.map(_.age).sum / children.size.toDouble
}

object TreeNode {
  implicit def rw: RW[TreeNode] = macroRW
}

case class Tree(lastUpdate: String, motd: String, root: TreeNode)

object Tree {
  implicit def rw: RW[Tree] = macroRW
}

class TreeBuilder(edges: List[Edge], nodes: Map[Node, List[DiffSummary]], loci: List[(String, Int)]) {

  def execute(): Tree = {
    val root = edges.find(_.parent.name == "root").get.child
    val tree = buildFrom(root, None)

    val today = Calendar.getInstance.getTime
    val dateFormat = new SimpleDateFormat("MM-dd-YYYY")

    Tree(dateFormat.format(today), "Version 2018.0.1", tree.get)
  }

  private def buildFrom(root: Node, parent: Option[TreeNode]): Option[TreeNode] = {
    val children = edges.filter(_.parent == root)
    val events = nodes.getOrElse(root, List()).map(xs => {
      val locus = loci(xs.index)
      TreeEvent(locus._1, locus._2, xs.from, xs.to)
    })

    if (parent.isEmpty || events.nonEmpty) {
      val node = TreeNode(root.name, events, ListBuffer())
      node.children ++= flattenDescendants(children).flatMap(buildFrom(_, Some(node)))
      Some(node)
    } else {
      // dnamlk outputs a binary tree structure.  Empty events mean the current
      // node holds children who are direct descendants of this node (and this may recurse)
      parent.get.children ++= flattenDescendants(children).flatMap(buildFrom(_, parent))
      None
    }
  }

  private def flattenDescendants(descEdges: List[Edge]): List[Node] = {
    val (a, b) = descEdges.partition(e => nodes(e.child).nonEmpty)
    a.map(_.child) ++ b.flatMap(c => flattenDescendants(edges.filter(_.parent == c.child)))
  }

}
