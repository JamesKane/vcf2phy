package app

import java.io.{BufferedWriter, FileOutputStream, OutputStreamWriter}
import java.nio.charset.Charset
import java.util.logging.Logger


/**
  * This report builder takes the results from dnamlk to build out a bi-clustered call matrix
  * to verify results prior to importing the tree into reporting systems
  */
object ClusteredReport extends App {

  val logger = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME)

  // TODO:  Fix these

  if (args.length < 6 || args(1) == "-h") {
    println(s"Usage: ${args(0)} vcf_file_path tree_table_path node_sequences_path matrix_path json_tree_path")
  } else {
    val vcfPathName = args(1)
    val treePathName = args(2)
    val nodeSequencesPathName = args(3)
    val matrixPathName = args(4)
    val jsonTreePathName = args(5)

    val matrix = new MatrixBuilder(vcfPathName).parseVCF()
    logger.info("Validating taxa have calls")
    val names = matrix.sampleNames.filter(taxa => matrix.rows.map(_.status(taxa)).exists(IUPAC.validBases.contains))
    if (names.size != matrix.sampleNames.size) {
      val removed = matrix.sampleNames.toSet.diff(names.toSet)
      logger.warning("Sample(s) removed from cohort: " + removed.mkString(", "))
    }

    var fw: BufferedWriter = null
    logger.info("Constructing raw Matrix")
    val (nodes, edges) = new ParseTreeTable().execute(treePathName)
    val nodeSequences = new NodeSequenceLoader(nodes).execute(nodeSequencesPathName)
    logger.info("Finding branch variant sites")
    val nodeDiffs = new TreeVariantDetector(nodeSequences, edges).execute()

    val orderedSamples = new SampleOrderer(nodes, edges).execute().map(s => matrix.sampleNames.find(_.contains(s)).getOrElse(s))
    logger.info("Ordering rows")
    val orderedRows = buildIterList(edges, nodeDiffs)
    logger.info("Writing Matrix")
    try {
      fw = new BufferedWriter(
        new OutputStreamWriter(
          new FileOutputStream(matrixPathName), Charset.forName("UTF-8").newEncoder()))
      fw.write(",,,," + orderedSamples.mkString(","))
      fw.newLine()
      orderedRows.foreach(summary => {
        if (summary.index > matrix.rows.size) {
          logger.severe("Node sequences do not match gVCF filters!")
        } else {
          val row = matrix.rows(summary.index)
          writeMatrixRow(fw, orderedSamples, row, summary.from, summary.to)
        }
      })
    } finally {
      fw.close()
    }

    logger.info("Exporting tree")
    val tree = new TreeBuilder(edges, nodeDiffs, matrix.rows.map(r => (r.contig, r.pos))).execute()
    try {
      fw = new BufferedWriter(
        new OutputStreamWriter(
          new FileOutputStream(jsonTreePathName), Charset.forName("UTF-8").newEncoder()))

      fw.write(upickle.default.write(tree))
    } finally {
      fw.close()
    }
  }

  def writeBranch(fw: BufferedWriter, root: Node, edges: List[Edge], diffs: Map[Node, List[DiffSummary]], matrix: MatrixDAO, i: Int): Unit = {
    val children = edges.filter(_.parent == root).map(_.child)

    //if(children.nonEmpty) {
    val events = diffs(root).map(d => {
      val row = matrix.rows(d.index)
      s"${row.contig}:${row.pos} ${d.from}->${d.to}"
    })
    fw.write("\t" * i + s"${root.name}\t" + events.mkString("|"))
    fw.newLine()
    children.foreach(c => writeBranch(fw, c, edges, diffs, matrix, i + 1))
    //}
  }

  private def writeMatrixRow(fw: BufferedWriter, orderedSamples: List[String], row: RowDAO, anc: String, der: String) = {
    fw.write(row.contig + "," + row.pos + "," + row.anc + "," + row.der + ",")
    // TODO: Sample name may be truncated when formatted for phylip
    fw.write(orderedSamples.map(n => row.status.getOrElse(n, "?")).mkString(","))
    fw.newLine()
  }

  private def buildIterList(edges: List[Edge], nodeDiffs: Map[Node, List[DiffSummary]]): List[DiffSummary] = {
    edges.find(_.parent.name == "root") match {
      case Some(n) =>
        nodeDiffs(n.child) ++ bfs(n.child, edges, nodeDiffs)
      case None => List()
    }
  }

  private def bfs(node: Node, edges: List[Edge], nodeDiffs: Map[Node, List[DiffSummary]]): List[DiffSummary] = {
    val chidren = edges.filter(_.parent == node).map(_.child)
    if (chidren.isEmpty) {
      logger.fine(s"Terminal ${node.name} has been processed.")
      nodeDiffs(node)
    } else {
      nodeDiffs(node) ++ chidren.flatMap(c => bfs(c, edges, nodeDiffs))
    }
  }
}
