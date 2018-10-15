/**
 * Copyright (C) 2016 Stratio (http://stratio.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.stratio.spray.oauth2.client

import java.lang.management.ManagementFactory

import org.slf4j.{Logger, LoggerFactory, MDC}

trait AuditLevel{
  def level: String
}

object Auditable extends AuditLevel{
  val level = "1"
}

object NoAuditable extends AuditLevel{
  val level = "0"
}

trait MDCKey{
  def value: String
}

object MDCUser extends MDCKey {
  val value = "mdc-user"
}

object MDCAudit extends MDCKey {
  val value = "mdc-audit"
}

object MDCProcess extends MDCKey {
  val value = "mdc-process"
}

object MDCEmptyValue{
  val value = "-"
}

trait Logging extends scala.AnyRef {
  protected lazy val logger = getLogger
  protected lazy val pid = ManagementFactory.getRuntimeMXBean().getName().split("@")(0)

  def setMDCKey(key:MDCKey,value:String): Unit ={
    MDC.put(key.value, value)
  }

  private def getLogger(): Logger ={
    val logger=LoggerFactory.getLogger(this.getClass)
    setMDCKey(MDCProcess,pid)
    setMDCKey(MDCAudit,NoAuditable.level)
    logger
  }
}
