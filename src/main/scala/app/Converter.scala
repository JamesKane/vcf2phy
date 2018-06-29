package app

import java.io.{BufferedWriter, FileOutputStream, OutputStreamWriter}
import java.nio.charset.Charset
import java.util.logging.Logger


object Converter extends App {
  val logger = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME)

  // TODO:  Replace this with a more robust argument system
  if (args.length < 4 || args(1) == "-h") {
    println(s"Usage: ${args(0)} vcf_file_path phy_output_path index_out_path")
  } else {
    val inputPathName = args(1)
    val outputPathName = args(2)
    val indexPathName = args(3)

    val matrix = new MatrixBuilder(inputPathName).parseVCF()
    logger.info("Validating taxa have calls")
    val names = matrix.sampleNames.filter(taxa => matrix.rows.map(_.status(taxa)).exists(IUPAC.validBases.contains))
    if (names.size != matrix.sampleNames.size) {
      val removed = matrix.sampleNames.toSet.diff(names.toSet)
      logger.warning("Sample(s) removed from cohort: " + removed.mkString(", "))
    }

    logger.info("Constructing PHYLIP data")
    var fw: BufferedWriter = null
    try {
      fw = new BufferedWriter(
        new OutputStreamWriter(
          new FileOutputStream(outputPathName), Charset.forName("UTF-8").newEncoder()
        )
      )


      fw.write(s"${names.length} ${matrix.rows.length}" + System.lineSeparator())
      names.foreach(k => {
        // Phylip format requires a 10 character row label for the organism
        val label = k.padTo(10, " ").slice(0, 10).mkString("")

        // Collect all the calls for the organism to a single string
        // TODO: Consider interleaving with a defined column count and group spacing
        // TODO: This works with Phylip itself, but raxml-ng says the format is broken.  Fix it.
        val calls = matrix.rows.map(r => r.status(k)).mkString("")

        fw.write(s"$label$calls" + System.lineSeparator())
      })
    } finally {
      fw.close()
    }

    logger.info("Exporting site map")
    var indexFW: BufferedWriter = null
    try {
      indexFW = new BufferedWriter(
        new OutputStreamWriter(
          new FileOutputStream(indexPathName), Charset.forName("UTF-8").newEncoder()
        )
      )
      matrix.rows.foreach(row => {
        indexFW.write(s"${row.contig}:${row.pos}")
        indexFW.newLine()
      })
    } finally {
      indexFW.close()
    }
  }
}
