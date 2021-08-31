package example.services

import cats.data.EitherT
import example.tables._
import example._

sealed trait VeggieCutterError
case object CouldNotFindSliceButWillCutOneMyself extends VeggieCutterError
case object NotEnoughTomatoesInStock             extends VeggieCutterError
case object TomatoSliceGotStolen                 extends VeggieCutterError
case object TomatoGotStolen                      extends VeggieCutterError

object VeggieCutter {
  import slick.jdbc.PostgresProfile.api._
  import scala.util.chaining._
  import slickeffect.implicits._

  import scala.concurrent.ExecutionContext.Implicits.global

  private val numSlicesPerTomato = 5

  private def cutTomatoIntoSlicesAndGetOneSlice(): EitherT[DBIO, VeggieCutterError, TomatoSlice] =
    for {
      tomato          <- Helpers.findAndDeleteFirstRecord[Tomato, TomatoRecord, VeggieCutterError](Tomatoes)(
                           _.id,
                           _.id,
                           errorIfNotFound = NotEnoughTomatoesInStock,
                           errorOnDeleteFailure = TomatoGotStolen
                         )
      slicesToSlice    = (1 to numSlicesPerTomato).map(TomatoSlice(tomato.id, _))
      _insertedSlices <- TomatoSlices
                           .returning(TomatoSlices)
                           .++=(slicesToSlice)
                           .pipe(EitherT.liftF[DBIO, VeggieCutterError, Seq[TomatoSlice]](_))
      tomatoSlice     <-
        Helpers.findAndDeleteFirstRecord[TomatoSlice, TomatoSliceRecord, VeggieCutterError](
          TomatoSlices
        )(
          _.id,
          _.id,
          errorIfNotFound = TomatoGotStolen,
          errorOnDeleteFailure = TomatoGotStolen
        )
    } yield tomatoSlice

  def getTomatoSlice(): EitherT[DBIO, VeggieCutterError, TomatoSlice] = for {
    slice <-
      Helpers
        .findAndDeleteFirstRecord[TomatoSlice, TomatoSliceRecord, VeggieCutterError](TomatoSlices)(
          _.id,
          _.id,
          errorIfNotFound = CouldNotFindSliceButWillCutOneMyself,
          errorOnDeleteFailure = TomatoSliceGotStolen
        )
        .leftFlatMap {
          case CouldNotFindSliceButWillCutOneMyself => cutTomatoIntoSlicesAndGetOneSlice()
          case err                                  => EitherT.leftT[DBIO, TomatoSlice].apply(err)
        }
  } yield slice
}
