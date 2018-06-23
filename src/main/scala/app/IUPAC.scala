package app

object IUPAC {
  val validBases = Set("A", "C", "G", "T", "U")

  // The common IUPAC nucleotide codes sourced from http://www.bioinformatics.org/sms/iupac.html
  val nucleotide = Map[String, String](
    "AA" -> "A"
    , // Adenine
    "CC" -> "C"
    , // Cytosine
    "GG" -> "G"
    , // Guanine
    "TT" -> "T"
    , // Thymine
    "UU" -> "U"
    , // Uracile
    "AC" -> "M"
    ,
    "AG" -> "R"
    ,
    "AT" -> "W"
    ,
    "CG" -> "S"
    ,
    "CT" -> "Y"
    ,
    "GT" -> "K"
    ,
    "ACG" -> "V"
    ,
    "ACT" -> "H"
    ,
    "AGT" -> "D"
    ,
    "CGT" -> "B"
    ,
    "ACGT" -> "N"
    ,
    // Spanning deletions
    "**" -> "-"
    ,
    "*A" -> "-"
    ,
    "*C" -> "-"
    ,
    "*G" -> "-"
    ,
    "*T" -> "-"
  ).withDefaultValue("?")

  def reversed: Map[String, String] = nucleotide.map(p => p._2 -> p._1)

}
