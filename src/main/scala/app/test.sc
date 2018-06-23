import app.SequenceDiffLocator

val a = "GCGCGcTACG GACCaGCtgg TCGAGAtCtG cGCgGcGGGT CTTCCCAAAA TAgGAgTCGC".replace(" ", "")
val b = "GCGCGTTACG GACCGGCCAA TCGAGAGCCG TGCTGTGGG? CTTCCCAAA? TAAGACTCGC".replace(" ", "")

SequenceDiffLocator.find(a, b)