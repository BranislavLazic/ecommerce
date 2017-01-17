package com.ecommerce.orchestrator.backend.actor.httpclient

import java.util.UUID

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.Uri.Path
import akka.http.scaladsl.model._
import akka.http.scaladsl.unmarshalling.{Unmarshal, Unmarshaller}
import akka.stream.scaladsl.{Sink, Source}
import akka.stream.{ActorMaterializerSettings, ActorMaterializer}
import com.ecommerce.orchestrator.backend.actor.httpclient.ResponseViews.{ResponseView, ShoppingCartView}
import de.heikoseeberger.akkahttpcirce.CirceSupport

import scala.concurrent.{Future, ExecutionContext}

/**
  * Created by lukewyman on 1/15/17.
  */
trait HttpClient {
  import HttpClient._

  implicit def system: ActorSystem
  implicit def executionContext: ExecutionContext
  val http = Http(system)

  implicit val materializer: ActorMaterializer = ActorMaterializer(ActorMaterializerSettings(system))

  def deserialize[T](r: HttpResponse)(implicit um: Unmarshaller[ResponseEntity, T]): Future[HttpClientResult[T]] =
    r.status match {
      case StatusCodes.OK => Unmarshal(r.entity).to[T] map Right.apply
      case StatusCodes.NotFound => Future(Left(NotFound(r.entity.toString)))
      case StatusCodes.BadRequest => Future(Left(BadRequest(r.entity.toString)))
      case StatusCodes.Unauthorized => Future(Left(Unauthorized(r.entity.toString)))
      case _ => Future(Left(UnexpectedStatusCode(r.status)))
    }
}

object HttpClient {
  type HttpClientResult[T] = Either[HttpClientError, T]

  sealed trait HttpClientError
  case class NotFound(error: String) extends HttpClientError
  case class BadRequest(error: String) extends HttpClientError
  case class Unauthorized(error: String) extends HttpClientError
  case class UnexpectedStatusCode(statusCode: StatusCode) extends HttpClientError
}