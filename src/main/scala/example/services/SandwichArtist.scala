package example.services

import cats.data.EitherT
import example.{Protein, Sandwich}

sealed trait SandwichArtistError
final case class WrappedBakerError(bakerError: BakerError)                      extends SandwichArtistError
final case class WrappedVeggieCutterError(veggieCutterError: VeggieCutterError) extends SandwichArtistError
final case class WrappedGrillerError(grillerError: GrillerError)                extends SandwichArtistError

object SandwichArtist {
  import slick.jdbc.PostgresProfile.api._
  import scala.util.chaining._
  import slickeffect.implicits._

  import scala.concurrent.ExecutionContext.Implicits.global

  def assembleSandwich(desiredProtein: Protein): EitherT[DBIO, SandwichArtistError, Sandwich] =
    for {
      bun         <- Baker.bakeDoughIntoBuns().leftMap(WrappedBakerError(_))
      tomatoSlice <- VeggieCutter.getTomatoSlice().leftMap(WrappedVeggieCutterError(_))
      patty       <- Griller.cookPatty(desiredProtein).leftMap(WrappedGrillerError(_) : SandwichArtistError)
    } yield Sandwich(bun, tomatoSlice, patty)

}
