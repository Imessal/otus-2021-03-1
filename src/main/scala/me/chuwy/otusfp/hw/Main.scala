package me.chuwy.otusfp.hw

import cats.effect._
import fs2.Stream
import org.http4s.blaze.server._
import org.http4s.dsl.io._
import org.http4s.implicits._
import org.http4s.server.{Router, Server}
import org.http4s.{HttpApp, HttpRoutes}

import scala.concurrent.ExecutionContext.global
import scala.concurrent.duration.DurationInt
import scala.util.Try

object Main extends IOApp {

    object PositiveIntVal {
        def unapply(value: String): Option[Int] = {
            Try {
                val asInt = value.toInt
                if (asInt < 0) None else Some(asInt)
            }.toOption.flatten
        }
    }

    type Counter[F[_]] = Ref[F, Int]

    def counterService(counter: Counter[IO]): HttpRoutes[IO] = HttpRoutes.of[IO] {
        case GET -> Root / "counter" =>
            counter.updateAndGet(_ + 1).flatMap { c =>
                Ok(Counter(c))
            }
    }

    def throttlingService: HttpRoutes[IO] = HttpRoutes.of[IO] {
        case GET -> Root / "slow" / PositiveIntVal(chunk) / PositiveIntVal(total) / PositiveIntVal(time) =>

            val largeByteStream = Stream.emit(1.byteValue).repeat
            val stream = {
                largeByteStream
                    .take(total)
                    .chunkN(chunk)
                    .metered[IO](time.second)
                    .evalMapChunk { c =>
                        IO.pure(s"${c.toString}; ")
                    }
            }
            Ok(stream)
    }

    def httpApp(counter: Counter[IO]): HttpApp[IO] = Router(
        "/" -> counterService(counter),
        "stream" -> throttlingService
    ).orNotFound

    val serverBuilder: Resource[IO, Server] = for {
        counter <- Resource.eval[IO, Counter[IO]](Ref.of(0))
        builder <- BlazeServerBuilder[IO](global)
            .bindHttp(8080, "localhost")
            .withHttpApp(httpApp(counter))
            .resource
    } yield {
        builder
    }

    override def run(args: List[String]): IO[ExitCode] =
        serverBuilder
            .use(_ => IO.never)
            .as(ExitCode.Success)
}
