package com.ecommerce.inventory.backend

import akka.cluster.sharding.ShardRegion

/**
  * Created by lukewyman on 1/5/17.
  */
object ShardSupport {

  val regionName = "inventoryitems"

  val extractEntityId: ShardRegion.ExtractEntityId = ???

  val extractShardId: ShardRegion.ExtractShardId = ???

}
