package me.chuwy.otusfp

import cats.effect.implicits._
import cats.effect.testing.specs2.CatsEffect
import cats.effect.{IO, Ref, Resource}
import io.circe.Json
import io.circe.syntax.EncoderOps
import me.chuwy.otusfp.hw.Counter
import me.chuwy.otusfp.hw.Main.httpApp
import org.http4s.Method.GET
import org.http4s.Request
import org.http4s.circe._
import org.http4s.client.Client
import org.http4s.implicits._
import org.specs2.mutable.Specification


class HwSpec extends Specification with CatsEffect {

    type Counter[F[_]] = Ref[F, Int]

    val testClient: Resource[IO, Client[IO]] = for {
        counter <- Resource.eval[IO, Counter[IO]](Ref.of(0))
        client = Client.fromHttpApp(httpApp(counter))
    } yield client

    "Counter Service" should {
        "count amount of requests" in {
            val req: Request[IO] = Request(method = GET, uri = uri"/counter")
            val expectedResp = Counter(2).asJson

            val actualResp = for {
                client <- testClient
                _ <- client.expect[Json](req).toResource
                response <- client.expect[Json](req).toResource
            } yield {
                response
            }

            actualResp.map(_ must beEqualTo(expectedResp))
        }
    }

    "Throttling Service" should {
        "slowly return chunks" in {
            val req: Request[IO] = Request(method = GET, uri = uri"/stream/slow/3/8/1")

            val expectedResp = "Chunk(1, 1, 1); Chunk(1, 1, 1); Chunk(1, 1); "

            val actualResp = for {
                client <- testClient
                response <- client.expect[String](req).toResource
            } yield {
                response
            }

            actualResp.map(_ must beEqualTo(expectedResp))
        }
    }
}
