package app

import scala.collection.mutable
import scala.io.Source

class NodeSequenceLoader(nodes: List[Node]) {

  //    96        GCGCGcTACG GACCaGCtgg TCGAGAtCtG cGCgGcGGGT CTTCCCAAAA TAgGAgTCGC
  def execute(filePathName: String): Map[Node, String] = {
    val table = new mutable.HashMap[Node, String]()
    Source.fromFile(filePathName).getLines().foreach(line => {
      // First 13 characters are the node
      val (taxa, sequence) = (line.take(13).trim, line.slice(13, line.length).replaceAll(" ", ""))
      table.filterKeys(_.name == taxa).headOption match {
        case Some((node, seq)) =>
          table.put(node, seq ++ sequence)
        case None =>
          nodes.find(_.name == taxa) match {
            case Some(node) =>
              table.put(node, sequence)
            case None =>
              // TODO: This is actually an error, but we'll ignore for now
          }
      }
    })

    table.toMap
  }

}
