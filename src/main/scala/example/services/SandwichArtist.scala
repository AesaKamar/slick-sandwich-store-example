package example.services

import cats.data.{EitherT, ValidatedNel}
import example.{Helpers, Protein, Sandwich}

sealed trait SandwichArtistError
final case class WrappedBakerError(bakerError: BakerError)       extends SandwichArtistError
final case class WrappedVeggieCutterError(veggieCutterError: VeggieCutterError)
    extends SandwichArtistError
final case class WrappedGrillerError(grillerError: GrillerError) extends SandwichArtistError

object SandwichArtist {
  import cats.implicits._
  import slick.jdbc.PostgresProfile.api._
  import slickeffect.implicits._
  import scala.util.chaining._
  import Helpers._

  import scala.concurrent.ExecutionContext.Implicits.global

  def assembleSandwich(desiredProtein: Protein): DBIO[Either[SandwichArtistError, Sandwich]] = {
    val res = for {
      bun         <-
        Baker
          .bakeDoughIntoBuns()
          .pipe(EitherT.apply)
          .leftMap(WrappedBakerError)
      tomatoSlice <-
        VeggieCutter
          .getTomatoSlice()
          .pipe(EitherT.apply)
          .leftMap(WrappedVeggieCutterError)
      patty       <-
        Griller
          .cookPatty(desiredProtein)
          .pipe(EitherT.apply)
          .leftMap(WrappedGrillerError(_): SandwichArtistError)
    } yield Sandwich(bun, tomatoSlice, patty)

    res.value.transactionallyWithRollbackOnLeft
  }

  def assembleSandwichPar(
      desiredProtein: Protein
  ): DBIO[ValidatedNel[SandwichArtistError, Sandwich]] = {
    val res = (
      Baker
        .bakeDoughIntoBuns()
        .pipe(EitherT.apply)
        .leftMap(WrappedBakerError(_): SandwichArtistError)
        .toValidatedNel,
      VeggieCutter
        .getTomatoSlice()
        .pipe(EitherT.apply)
        .leftMap(WrappedVeggieCutterError(_): SandwichArtistError)
        .toValidatedNel,
      Griller
        .cookPatty(desiredProtein)
        .pipe(EitherT.apply)
        .leftMap(WrappedGrillerError(_): SandwichArtistError)
        .toValidatedNel
    ).mapN { case a => a.mapN { case (aa, bb, cc) => Sandwich(aa, bb, cc) } }

    res.transactionallyWithRollbackOnInvalid
  }

}
