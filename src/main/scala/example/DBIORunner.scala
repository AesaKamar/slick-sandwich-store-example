package example

import cats.effect.IO
import com.typesafe.config.ConfigFactory
import slick.basic.DatabaseConfig
import slick.jdbc.PostgresProfile

object DBIORunner {
  import slick.jdbc.PostgresProfile.api._

  private val cfg =
    s"""
      |profile = "slick.jdbc.PostgresProfile$$"
      |db {
      |  connectionPool = "HikariCP" //use HikariCP for our connection pool
      |  driver = "org.postgresql.Driver"
      |  url = "jdbc:postgresql://localhost/fp?user=fp"
      |  keepAliveConnection = true
      |}
      |""".stripMargin

  private val config   = ConfigFactory.parseString(cfg)
  private val dbConfig = DatabaseConfig.forConfig[PostgresProfile](path = "", config = config)

  def run[A](dbio: DBIO[A]): IO[A] = IO.fromFuture(IO(dbConfig.db.run(dbio)))
}
