package example

import slick.jdbc.PostgresProfile.api._
import slick.lifted.{ForeignKeyQuery, ProvenShape}

/**[[Tomato]]*/
final class TomatoRecord(tag: Tag) extends Table[Tomato](tag, "tomatoes") {
  def id: Rep[Long] = column[Long]("id", O.AutoInc, O.PrimaryKey)

  def * : ProvenShape[Tomato] = (id) <> (Tomato.apply, Tomato.unapply)
}
/**[[TomatoSlice]]*/
final class TomatoSliceRecord(tag: Tag) extends Table[TomatoSlice](tag, "tomato_slices") {
  def tomatoId: Rep[Long] = column[Long]("tomato_id")
  def id: Rep[Long]       = column[Long]("id", O.AutoInc, O.PrimaryKey)

  def fkTomatoSliceToTomato: ForeignKeyQuery[TomatoRecord, Tomato] = foreignKey("tomato_slice_to_tomato", tomatoId, tables.Tomatoes)(_.id)

  def * : ProvenShape[TomatoSlice] = (tomatoId, id) <> (TomatoSlice.tupled, TomatoSlice.unapply)
}
/**[[Dough]]*/
final class DoughRecord(tag: Tag) extends Table[Dough](tag, "doughs") {
  def id: Rep[Long] = column[Long]("id", O.AutoInc, O.PrimaryKey)

  def * : ProvenShape[Dough] = id <> (Dough.apply, Dough.unapply)
}

/**[[Bun]]*/
final class BunRecord(tag: Tag) extends Table[Bun](tag, "buns") {
  def doughId: Rep[Long] = column[Long]("dough_id")
  def id: Rep[Long] = column[Long]("id", O.AutoInc, O.PrimaryKey)
  def fkBunToDough: ForeignKeyQuery[DoughRecord, Dough] = foreignKey("bun_to_dough", doughId, tables.Doughs)(_.id)

  def * : ProvenShape[Bun] = (doughId, id) <> (Bun.tupled, Bun.unapply)
}

sealed trait ProteinRecord
/**[[Tofu]]*/
final class TofuRecord(tag: Tag)  extends Table[Tofu](tag, "tofus") with ProteinRecord   {
  def id: Rep[Long] = column[Long]("id", O.AutoInc, O.PrimaryKey)

  def * : ProvenShape[Tofu] = id <> (Tofu.apply, Tofu.unapply)
}
/**[[Steak]]*/
final class SteakRecord(tag: Tag) extends Table[Steak](tag, "steaks") with ProteinRecord {
  def id: Rep[Long] = column[Long]("id", O.AutoInc, O.PrimaryKey)

  def * : ProvenShape[Steak] = id <> (Steak.apply, Steak.unapply)
}
/**[[Tuna]]*/
final class TunaRecord(tag: Tag)  extends Table[Tuna](tag, "tunas") with ProteinRecord   {
  def id: Rep[Long] = column[Long]("id", O.AutoInc, O.PrimaryKey)

  def * : ProvenShape[Tuna] = id <> (Tuna.apply, Tuna.unapply)
}
/**[[Patty]]*/
final class PattyRecord(tag: Tag)  extends Table[Patty](tag, "patties") {
  def id: Rep[Long] = column[Long]("id", O.AutoInc, O.PrimaryKey)
  def tofuId: Rep[Option[Long]] = column[Option[Long]]("tofu_id")
  def steakId: Rep[Option[Long]] = column[Option[Long]]("steak_id")
  def tunaId: Rep[Option[Long]] = column[Option[Long]]("tuna_id")

  def fkPattyToTofu: ForeignKeyQuery[TofuRecord, Tofu] = foreignKey("patty_to_tofu", tofuId, tables.Tofus)(_.id.?)
  def fkPattyToSteak: ForeignKeyQuery[SteakRecord, Steak] = foreignKey("patty_to_steak", steakId, tables.Steaks)(_.id.?)
  def fkPattyToTuna: ForeignKeyQuery[TunaRecord, Tuna] = foreignKey("patty_to_tuna", tunaId, tables.Tunas)(_.id.?)

  def * : ProvenShape[Patty] = (id, tofuId, steakId, tunaId) <> (Patty.tupled, Patty.unapply)
}


object tables {
  object Tomatoes extends TableQuery(new TomatoRecord(_))
  object TomatoSlices extends TableQuery(new TomatoSliceRecord(_))

  object Doughs extends TableQuery(new DoughRecord(_))
  object Buns extends TableQuery(new BunRecord(_))

  object Tofus extends TableQuery(new TofuRecord(_))
  object Steaks extends TableQuery(new SteakRecord(_))
  object Tunas extends TableQuery(new TunaRecord(_))

  object Patties extends TableQuery(new PattyRecord(_))
}

/**[[Sandwich]]*/
final case class SandwichRep(
    bun: BunRecord,
    tomatoSlice: TomatoSliceRecord,
    protein: PattyRecord,
)

