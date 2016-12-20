package com.ecommerce.shoppingcart.backend

import akka.serialization.Serializer
import io.circe.generic.auto._
import io.circe.parser._
import io.circe.syntax._

/**
  * Created by lukewyman on 12/16/16.
  */
class ShoppingCartEventSerializer extends Serializer {

  def identifier: Int = 1234567

  def includeManifest: Boolean = false

  def fromBinary(bytes: Array[Byte], manifest: Option[Class[_]]): AnyRef = decode[Event](new String(bytes))

  def toBinary(obj: AnyRef): Array[Byte] = obj match {
    case event: Event => obj.asInstanceOf[Event].asJson.noSpaces.getBytes
    case other => throw new Exception(s"Cannot serialize ${other} of type ${other.getClass}")
  }

}