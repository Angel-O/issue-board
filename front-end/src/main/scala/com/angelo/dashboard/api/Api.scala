package com.angelo.dashboard.api

import io.circe.{Decoder, Encoder}
import io.circe.parser._
import io.circe.syntax._
import japgolly.scalajs.react.{AsyncCallback, Callback}
import japgolly.scalajs.react.extra.Ajax

object Api {
  case class RequestException(msg: String) extends Exception(msg)

  private[api] def GET[A: Decoder](url: String): AsyncCallback[A] =
    Ajax
      .get(url)
      .setRequestContentTypeJsonUtf8
      .send
      .validateStatusIs(200)(err => Callback.throwException(RequestException(err.getMessage)))
      .asAsyncCallback
      .map(xhr => parse(xhr.responseText).flatMap(_.as[A]))
      .flatMap {
        case Left(err) => AsyncCallback.throwException[A](RequestException(err.getMessage))
        case Right(a)  => AsyncCallback.pure(a)
      }

  private[api] def POST[A: Encoder](url: String, payload: A): AsyncCallback[Unit] =
    Ajax
      .post(url)
      .setRequestContentTypeJsonUtf8
      .send(payload.asJson.noSpaces)
      .validateStatusIsSuccessful(err => Callback.throwException(RequestException(err.getMessage)))
      .asAsyncCallback
      .void

  private[api] def PATCH[A: Encoder](url: String, payload: A): AsyncCallback[Unit] =
    Ajax
      .apply("PATCH", url)
      .setRequestContentTypeJsonUtf8
      .send(payload.asJson.noSpaces)
      .validateStatusIsSuccessful(err => Callback.throwException(RequestException(err.getMessage)))
      .asAsyncCallback
      .void

  private[api] def DELETE(url: String): AsyncCallback[Unit] =
    Ajax
      .apply("DELETE", url)
      .setRequestContentTypeJsonUtf8
      .send
      .validateStatusIsSuccessful(err => Callback.throwException(RequestException(err.getMessage)))
      .asAsyncCallback
      .void
}
