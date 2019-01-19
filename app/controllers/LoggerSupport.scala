package controllers

import org.slf4j.LoggerFactory

trait LoggerSupport {

  val logger = LoggerFactory.getLogger(this.getClass)

}