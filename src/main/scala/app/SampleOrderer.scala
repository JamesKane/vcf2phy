package app

class SampleOrderer(nodes: List[Node], edges: List[Edge]) {

  def execute(): List[String] = {
    nodes.find(_.name == "root") match {
      case Some(n) =>
        process(n)
      case None =>
        List()
    }
  }

  private def process(n: Node): List[String] = {
    val children = edges.filter(e => n == e.parent).map(_.child)
    if(children.isEmpty) {
      List(n.name)
    } else {
      children.flatMap(process)
    }
  }

}
