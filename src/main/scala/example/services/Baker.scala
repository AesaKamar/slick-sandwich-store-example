package example.services

import cats.data.EitherT
import example.{Bun, Dough, DoughRecord, Helpers}
import example.tables.{Buns, Doughs}

sealed trait BakerError
case object NotEnoughDoughInStock extends BakerError
case object DoughGotStolen        extends BakerError

object Baker {
  import slick.jdbc.PostgresProfile.api._
  import scala.util.chaining._
  import slickeffect.implicits._

  import scala.concurrent.ExecutionContext.Implicits.global

  def bakeDoughIntoBuns(): EitherT[DBIO, BakerError, Bun] = for {
    dough     <-
      Helpers
        .findAndDeleteFirstRecord[Dough, DoughRecord , BakerError](Doughs)(
          _.id,
          _.id,
          errorIfNotFound = NotEnoughDoughInStock,
          errorOnDeleteFailure = DoughGotStolen
        )
    bakedBun <-
      Buns
        .returning(Buns)
        .+=(Bun(dough.id, 0))
        .pipe(EitherT.liftF[DBIO, BakerError, Bun])
  } yield bakedBun

}
