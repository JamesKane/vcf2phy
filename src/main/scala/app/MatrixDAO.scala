package app

import app.Converter.logger
import htsjdk.tribble.AbstractFeatureReader
import htsjdk.variant.vcf.{VCFCodec, VCFHeader}

import scala.collection.JavaConversions._

case class MatrixDAO(sampleNames: List[String], rows: List[RowDAO])

class MatrixBuilder(source: String) {
  private var rows: List[RowDAO] = List()

  def parseVCF() = {
    val reader = AbstractFeatureReader.getFeatureReader(source, new VCFCodec, false)
    logger.info(s"Reading vcf: ${source}")

    val header = reader.getHeader.asInstanceOf[VCFHeader]
    val kits = header.getSampleNamesInOrder.toList

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