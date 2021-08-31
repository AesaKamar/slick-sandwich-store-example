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
      |  dataSourceClass = "org.postgresql.ds.PGSimpleDataSource"
      |  properties = {
      |    serverName = "localhost"
      |    portNumber = "5432"
      |    databaseName = "fp"
      |    user = "fp"
      |  }
      |  numThreads = 2
      |}
      |""".stripMargin

  private val config = ConfigFactory.parseString(cfg)
  private val dbConfig = DatabaseConfig.forConfig[PostgresProfile](path = "", config = config)

  def run[A](dbio: DBIO[A]): IO[A] = IO.fromFuture(IO(dbConfig.db.run(dbio)))
}
