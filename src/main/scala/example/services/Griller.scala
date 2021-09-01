package example.services

import cats.data.EitherT
import example.tables._
import example._

import Helpers._

sealed trait GrillerError
case object BurnedThePatty          extends GrillerError
case object NotEnoughProteinInStock extends GrillerError
case object PattyGotStolen          extends GrillerError

object Griller {
  import slick.jdbc.PostgresProfile.api._
  import scala.util.chaining._
  import slickeffect.implicits._

  import scala.concurrent.ExecutionContext.Implicits.global

  private def acquireProteinFromPantry(
      proteinType: Protein
  ): EitherT[DBIO, GrillerError, Protein] =
    proteinType match {
      case Tofu(_)  =>
        findAndDeleteFirstRecord[Tofu, TofuRecord, GrillerError](Tofus)(
          _.id,
          _.id,
          errorIfNotFound = NotEnoughProteinInStock,
          errorOnDeleteFailure = PattyGotStolen
        ).pipe(EitherT.apply).map(x => x: Protein)
      case Steak(_) =>
        findAndDeleteFirstRecord[Steak, SteakRecord, GrillerError](Steaks)(
          _.id,
          _.id,
          errorIfNotFound = NotEnoughProteinInStock,
          errorOnDeleteFailure = PattyGotStolen
        ).pipe(EitherT.apply).map(x => x: Protein)
      case Tuna(_)  =>
        findAndDeleteFirstRecord[Tuna, TunaRecord, GrillerError](Tunas)(
          _.id,
          _.id,
          errorIfNotFound = NotEnoughProteinInStock,
          errorOnDeleteFailure = PattyGotStolen
        ).pipe(EitherT.apply).map(x => x: Protein)
    }

  private def makePatty(protein: Protein): Patty = protein match {
    case Tofu(id)  => Patty(0, Some(id), None, None)
    case Steak(id) => Patty(0, None, Some(id), None)
    case Tuna(id)  => Patty(0, None, None, Some(id))
  }

  def cookPatty(protein: Protein): DBIO[Either[GrillerError, Patty]] = {
    val res = for {
      rawProtein  <- acquireProteinFromPantry(protein)
      cookedPatty <- Patties
                       .returning(Patties)
                       .+=(makePatty(rawProtein))
                       .pipe(EitherT.liftF[DBIO, GrillerError, Patty](_))
    } yield cookedPatty

    res.value.transactionally
  }

}
