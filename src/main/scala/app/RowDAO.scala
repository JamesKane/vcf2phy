package app

import htsjdk.tribble.CloseableTribbleIterator
import htsjdk.variant.variantcontext.VariantContext

import scala.collection.parallel.mutable.ParMap
import scala.collection.JavaConversions._

case class RowDAO(id: String, contig: String, pos: Int, anc: String, der: String,
                  variantType: String, status: ParMap[String, String]) {
}

object RowDAO {
  def create(it: CloseableTribbleIterator[VariantContext]): Option[RowDAO] = {
    val vc = it.next()

    val start = vc.getStart
    val ref = vc.getReference.getBaseString
    val alt = vc.getAltAlleleWithHighestAlleleCount.getBaseString
    val key = s"$start-$ref-$alt"

    val vType = if (vc.isSNP) "snp"
    else if (vc.isSimpleInsertion) "ins"
    else if (vc.isSimpleDeletion) "del"
    else if (vc.isMNP) "mnp"
    else ""

    if (alt.nonEmpty && vc.isSNP && vc.isNotFiltered && belowNoCallRatio(vc)) {
      val rowDAO = RowDAO(key, vc.getContig, start, ref, alt, vType, ParMap())
      vc.getGenotypes.toList.foreach(gt => {
        val status = gt.getGenotypeString(true).replace("/", "").sorted
        // TODO:  Need to deal with indels
        rowDAO.status.put(gt.getSampleName, IUPAC.nucleotide(status))
      })
      Some(rowDAO).filter(belowHeterozygousRatio)
    } else {
      None
    }
  }

  // TODO: Expose these ratios as command line arguments
  private def belowNoCallRatio(vc: VariantContext) = {
    vc.getNoCallCount / vc.getNSamples.toDouble < 0.1
  }

  private val alleles = Set("A", "C", "G", "T", "?", "-")
  private def belowHeterozygousRatio(row: RowDAO) = {
    val numSamples = row.status.size
    val heterozygosityScore = row.status.values.count(sample => !alleles.contains(sample))
    heterozygosityScore / numSamples.toDouble < 0.2
  }
}
