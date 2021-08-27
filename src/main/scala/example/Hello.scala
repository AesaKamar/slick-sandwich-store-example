package example
import zio.{ExitCode, Has, RIO, Schedule, UIO, URIO, ZIO, ZLayer, ZManaged}
import zio.clock.Clock
import zio.console.putStrLn
import zio.duration.Duration
import zio.metrics.statsd._
import zio.Runtime
import zio.console.Console
import zio.metrics.encoders.Encoder
import java.util.concurrent.TimeUnit

object Hello extends App {

  val schd               = Schedule.recurs(10) // optional, used to send more samples to StatsD
  val createStatsDClient = StatsDClient()      // StatsD default client

  val rt: Runtime.Managed[Encoder with Console with Clock] = Runtime.unsafeFromLayer(Encoder.statsd ++ Console.live ++ Clock.live)

  def program(r: Long)(statsDClient: StatsDClient) =
    for {
      clock <- RIO.environment[Clock]
      t1    <- clock.get.currentTime(TimeUnit.MILLISECONDS)
      _     <- statsDClient.increment("zmetrics.counter", 0.9)
      _     <- putStrLn(s"waiting for $r ms") *> clock.get.sleep(Duration(r, TimeUnit.MILLISECONDS))
      t2    <- clock.get.currentTime(TimeUnit.MILLISECONDS)
      _     <- statsDClient.timer("zmetrics.timer", (t2 - t1).toDouble, 0.9)
    } yield ()

  val timeouts = Seq(34L, 76L, 52L)
  rt.unsafeRun(
    createStatsDClient.use { statsDClient =>
      RIO
        .foreach(timeouts)(l => program(l)(statsDClient))
        .repeat(schd)
    }
  )
  Thread.sleep(1000) // wait for all messages to be consumed

}
