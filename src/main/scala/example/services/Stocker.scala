package example.services

import example.tables._
import example._

sealed trait StockerError
case object NotEnoughMoneyInWallet extends StockerError
case object NotEnoughStockInMarket extends StockerError

object Stocker {
  import slick.jdbc.PostgresProfile.api._
  import cats.implicits._
  import scala.concurrent.ExecutionContext.Implicits.global

  def buyDoughs(qty: Int): DBIO[Either[StockerError, List[Dough]]] =
    Doughs
      .returning(Doughs)
      .++=(List.fill(qty)(Dough(0)))
      .map(_.toList.asRight[StockerError])

  def buyTomates(qty: Int): DBIO[Either[StockerError, List[Tomato]]] =
    Tomatoes
      .returning(Tomatoes)
      .++=(List.fill(qty)(Tomato(0)))
      .map(_.toList.asRight[StockerError])

  def buyTofu(qty: Int): DBIO[Either[StockerError, List[Tofu]]]   =
    Tofus
      .returning(Tofus)
      .++=(List.fill(qty)(Tofu(0)))
      .map(_.toList.asRight[StockerError])
  def buySteak(qty: Int): DBIO[Either[StockerError, List[Steak]]] =
    Steaks
      .returning(Steaks)
      .++=(List.fill(qty)(Steak(0)))
      .map(_.toList.asRight[StockerError])
  def buyTuna(qty: Int): DBIO[Either[StockerError, List[Tuna]]]   =
    Tunas
      .returning(Tunas)
      .++=(List.fill(qty)(Tuna(0)))
      .map(_.toList.asRight[StockerError])

}
