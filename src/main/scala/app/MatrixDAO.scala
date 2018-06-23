package app

import java.util.logging.Logger

import htsjdk.tribble.AbstractFeatureReader
import htsjdk.variant.vcf.{VCFCodec, VCFHeader}

import scala.collection.JavaConversions._

case class MatrixDAO(sampleNames: List[String], rows: List[RowDAO])

class MatrixBuilder(source: String) {
  val logger = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME)
  private var rows: List[RowDAO] = List()

  def parseVCF() = {
    val reader = AbstractFeatureReader.getFeatureReader(source, new VCFCodec, false)

    val header = reader.getHeader.asInstanceOf[VCFHeader]
    val kits = header.getSampleNamesInOrder.toList
    logger.info(s"Reading vcf: $source containing ${kits.size} organisms")

    val it = reader.iterator()
    while (it.hasNext) {
      RowDAO.create(it) match {
        case Some(rowDAO) => rows = rows :+ rowDAO
        case None => // Do nothing
      }
    }

    MatrixDAO(kits, rows)
  }

}