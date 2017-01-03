//package com.ecommerce.inventory.backend
//
//import java.time.format.DateTimeFormatter
//
//import akka.serialization.Serializer
//import io.circe._
//import io.circe.generic.auto._
//import io.circe.parser._
//import io.circe.syntax._
//import io.circe.java8.time._
//
///**
//  * Created by lukewyman on 1/1/17.
//  */
//class StockEventSerializer extends Serializer {
//  import StockMessage.Event
//
//  def identifier: Int = 1212121
//
//  def includeManifest: Boolean = false
//
//  def fromBinary(bytes: Array[Byte], manifest: Option[Class[_]]): AnyRef = decode[Event](new String(bytes))
//
//  def toBinary(obj: AnyRef): Array[Byte] = obj match {
//    case event: StockMessage.Event => obj.asInstanceOf[Event].asJson.noSpaces.getBytes
//    case other => throw new Exception(s"Cannot serialize ${other} of type ${other.getClass}")
//  }
//}
//
//class BackorderEventSerializer extends Serializer {
//
//  def identifier: Int = 2323232
//
//  def includeManifest: Boolean = false
//
//  def fromBinary(bytes: Array[Byte], manifest: Option[Class[_]]): AnyRef = decode[BackorderMessage.Event](new String(bytes))
//
//  def toBinary(obj: AnyRef): Array[Byte] = obj match {
//    case event: BackorderMessage.Event => obj.asInstanceOf[BackorderMessage.Event].asJson.noSpaces.getBytes
//    case other => throw new Exception(s"Cannot serialize ${other} of type ${other.getClass}")
//  }
//}
