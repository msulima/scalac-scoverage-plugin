package scoverage

import java.io.File
import java.util.UUID

import org.scalatest.{FreeSpec, Matchers}
import scoverage.report.{CoverageAggregator, ScoverageXmlWriter}

class CoverageAggregatorTest extends FreeSpec with Matchers {

  "coverage aggregator" - {
    "should merge coverage objects with same id" in {

      val source = "/home/sam/src/main/scala/com/scoverage/class.scala"
      val location = Location("com.scoverage.foo",
        "ServiceState",
        "Service",
        ClassType.Trait,
        "methlab",
        source)

      val coverage1 = Coverage()
      coverage1.add(Statement(source, location, 1, 155, 176, 4, "", "", "", true, 1))
      coverage1.add(Statement(source, location, 2, 95, 105, 19, "", "", "", false, 10))
      val dir1 = new File(IOUtils.getTempPath, UUID.randomUUID.toString)
      dir1.mkdir()
      new ScoverageXmlWriter(new File("/home/sam"), dir1, false).write(coverage1)

      val coverage2 = Coverage()
      coverage2.add(Statement(source, location, 1, 95, 105, 19, "", "", "", false, 2))
      val dir2 = new File(IOUtils.getTempPath, UUID.randomUUID.toString)
      dir2.mkdir()
      new ScoverageXmlWriter(new File("/home/sam"), dir2, false).write(coverage2)

      val coverage3 = Coverage()
      coverage3.add(Statement(source, location, 2, 14, 1515, 544, "", "", "", false, 3))
      val dir3 = new File(IOUtils.getTempPath, UUID.randomUUID.toString)
      dir3.mkdir()
      new ScoverageXmlWriter(new File("/home/sam"), dir3, false).write(coverage3)

      val aggregated = CoverageAggregator.aggregatedCoverage(
        Seq(IOUtils.reportFile(dir1, debug = false),
          IOUtils.reportFile(dir2, debug = false),
          IOUtils.reportFile(dir3, debug = false))
      )

      aggregated.statements.map(_.copy(id=0)) should contain.theSameElementsAs(Set(
        Statement(source, location, 0, 155, 176, 4, "", "", "", true, 1),
        Statement(source, location, 0, 95, 105, 19, "", "", "", false, 10),
        Statement(source, location, 0, 95, 105, 19, "", "", "", false, 2),
        Statement(source, location, 0, 14, 1515, 544, "", "", "", false, 3)
      ))
    }
  }
}
