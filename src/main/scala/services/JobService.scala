package services

import scala.concurrent._
import ExecutionContext.Implicits.global
import scala.collection.mutable.ListBuffer

import scala.util.{ Failure, Success }

final case class Job(name: String, id: Long, status: String)

//@Singleton
object JobService {

  val jobs = new ListBuffer[Job]()

  var currentId = 0

  def findJob(jobId: Int): Future[Option[Job]] = {
    return Future {
      findJobById(jobId)
    }
  }

  def findJobById(jobId: Int) =  jobs.filter(_.id == jobId).headOption


  def startJob(jobName: String): Future[Option[Job]] = {
    return Future {
      //if(job == None){
      currentId = currentId + 1
      val jobNew = Job(jobName, currentId, "Started")
      jobs += jobNew
      //}
      Some(jobNew)
    }
  }

  def stopJob(jobId: Int): Future[Option[Job]] = {
    return Future {
      val job = findJobById(jobId)
      job match {
        case Some(x) => { val y = x.copy(status = "Stopped"); Some(y) }
        case None    => None
      }
    }
  }

}