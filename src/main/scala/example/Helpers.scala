package example

import cats.data.{EitherT, Validated, ValidatedNel}
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
  ): DBIO[Either[E, RT#TableElementType]] = {
    val res = for {
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

    res.value.transactionally
  }

  def transactionallyWithRollbackOnLeft[E, A](
      dbio: DBIO[Either[E, A]]
  ): DBIO[Either[E, A]] =
    dbio.transactionally.flatMap {
      case res @ Left(_)  =>
        (DBIO.failed(new Exception): DBIO[Either[E, A]]).recover { case _ => res }
      case res @ Right(_) => DBIO.successful(res)
    }

  def transactionallyWithRollbackOnInvalid[E, A](
      dbio: DBIO[ValidatedNel[E, A]]
  ): DBIO[ValidatedNel[E, A]] =
    dbio.transactionally.flatMap {
      case res @ Validated.Invalid(_) =>
        (DBIO.failed(new Exception): DBIO[ValidatedNel[E, A]]).recover { case _ => res }
      case res @ Validated.Valid(_)   => DBIO.successful(res)
    }
}
