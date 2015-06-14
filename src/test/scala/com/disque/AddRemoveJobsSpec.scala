package com.disque

import com.disque.commands.JobId
import org.scalatest.{BeforeAndAfterAll, MustMatchers, WordSpec}

// These tests assume that disque is running on default port
class AddRemoveJobsSpec extends WordSpec with MustMatchers with BeforeAndAfterAll {
  val client = new DisqueClient()
  val queueName = "testQueue2"
  def fakeJobId =
    JobId("DI00000000000000000000000000000000000000000000SQ")

  def clearQueue(queueName: String) = {
    while (client.queueLength(queueName) != 0) {
      val ids = client.peek(queueName).map { _.id }
      client.deleteJobs(queueName, ids)
    }
  }

  override def beforeAll() = {
    clearQueue(queueName)
  }

  "COMMANDS" should {
    "QLEN and ADDJOB/DELJOB -" should {
      "queues should start empty" in {
        client.queueLength(queueName) must equal(0L)
      }

      var jobId: JobId = null
      "adding a job should increment queue length" in {
        jobId = client.addJob(queueName, "testJob", 10000)
        client.queueLength(queueName) must equal(1L)
      }

      "deleteJob should return false if no jobs are removed" in {
        client.deleteJob(queueName, fakeJobId) must equal(false)
      }

      "deleteJob should decrement job queue length" in {
        client.deleteJob(queueName, jobId) must equal(true)
        client.queueLength(queueName) must equal(0L)
      }
    }
  }
}
