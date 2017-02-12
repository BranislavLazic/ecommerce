package com.ecommerce.orchestrator.backend.orchestrator

import akka.testkit.TestKit
import org.scalatest.{Suite, BeforeAndAfterAll}

/**
  * Created by lukewyman on 2/11/17.
  */
trait StopSystemAfterAll extends BeforeAndAfterAll {
  this: TestKit with Suite =>
  override protected def afterAll(): Unit = {
    super.afterAll()
    system.terminate()
  }
}