package app

import java.io.{BufferedWriter, FileOutputStream, OutputStreamWriter}
import java.nio.charset.Charset
import java.util.logging.Logger


object Converter extends App {
  val logger = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME)
  val bases = Set("A", "C", "G", "T")

  // TODO:  Replace this with a more robust argument system
  if (args.length < 3 || args(1) == "-h") {
    println(s"Usage: ${args(0)} vcf_file_path phy_output_path")
  } else {
    val inputPathName = args(1)
    val outputPathName = args(2)

    val matrix = new MatrixBuilder(inputPathName).parseVCF()

    logger.info("Constructing PHYLIP data")
    var fw: BufferedWriter = null
    try {
      fw = new BufferedWriter(
        new OutputStreamWriter(
          new FileOutputStream(outputPathName), Charset.forName("UTF-8").newEncoder()))

      val names = matrix.sampleNames.filter(taxa => matrix.rows.map(_.status(taxa)).exists(bases.contains))
      if (names.size != matrix.sampleNames.size) {
        logger.warning("Sample(s) removed from cohort: " + (matrix.sampleNames.toSet - names).mkString(", "))
      }
      fw.write(s"${names.length} ${matrix.rows.length}" + System.lineSeparator())
      names.foreach(k => {
        // Phylip format requires a 10 character row label for the organism
        val label = k.padTo(10, " ").slice(0, 10).mkString("")

        // Collect all the calls for the organism to a single string
        // TODO: Consider interleaving with a defined column count and group spacing
        val calls = matrix.rows.map(r => r.status(k)).mkString("")
        fw.write(s"$label$calls" + System.lineSeparator())
      })
    } finally {
      fw.close()
    }
  }
}
