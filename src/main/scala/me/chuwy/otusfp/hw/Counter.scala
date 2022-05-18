package me.chuwy.otusfp.hw

import cats.effect.Concurrent
import io.circe._
import org.http4s.circe.{jsonEncoderOf, jsonOf}
import org.http4s.{EntityDecoder, EntityEncoder}


case class Counter(counter: Int) extends AnyVal

object Counter {
    implicit val encodeCounter: Encoder[Counter] = (c: Counter) => Json.obj(
        ("counter", Json.fromInt(c.counter))
    )

    implicit val decodeCounter: Decoder[Counter] = {
        Decoder.instance { cur =>
            for {
                jsonObject <- cur.as[JsonObject]
                counterOpt <- jsonObject.apply("counter").toRight(DecodingFailure("Key name doesn't exist", cur.history))
                counter <- counterOpt.as[Int]
            } yield {
                Counter(counter)
            }
        }
    }

    implicit def counterEntityDecoder[F[_] : Concurrent]: EntityDecoder[F, Counter] =
        jsonOf[F, Counter]

    implicit def counterEntityEncoder[F[_] : Concurrent]: EntityEncoder[F, Counter] =
        jsonEncoderOf[F, Counter]
}
