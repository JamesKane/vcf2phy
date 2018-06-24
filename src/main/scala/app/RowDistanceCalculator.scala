package app

class RowDistanceCalculator {

  def dist(a: RowDAO, b: RowDAO): Double = {
    val vectorizedA = vectorize(a)
    val vectorizedB = vectorize(b)

    Math.sqrt(vectorizedA.zip(vectorizedB).map(p => Math.pow(p._1 + p._2, 2)).sum)
  }

  private def vectorize(row: RowDAO): List[Double] = row.samples.map(n => alleleDist(row.anc, row.status(n)))

  // TODO: This should be replaced with a Markov model
  private def alleleDist(ref: String, obs: String): Double = {
    if(ref == obs) {
      0.0
    } else if(IUPAC.reversed.contains(obs) && IUPAC.reversed(obs).contains(ref)) {
      0.5
    } else {
      1.0
    }
  }

}
