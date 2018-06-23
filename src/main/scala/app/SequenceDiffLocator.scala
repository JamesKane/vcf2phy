package app

case class DiffSummary(index: Int, from: String, to: String)

object SequenceDiffLocator {

  def find(a: String, b: String): List[DiffSummary] = {
    val diffs = (for {
      i <- Range(0, a.length)
      if a(i).toUpper != b(i).toUpper && IUPAC.validBases.contains(b(i).toString)
    } yield DiffSummary(i, a(i).toString, b(i).toString)).toList
    diffs
  }

}
