package app

import java.util.logging.Logger

import scala.collection.mutable

class TreeVariantDetector(nodeSequences: Map[Node, String], edges: List[Edge]) {
  private val table = new mutable.HashMap[Node, List[DiffSummary]]()
  val logger = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME)

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
        val nodeSequence = nodeSequences(node)
        val childSequence = nodeSequences(child)
        val summaries = SequenceDiffLocator.find(nodeSequence, childSequence)
        table.put(child, summaries)
        process(child)
      })
    }
  }

}
