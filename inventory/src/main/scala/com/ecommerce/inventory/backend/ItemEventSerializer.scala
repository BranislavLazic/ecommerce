package com.ecommerce.inventory.backend

import akka.serialization.Serializer
import io.circe.generic.auto._
import io.circe.parser._
import io.circe.syntax._
import io.circe.java8.time._
import com.ecommerce.inventory.backend.InventoryItemManager.Event

/**
  * Created by lukewyman on 1/5/17.
  */
class ItemEventSerializer extends Serializer {
  def identifier: Int = 1212121

  def includeManifest: Boolean = false

  def fromBinary(bytes: Array[Byte], manifest: Option[Class[_]]): AnyRef = decode[Event](new String(bytes))

  def toBinary(obj: AnyRef): Array[Byte] = obj match {
    case event: Event => obj.asInstanceOf[Event].asJson.noSpaces.getBytes
    case other => throw new Exception(s"Cannot serialize ${other} of type ${other.getClass}")
  }
}
