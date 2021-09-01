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

    res.value.transactionallyWithRollbackOnLeft
  }

  implicit class TransactionalEitherOps[E, A](dbio: DBIO[Either[E, A]]){
    def transactionallyWithRollbackOnLeft: DBIO[Either[E, A]] = {
      var resVar : Either[E, A] = null
      dbio.flatMap {
        case res @ Left(_)  =>
          resVar = res
          (DBIO.failed(new Exception): DBIO[Either[E, A]])
        case res @ Right(_) =>
          resVar = res
          DBIO.successful(res)
      }.transactionally.asTry.flatMap {
        _ => DBIO.successful(resVar)
      }
    }
  }
  implicit class TransactionalValidatedOps[E, A](dbio: DBIO[ValidatedNel[E, A]]) {
    def transactionallyWithRollbackOnInvalid: DBIO[ValidatedNel[E, A]] = {
      var resVar : ValidatedNel[E, A] = null
      dbio.flatMap {
        case res @ Validated.Invalid(_) =>
          resVar = res
          (DBIO.failed(new Exception): DBIO[ValidatedNel[E, A]])
        case res @ Validated.Valid(_)   =>
          resVar = res
          DBIO.successful(res)
      }.transactionally.asTry.flatMap {
        _ => DBIO.successful(resVar)
      }
    }
  }
}
