package com.ecommerce.inventory.backend

import akka.cluster.sharding.ShardRegion
import com.ecommerce.inventory.backend.InventoryItemManager.{Query, Command}

/**
  * Created by lukewyman on 1/5/17.
  */
object ShardSupport {

  val regionName = "inventoryitems"

  val extractEntityId: ShardRegion.ExtractEntityId = {
    case cmd: Command => (cmd.item.id.toString, cmd)
    case qry: Query => (qry.item.id.toString, qry)
  }

  val extractShardId: ShardRegion.ExtractShardId = {
    case cmd: Command => toHex(cmd.item.id.hashCode & 255)
    case qry: Command => toHex(qry.item.id.hashCode & 255)
  }

  private def toHex(b: Int) =
    new java.lang.StringBuilder(2)
      .append(hexDigits(b >> 4))
      .append(hexDigits(b & 15))
      .toString

  private val hexDigits = "0123456789ABCDEF"

}
