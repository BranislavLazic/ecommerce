package com.ecommerce.shoppingcart.backend

import akka.actor.{Props, Actor}
import akka.cluster.sharding.{ClusterShardingSettings, ClusterSharding}

/**
  * Created by lukewyman on 12/12/16.
  */
object ShoppingCarts {

  def props = Props(new ShoppingCarts)

  def name = "shoppingcarts"

}

class ShoppingCarts extends Actor {

  ClusterSharding(context.system).start(
    ShoppingCartManager.regionName,
    ShoppingCartManager.props,
    ClusterShardingSettings(context.system),
    ShoppingCartManager.extractEntityId,
    ShoppingCartManager.extractShardId
  )

  def shoppingCart = ClusterSharding(context.system).shardRegion(ShoppingCartManager.regionName)

  def receive = {
    case cmd: Command => shoppingCart forward cmd
    case qry: Query => shoppingCart forward qry
  }
}
