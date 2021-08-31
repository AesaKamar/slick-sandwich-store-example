package example

import cats.data.EitherT
import slick.relational.RelationalProfile

object Helpers {

  import slick.jdbc.PostgresProfile.api._
  import cats.implicits._
  import scala.util.chaining._
  import slickeffect.implicits._

  import scala.concurrent.ExecutionContext.Implicits.global

  def findAndDeleteFirstRecord[T, RT <: RelationalProfile#Table[T], E](table: TableQuery[RT])(
      getIdR: RT => Rep[Long],
      getId: RT#TableElementType => Long,
      errorIfNotFound: E,
      errorOnDeleteFailure: E
  ): EitherT[DBIO, E, RT#TableElementType] = {
    for {
      firstRecord <- table
        .take(1)
        .result
        .headOption
        .pipe (EitherT.fromOptionF[DBIO, E, T](_, errorIfNotFound))
      _           <- table
        .filter(getIdR(_) === getId(firstRecord))
        .delete
        .map {
          case 1 => firstRecord.asRight[E]
          case _ => errorOnDeleteFailure.asLeft
        }
        .pipe(EitherT.apply[DBIO, E, T])
  } yield firstRecord
  }

}
