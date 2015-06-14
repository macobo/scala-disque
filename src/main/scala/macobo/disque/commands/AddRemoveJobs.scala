package macobo.disque.commands

import macobo.disque.{BulkString, Multi, Disque}

case class Job[T](value: T, id: JobId, source: Option[String] = None)

case class JobId(id: String)

trait AddRemoveJobs { self: Disque =>
  def parseJob(job: Multi): Job[String] = {
    job.results match {
      case List(BulkString(queueName), BulkString(id), BulkString(jobDesc)) =>
        Job(jobDesc, JobId(id), Some(queueName))
    }
  }

  private def parseJobList(jobs: Multi) =
    jobs.results.map {
      case job: Multi => parseJob(job)
    }

  def addJob(queueName: String, job: String, timeout: Long): JobId = {
    val resp = send("ADDJOB", Seq(queueName, job, timeout)) { as(simpleStringResponse) }
    JobId(resp.s)
  }

  def queueLength(queueName: String): Long =
    send("QLEN", Seq(queueName))(as(integerResponse).n)

  def getJobMulti(queueNames: Seq[String], timeout: Option[Int] = Some(100)): Option[Job[String]] = {
    val args = ((timeout match {
      case Some(t) => List("TIMEOUT", t)
      case None => Nil
    }) :+ "FROM") ++ queueNames

    val response = send("GETJOB", args)(as(multiResponse orElse bulkResponse))
    response match {
      case Multi(List(job: Multi)) => Some(parseJob(job))
      case Multi(Nil) => None
    }
  }

  def getJob(queueName: String, timeout: Option[Int] = Some(100)): Option[Job[String]] =
    getJobMulti(Seq(queueName), timeout)

  def peek(queueName: String, count: Int = 10): List[Job[String]] = {
    val response = send("QPEEK", Seq(queueName, count))(as(multiResponse))
    parseJobList(response)
  }

  def acknowledge(jobIds: Seq[JobId]): Long =
    send("ACKJOB", jobIds.map { _.id })(as(integerResponse).n)

  def acknowledge(jobId: JobId): Boolean =
    acknowledge(Seq(jobId)) == 1L

  def deleteJob(jobId: JobId): Boolean =
    deleteJobs(List(jobId)) == 1

  def deleteJobs(jobs: Seq[JobId]): Long = {
    val ids = jobs.map { _.id }
    send("DELJOB", ids)(as(integerResponse).n)
  }

//  def getResponseType(command: String, arguments: Seq[Any]) =
//    send(command, arguments)(as(anyResponse))
}
