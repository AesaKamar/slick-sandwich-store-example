package example

import example.services.{SandwichArtist, Stocker}
import org.scalatest.freespec.AsyncFreeSpec

class HelloSpec extends AsyncFreeSpec {
  import slick.jdbc.PostgresProfile.api._
  import cats.implicits._
  import cats.effect.unsafe.implicits._
  import slickeffect.implicits._
  import scala.util.chaining._
  import org.scalatest.matchers.must.Matchers._

  def setupSchema: DBIO[List[Unit]] = List(
    tables.Tomatoes.schema.createIfNotExists,
    tables.TomatoSlices.schema.createIfNotExists,
    tables.Doughs.schema.createIfNotExists,
    tables.Buns.schema.createIfNotExists,
    tables.Tofus.schema.createIfNotExists,
    tables.Steaks.schema.createIfNotExists,
    tables.Tunas.schema.createIfNotExists,
    tables.Patties.schema.createIfNotExists,
  )
    .map(x => x: DBIO[Unit])
    .sequence

  def clearDb = List(
    tables.Tomatoes.delete,
    tables.TomatoSlices.delete,
    tables.Doughs.delete,
    tables.Buns.delete,
    tables.Tofus.delete,
    tables.Steaks.delete,
    tables.Tunas.delete,
    tables.Patties.delete,
  ).traverse(x => x: DBIO[Int])

  "When we are missing ingredients, we should report invalid and not consume resources" in {
    val f = for {
      _        <- clearDb.pipe(DBIORunner.run)
      _ <- Stocker.buyTomates(1).pipe(DBIORunner.run)
      _ <- Stocker.buyDoughs(1).pipe(DBIORunner.run)

      sandwiches <- SandwichArtist.assembleSandwichPar(Steak(0)).pipe(DBIORunner.run)
    } yield {
      pprint.log(sandwiches)
      sandwiches.isValid mustBe false
    }

    f.unsafeToFuture()
  }
  "When we have all ingredients, we should report valid" in {
    val f = for {
      _        <- clearDb
      doughs   <- Stocker.buyDoughs(1)
      tunas    <- Stocker.buyTuna(1)
      tofus    <- Stocker.buyTofu(1)
      steaks   <- Stocker.buySteak(1)
      tomatoes <- Stocker.buyTomates(1)

      sandwiches <- SandwichArtist.assembleSandwichPar(Steak(0))
    } yield {
      pprint.log(sandwiches)
      sandwiches.isValid mustBe true

    }

    f.pipe(DBIORunner.run).unsafeToFuture()
  }
}
