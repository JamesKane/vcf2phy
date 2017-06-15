package app

object IUPAC {

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

}
