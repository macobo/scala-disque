package com.disque.commands

import com.disque.Disque

case class JobId(id: String)

trait AddRemoveJobs { self: Disque =>
  def addJob(queueName: String, job: String, timeout: Long): JobId = {
    val resp = send("ADDJOB", Seq(queueName, job, timeout)) { as(simpleStringResponse) }
    JobId(resp.s)
  }
}
