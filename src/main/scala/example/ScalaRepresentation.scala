package example


final case class Tomato(id: Long)
final case class TomatoSlice(tomatoId: Long, id: Long)

final case class Dough(id: Long)
final case class Bun(doughId: Long, id: Long)

sealed trait Protein
final case class Tofu(id: Long)  extends Protein
final case class Steak(id: Long) extends Protein
final case class Tuna(id: Long)  extends Protein

final case class Patty(id: Long, tofuId: Option[Long], steakId: Option[Long], tunaId: Option[Long])

final case class Sandwich(bun: Bun, tomatoSlice: TomatoSlice, patty: Patty)
