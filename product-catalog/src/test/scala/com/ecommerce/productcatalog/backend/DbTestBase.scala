package com.ecommerce.productcatalog.backend

import java.util.concurrent.TimeUnit

import com.wix.mysql.EmbeddedMysql._
import com.wix.mysql.ScriptResolver._
import com.wix.mysql.config.MysqldConfig._
import com.wix.mysql.distribution.Version._
import slick.jdbc.MySQLProfile.api._

/**
  * Created by lukewyman on 2/28/17.
  */
object DbTestBase {

  val config = aMysqldConfig(v5_7_16)
    .withPort(3307)
    .withUser("test", "password1")
      .withTimeout(1, TimeUnit.MINUTES)
    .build();

  val makeInstance =  anEmbeddedMysql(config)
    .addSchema("productcatalog", classPathScript("db/001-schema.sql"))
    .start()

  //val db = Database.forURL("jdbc:mysql://localhost:3307/productcatalog", driver = "com.mysql.jdbc.Driver", user = "test", password = "password1")
  val db = Database.forConfig("mysqlDb")
}
