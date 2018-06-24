name := "vcf2phy"

version := "1.0"

scalaVersion := "2.11.7"

libraryDependencies ++= Seq(
  "com.github.samtools" % "htsjdk" % "2.10.1",
  "com.lihaoyi" %% "upickle" % "0.6.6"
)