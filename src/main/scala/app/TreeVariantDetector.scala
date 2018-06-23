package app

import scala.collection.mutable

class TreeVariantDetector(nodeSequences: Map[Node, String], edges: List[Edge]) {
  private val table = new mutable.HashMap[Node, List[DiffSummary]]()

  def execute(): Map[Node, List[DiffSummary]] = {
    edges.find(_.parent.name == "root") match {
      case Some(rootEdge) =>
        // The root edge's child is the real start of the process
        table.put(rootEdge.child, List())
        process(rootEdge.child)
      case None =>
    }
    table.toMap
  }

  private def process(node: Node): Unit = {
    val children = edges.filter(_.parent == node).map(_.child)

    if(children.nonEmpty) {
      children.foreach(child => {
        table.put(child, SequenceDiffLocator.find(nodeSequences(node), nodeSequences(child)))
        process(child)
      })
    }
  }

}
