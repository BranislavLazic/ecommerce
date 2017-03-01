package com.ecommerce.productcatalog.backend.data

import akka.actor.Actor
import slick.jdbc.MySQLProfile.api._
import slick.lifted.MappedProjection


/**
  * Created by lukewyman on 2/26/17.
  */
private[data] trait Database { val db = Database.forConfig("mysqlDb") }
